package me.bombom.api.v1.file.service;

import io.awspring.cloud.s3.S3Template;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile file) {
        String originalFileName = file.getOriginalFilename();
        String storeFileName = createStoreFileName(originalFileName);

        try (InputStream inputStream = file.getInputStream()) {
            s3Template.upload(bucketName, storeFileName, inputStream);
            return s3Template.download(bucketName, storeFileName)
                    .getURL().toString();
        } catch (IOException e) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "s3Upload");
        }
    }

    private String createStoreFileName(String originalFilename) {
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return datePath + "/" + originalFilename;
    }
}
