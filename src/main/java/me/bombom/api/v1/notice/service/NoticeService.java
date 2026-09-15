package me.bombom.api.v1.notice.service;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeImageAsset;
import me.bombom.api.v1.notice.domain.NoticeImageAssetStatus;
import me.bombom.api.v1.notice.domain.NoticeRepresentative;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.CreateNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticeDetailResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.repository.NoticeImageAssetRepository;
import me.bombom.api.v1.notice.repository.NoticeRepresentativeRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final Clock clock;
    private final NoticeRepository noticeRepository;
    private final NoticeImageAssetRepository noticeImageAssetRepository;
    private final NoticeRepresentativeRepository noticeRepresentativeRepository;

    public Page<GetNoticeResponse> getNotices(GetNoticesRequest request, Pageable pageable) {
        return noticeRepository.findNotices(request, pageable);
    }

    public GetNoticeDetailResponse getNotice(Long id) {
        return noticeRepository.findById(id)
                .map(GetNoticeDetailResponse::from)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                        .addContext(ErrorContextKeys.OPERATION, "getNotice"));
    }

    @Transactional
    public CreateNoticeResponse createNotice() {
        Notice notice = Notice.builder()
                .visibility(NoticeVisibility.PRIVATE)
                .build();
        return CreateNoticeResponse.from(noticeRepository.save(notice));
    }

    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        request.validate();

        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.NOTICE_ID, id));

        if (request.isRepresentative() != null) {
            applyRepresentative(id, request.isRepresentative());
        }

        notice.update(
            request.title(),
            request.content(),
            request.noticeCategory(),
            request.visibility()
        );

        if (request.referencedImageIds() != null) {
            updateReferencedImages(id, request.distinctReferencedImageIds());
        }
    }

    private void applyRepresentative(Long noticeId, boolean isRepresentative) {
        if (isRepresentative) {
            noticeRepresentativeRepository.findById(NoticeRepresentative.SINGLETON_ID)
                    .ifPresentOrElse(
                            representative -> representative.changeTo(noticeId),
                            () -> noticeRepresentativeRepository.save(NoticeRepresentative.of(noticeId)));
            return;
        }

        noticeRepresentativeRepository.deleteByNoticeId(noticeId);
    }

    private void updateReferencedImages(Long noticeId, List<Long> referencedImageIds) {
        List<NoticeImageAsset> noticeImages = noticeImageAssetRepository.findAllByNoticeId(noticeId);
        Map<Long, NoticeImageAsset> noticeImageMap = new LinkedHashMap<>();
        for (NoticeImageAsset noticeImage : noticeImages) {
            noticeImageMap.put(noticeImage.getId(), noticeImage);
        }

        validateReferencedImages(noticeId, referencedImageIds, noticeImageMap);

        for (Long referencedImageId : referencedImageIds) {
            noticeImageMap.get(referencedImageId).attach();
        }

        LocalDateTime deleteRequestedAt = LocalDateTime.now(clock);
        for (NoticeImageAsset noticeImage : noticeImages) {
            boolean isReferencedImage = referencedImageIds.contains(noticeImage.getId());
            if (isReferencedImage) {
                continue;
            }

            boolean isAttachedImage = noticeImage.getStatus() == NoticeImageAssetStatus.ATTACHED;
            if (isAttachedImage) {
                noticeImage.markDeletePending(deleteRequestedAt);
            }
        }
    }

    private void validateReferencedImages(
            Long noticeId,
            List<Long> referencedImageIds,
            Map<Long, NoticeImageAsset> noticeImageMap
    ) {
        if (referencedImageIds.isEmpty()) {
            return;
        }

        List<NoticeImageAsset> referencedImages = noticeImageAssetRepository.findAllByIdIn(referencedImageIds);
        if (referencedImages.size() != referencedImageIds.size()) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "noticeImageAsset")
                    .addContext(ErrorContextKeys.OPERATION, "updateNotice");
        }

        boolean containsForeignImage = referencedImages.stream()
                .anyMatch(image -> !image.getNoticeId().equals(noticeId));

        if (containsForeignImage) {
            throw invalidInput("referencedImageIds");
        }

        boolean containsUnknownImage = referencedImageIds.stream()
                .anyMatch(imageId -> !noticeImageMap.containsKey(imageId));

        if (containsUnknownImage) {
            throw invalidInput("referencedImageIds");
        }
    }

    private CIllegalArgumentException invalidInput(String field) {
        return new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                .addContext("field", field);
    }

    @Transactional
    public void deleteNotice(@Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                    .addContext(ErrorContextKeys.OPERATION, "deleteNotice");
        }

        // TODO: S3 delete 지원이 추가되면 DELETE_PENDING 자산의 실제 객체 삭제 수행
        LocalDateTime deleteRequestedAt = LocalDateTime.now(clock);
        for (NoticeImageAsset noticeImage : noticeImageAssetRepository.findAllByNoticeId(id)) {
            noticeImage.markDeletePending(deleteRequestedAt);
        }

        noticeRepository.deleteById(id);
    }
}
