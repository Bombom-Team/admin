package me.bombom.api.v1.file.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.file.dto.UploadFileResponse;
import me.bombom.api.v1.file.dto.StoredFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class FileUploadService {

    private static final String NOTICE_IMAGE_PREFIX = "notices";

    private final S3FileService s3FileService;

    @Value("${spring.cloud.aws.s3.notice-bucket}")
    private String noticeBucketName;

    public UploadFileResponse uploadNoticeImage(MultipartFile multipartFile) {
        StoredFile storedFile = s3FileService.uploadToBucketWithMetadata(
                multipartFile,
                noticeBucketName,
                NOTICE_IMAGE_PREFIX
        );
        return UploadFileResponse.from(storedFile);
    }
}
