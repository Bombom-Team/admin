package me.bombom.api.v1.newsletterrequest.collector;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.jsoup.Jsoup;
import org.junit.jupiter.api.Test;

class FetchedNewsletterPageTest {

    @Test
    void 메타_태그와_파비콘과_링크를_절대_주소로_추린다() {
        // given
        String html = """
                <html><head>
                  <title>주간 개발 노트</title>
                  <meta property="og:title" content="주간 개발 노트 | 스티비">
                  <meta property="og:description" content="매주 월요일 개발 글">
                  <meta property="og:image" content="/images/logo.png">
                  <link rel="icon" href="/favicon.ico">
                </head><body>
                  <p>매주 월요일 아침에 보내드려요.</p>
                  <a href="/subscribe">구독하기</a>
                  <a href="/subscribe">구독하기</a>
                  <a href="mailto:hello@weeklydev.com">문의</a>
                </body></html>
                """;

        // when
        FetchedNewsletterPage page = FetchedNewsletterPage.from(Jsoup.parse(html, "https://weeklydev.stibee.com/"));

        // then
        assertSoftly(softly -> {
            softly.assertThat(page.title()).isEqualTo("주간 개발 노트");
            softly.assertThat(page.ogDescription()).isEqualTo("매주 월요일 개발 글");
            softly.assertThat(page.ogImage()).isEqualTo("https://weeklydev.stibee.com/images/logo.png");
            softly.assertThat(page.faviconUrl()).isEqualTo("https://weeklydev.stibee.com/favicon.ico");
            softly.assertThat(page.bodyText()).contains("매주 월요일 아침에 보내드려요.");
            softly.assertThat(page.links()).extracting(FetchedNewsletterPage.PageLink::url)
                    .containsExactly("https://weeklydev.stibee.com/subscribe");
        });
    }
}
