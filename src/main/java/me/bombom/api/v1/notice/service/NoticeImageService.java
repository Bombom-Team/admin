package me.bombom.api.v1.notice.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeImageAsset;
import me.bombom.api.v1.notice.domain.NoticeImageAssetStatus;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.UploadNoticeImageResponse;
import me.bombom.api.v1.notice.repository.NoticeImageAssetRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeImageService {

    private static final String NOTICE_IMAGE_PREFIX = "notices";

    private final NoticeRepository noticeRepository;
    private final NoticeImageAssetRepository noticeImageAssetRepository;
    private final S3FileService s3FileService;

    @Value("${spring.cloud.aws.s3.notice-bucket}")
    private String noticeBucketName;

    @Transactional
    public UploadNoticeImageResponse uploadNoticeImage(Long noticeId, MultipartFile imageFile) {
        Notice notice = getNotice(noticeId);
        validateImageFile(imageFile);

        StoredFile storedFile = s3FileService.uploadToBucketWithS3Url(
                imageFile,
                noticeBucketName,
                NOTICE_IMAGE_PREFIX
        );

        try {
            NoticeImageAsset noticeImageAsset = noticeImageAssetRepository.save(NoticeImageAsset.builder()
                    .noticeId(noticeId)
                    .objectKey(storedFile.objectKey())
                    .imageUrl(storedFile.fileUrl())
                    .status(resolveImageStatus(notice))
                    .build());

            return UploadNoticeImageResponse.from(noticeImageAsset);
        } catch (RuntimeException e) {
            // TODO: S3 delete 지원이 추가되면 업로드된 objectKey 보상 삭제 수행
            throw e;
        }
    }

    private Notice getNotice(Long noticeId) {
        return noticeRepository.findById(noticeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "notice")
                        .addContext(ErrorContextKeys.OPERATION, "findById")
                        .addContext(ErrorContextKeys.NOTICE_ID, noticeId));
    }

    private void validateImageFile(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext("field", "imageFile");
        }
    }

    private NoticeImageAssetStatus resolveImageStatus(Notice notice) {
        if (notice.getVisibility() == NoticeVisibility.PUBLIC) {
            return NoticeImageAssetStatus.ATTACHED;
        }

        return NoticeImageAssetStatus.UPLOADED;
    }
}
