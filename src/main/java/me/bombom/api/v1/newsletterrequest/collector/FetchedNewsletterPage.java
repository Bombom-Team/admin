package me.bombom.api.v1.newsletterrequest.collector;

import java.util.List;
import java.util.Objects;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

/**
 * 뉴스레터 페이지에서 LLM에 넘길 정보만 추린 결과.
 */
public record FetchedNewsletterPage(
        String url,
        String title,
        String ogTitle,
        String ogDescription,
        String ogImage,
        String faviconUrl,
        String bodyText,
        List<PageLink> links
) {

    private static final int BODY_TEXT_MAX_LENGTH = 12_000;
    private static final int LINK_MAX_COUNT = 80;

    public static FetchedNewsletterPage from(Document document) {
        return new FetchedNewsletterPage(
                document.location(),
                document.title(),
                meta(document, "og:title"),
                meta(document, "og:description"),
                absoluteMeta(document, "og:image"),
                favicon(document),
                truncate(document.body() == null ? "" : document.body().text()),
                links(document)
        );
    }

    private static String meta(Document document, String property) {
        Element element = document.selectFirst("meta[property=" + property + "], meta[name=" + property + "]");
        if (element == null) {
            return null;
        }
        return element.attr("content");
    }

    private static String absoluteMeta(Document document, String property) {
        Element element = document.selectFirst("meta[property=" + property + "], meta[name=" + property + "]");
        if (element == null) {
            return null;
        }
        return element.absUrl("content");
    }

    private static String favicon(Document document) {
        Element element = document.selectFirst("link[rel~=(?i)^(shortcut )?icon$], link[rel=apple-touch-icon]");
        if (element == null) {
            return null;
        }
        return element.absUrl("href");
    }

    private static List<PageLink> links(Document document) {
        return document.select("a[href]")
                .stream()
                .map(anchor -> new PageLink(anchor.text().strip(), anchor.absUrl("href")))
                .filter(link -> link.url().startsWith("http"))
                .distinct()
                .limit(LINK_MAX_COUNT)
                .toList();
    }

    private static String truncate(String text) {
        String value = Objects.requireNonNullElse(text, "");
        if (value.length() <= BODY_TEXT_MAX_LENGTH) {
            return value;
        }
        return value.substring(0, BODY_TEXT_MAX_LENGTH);
    }

    public record PageLink(
            String text,
            String url
    ) {
    }
}
