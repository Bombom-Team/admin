package me.bombom.api.v1.notice.service;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.dto.GetNoticeDetailResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.repository.NoticeRepository;

import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

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
    public void createNotice(CreateNoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .noticeCategory(request.noticeCategory())
                .build();
        noticeRepository.save(notice);
    }

    @Transactional
    public void updateNotice(Long id, UpdateNoticeRequest request) {
        Notice notice = noticeRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                        .addContext(ErrorContextKeys.OPERATION, "updateNotice"));
        notice.update(request.title(), request.content(), request.noticeCategory());
    }

    @Transactional
    public void deleteNotice(@Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        if (!noticeRepository.existsById(id)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                    .addContext(ErrorContextKeys.OPERATION, "deleteNotice");
        }
        noticeRepository.deleteById(id);
    }
}
