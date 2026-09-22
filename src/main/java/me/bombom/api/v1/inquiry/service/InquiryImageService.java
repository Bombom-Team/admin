package me.bombom.api.v1.inquiry.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryImageService {

    private static final int MAX_IMAGE_COUNT = 4;
    private static final String IMAGE_PREFIX = "inquiry";

    private final S3FileService s3FileService;

    @Value("${spring.cloud.aws.s3.inquiry-bucket}")
    private String inquiryBucketName;

    public InquiryImageUploadResponse uploadImages(List<MultipartFile> images) {
        log.info("[Inquiry] 이미지 업로드 요청: {}장", images.size());

        if (images.size() > MAX_IMAGE_COUNT) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                    .addContext(ErrorContextKeys.REASON, "이미지는 최대 4장까지 업로드할 수 있습니다.");
        }

        List<String> imageUrls = images.stream()
                .map(this::uploadImage)
                .toList();

        log.info("[Inquiry] 이미지 업로드 완료: {}장", imageUrls.size());
        return InquiryImageUploadResponse.from(imageUrls);
    }

    private String uploadImage(MultipartFile image) {
        try {
            StoredFile storedFile = s3FileService.uploadToBucketWithMetadata(image, inquiryBucketName, IMAGE_PREFIX);
            return storedFile.fileUrl();
        } catch (RuntimeException e) {
            log.warn("[Inquiry] 이미지 업로드 실패: filename={}", image.getOriginalFilename(), e);
            throw e;
        }
    }
}
