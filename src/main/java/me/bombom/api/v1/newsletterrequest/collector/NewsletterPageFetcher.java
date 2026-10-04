package me.bombom.api.v1.newsletterrequest.collector;

import java.io.IOException;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletterrequest.config.NewsletterRequestCollectProperties;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NewsletterPageFetcher {

    private static final String USER_AGENT = "Mozilla/5.0 (compatible; BombomNewsletterBot/1.0; +https://bombom.news)";
    private static final int PAGE_MAX_BYTES = 2 * 1024 * 1024;
    private static final int IMAGE_MAX_BYTES = 5 * 1024 * 1024;
    private static final int MAX_REDIRECT_COUNT = 5;

    private final NewsletterRequestCollectProperties properties;

    public FetchedNewsletterPage fetchPage(String url) {
        try {
            Connection.Response response = executeWithSafeRedirects(url, PAGE_MAX_BYTES);
            return FetchedNewsletterPage.from(response.parse());
        } catch (IOException exception) {
            throw new CollectFailedException("페이지 수집 실패: " + exception.getMessage(), exception);
        }
    }

    public DownloadedImage downloadImage(String url) {
        Connection.Response response = executeWithSafeRedirects(url, IMAGE_MAX_BYTES);
        return new DownloadedImage(response.bodyAsBytes(), response.contentType());
    }

    /**
     * 리다이렉트를 직접 따라가며 매번 내부망 주소인지 검사한다.
     */
    private Connection.Response executeWithSafeRedirects(String url, int maxBodyBytes) {
        String currentUrl = url;
        for (int redirectCount = 0; redirectCount <= MAX_REDIRECT_COUNT; redirectCount += 1) {
            PublicUrlValidator.validate(currentUrl);
            Connection.Response response = execute(currentUrl, maxBodyBytes);
            if (isRedirect(response)) {
                currentUrl = URI.create(currentUrl).resolve(response.header("Location")).toString();
                continue;
            }
            return response;
        }
        throw new CollectFailedException("리다이렉트가 너무 많음: " + url);
    }

    private Connection.Response execute(String url, int maxBodyBytes) {
        try {
            return Jsoup.connect(url)
                    .userAgent(USER_AGENT)
                    .timeout(properties.getFetchTimeoutMillis())
                    .maxBodySize(maxBodyBytes)
                    .followRedirects(false)
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException exception) {
            throw new CollectFailedException("요청 실패: " + url + " (" + exception.getMessage() + ")", exception);
        }
    }

    private static boolean isRedirect(Connection.Response response) {
        int statusCode = response.statusCode();
        return statusCode >= 300 && statusCode < 400 && response.hasHeader("Location");
    }

    public record DownloadedImage(
            byte[] content,
            String contentType
    ) {

        public boolean isNotImage() {
            return contentType == null || !contentType.startsWith("image/");
        }
    }
}
