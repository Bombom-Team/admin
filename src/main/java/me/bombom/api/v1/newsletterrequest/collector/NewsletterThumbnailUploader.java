package me.bombom.api.v1.newsletterrequest.collector;

import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.file.service.S3FileService;
import me.bombom.api.v1.newsletterrequest.collector.NewsletterPageFetcher.DownloadedImage;
import me.bombom.api.v1.newsletterrequest.config.NewsletterRequestCollectProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 후보 이미지를 순서대로 내려받아 첫 번째로 성공한 이미지를 봄봄 S3에 올린다.
 * 외부 이미지 링크는 언제든 깨질 수 있어서 그대로 쓰지 않는다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NewsletterThumbnailUploader {

    private final NewsletterPageFetcher newsletterPageFetcher;
    private final S3FileService s3FileService;
    private final NewsletterRequestCollectProperties properties;

    @Value("${spring.cloud.aws.s3.notice-bucket}")
    private String bucketName;

    /**
     * @return 업로드한 이미지 URL. 모든 후보가 실패하면 null (운영자가 직접 넣는다)
     */
    public String upload(List<String> candidateUrls) {
        return candidateUrls.stream()
                .filter(Objects::nonNull)
                .filter(StringUtils::hasText)
                .distinct()
                .map(this::tryUpload)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private String tryUpload(String candidateUrl) {
        try {
            DownloadedImage image = newsletterPageFetcher.downloadImage(candidateUrl);
            if (image.isNotImage()) {
                return null;
            }
            return s3FileService.uploadImageBytesToBucketWithMetadata(
                    image.content(),
                    image.contentType(),
                    bucketName,
                    properties.getThumbnailPrefix()
            ).fileUrl();
        } catch (RuntimeException exception) {
            log.info("썸네일 후보 업로드 실패: url={}, reason={}", candidateUrl, exception.getMessage());
            return null;
        }
    }
}
