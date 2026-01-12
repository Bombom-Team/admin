package me.bombom.api.v1.file.service;

import io.awspring.cloud.s3.S3Template;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3FileService {

    private static final int MAX_WIDTH = 1000;
    private static final int MAX_HEIGHT = 1000;
    private static final double OUTPUT_QUALITY = 0.8;

    private final S3Template s3Template;

    @Value("${spring.cloud.aws.s3.bucket}")
    private String bucketName;

    @Value("${spring.cloud.aws.cloudfront.domain:}")
    private String cloudFrontDomain;

    public String upload(MultipartFile file) {
        String storeFileName = createStoreFileName(file);

        try (InputStream inputStream = file.getInputStream()) {
            InputStream uploadStream = inputStream;

            if (isImage(file)) {
                uploadStream = resizeImage(inputStream);
            }

            s3Template.upload(bucketName, storeFileName, uploadStream);
            return getFileUrl(storeFileName);
        } catch (IOException e) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "s3Upload");
        }
    }

    private String getFileUrl(String storeFileName) {
        if (StringUtils.hasText(cloudFrontDomain)) {
            return cloudFrontDomain + "/" + storeFileName;
        }
        return getS3Url(storeFileName);
    }

    private String getS3Url(String storeFileName) {
        try {
            return s3Template.download(bucketName, storeFileName).getURL().toString();
        } catch (IOException e) {
            throw new CServerErrorException(ErrorDetail.EXTERNAL_API_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "s3GetUrl");
        }
    }

    private InputStream resizeImage(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Thumbnails.of(inputStream)
                .size(MAX_WIDTH, MAX_HEIGHT)
                .outputQuality(OUTPUT_QUALITY)
                .toOutputStream(outputStream);
        return new ByteArrayInputStream(outputStream.toByteArray());
    }

    private boolean isImage(MultipartFile file) {
        String contentType = file.getContentType();
        return contentType != null && contentType.startsWith("image/");
    }

    private String createStoreFileName(MultipartFile file) {
        String ext = extractExt(file);
        String uuid = UUID.randomUUID().toString();
        String datePath = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        return "notices/" + datePath + "/" + uuid + "." + ext;
    }

    private String extractExt(MultipartFile file) {
        String ext = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (StringUtils.hasText(ext)) {
            return ext;
        }
        return getExtensionFromContentType(file.getContentType());
    }

    private String getExtensionFromContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return "jpg";
        }

        if (contentType.contains("png"))
            return "png";
        if (contentType.contains("gif"))
            return "gif";
        if (contentType.contains("webp"))
            return "webp";
        if (contentType.contains("jpeg"))
            return "jpg";

        return "jpg";
    }
}
