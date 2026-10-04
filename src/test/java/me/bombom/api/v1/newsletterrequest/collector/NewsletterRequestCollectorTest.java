package me.bombom.api.v1.newsletterrequest.collector;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;
import me.bombom.api.v1.newsletterrequest.dto.response.CollectTarget;
import me.bombom.api.v1.newsletterrequest.service.NewsletterRequestCollectService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * 외부 호출(페이지, LLM, S3)을 대역으로 바꿔 수집 흐름만 검증한다. DB 반영은 NewsletterRequestCollectServiceTest가 맡는다.
 */
@ExtendWith(MockitoExtension.class)
class NewsletterRequestCollectorTest {

    private static final CollectTarget TARGET = new CollectTarget(10L, 1L, "주간 개발 노트", "https://weeklydev.stibee.com");
    private static final List<String> CATEGORY_NAMES = List.of("IT/테크");
    private static final FetchedNewsletterPage PAGE = new FetchedNewsletterPage(
            "https://weeklydev.stibee.com",
            "주간 개발 노트",
            null,
            null,
            "https://weeklydev.stibee.com/og.png",
            null,
            "본문",
            List.of()
    );

    @Mock
    private NewsletterRequestCollectService newsletterRequestCollectService;

    @Mock
    private NewsletterPageFetcher newsletterPageFetcher;

    @Mock
    private NewsletterInfoExtractor newsletterInfoExtractor;

    @Mock
    private NewsletterThumbnailUploader newsletterThumbnailUploader;

    @InjectMocks
    private NewsletterRequestCollector newsletterRequestCollector;

    @Test
    void 수집에_성공하면_추출한_값으로_초안을_채우고_빈_링크는_신청_링크로_채운다() {
        // given
        given(newsletterRequestCollectService.claimTargets()).willReturn(List.of(TARGET));
        given(newsletterRequestCollectService.getCategoryNames()).willReturn(CATEGORY_NAMES);
        given(newsletterRequestCollectService.findCategoryId("IT/테크")).willReturn(3L);
        given(newsletterPageFetcher.fetchPage(TARGET.requestedUrl())).willReturn(PAGE);
        given(newsletterInfoExtractor.extract(TARGET.requestedName(), PAGE, CATEGORY_NAMES)).willReturn(extractedInfo());
        given(newsletterThumbnailUploader.upload(anyList())).willReturn("https://cdn.bombom.news/newsletters/a.png");

        // when
        newsletterRequestCollector.collectPending();

        // then
        ArgumentCaptor<DraftContent> captor = ArgumentCaptor.forClass(DraftContent.class);
        verify(newsletterRequestCollectService).complete(eq(TARGET.draftId()), captor.capture());
        DraftContent content = captor.getValue();
        assertSoftly(softly -> {
            softly.assertThat(content.name()).isEqualTo("주간 개발 노트");
            softly.assertThat(content.categoryId()).isEqualTo(3L);
            softly.assertThat(content.imageUrl()).isEqualTo("https://cdn.bombom.news/newsletters/a.png");
            softly.assertThat(content.mainPageUrl()).isEqualTo(TARGET.requestedUrl());
            softly.assertThat(content.subscribeUrl()).isEqualTo("https://weeklydev.stibee.com/subscribe");
        });
    }

    @Test
    void 수집에_실패하면_실패_사유를_남긴다() {
        // given
        given(newsletterRequestCollectService.claimTargets()).willReturn(List.of(TARGET));
        given(newsletterRequestCollectService.getCategoryNames()).willReturn(CATEGORY_NAMES);
        given(newsletterPageFetcher.fetchPage(TARGET.requestedUrl()))
                .willThrow(new CollectFailedException("페이지 수집 실패: timeout"));

        // when
        newsletterRequestCollector.collectPending();

        // then
        verify(newsletterRequestCollectService).fail(TARGET.draftId(), "페이지 수집 실패: timeout");
        verify(newsletterRequestCollectService, never()).complete(any(), any());
    }

    private static ExtractedNewsletterInfo extractedInfo() {
        return new ExtractedNewsletterInfo(
                "주간 개발 노트",
                "매주 월요일 개발 글 5편을 보내드려요.",
                "IT/테크",
                "매주 월요일",
                "주간 개발 노트 편집팀",
                null,
                "https://weeklydev.stibee.com/subscribe",
                null,
                null,
                null
        );
    }
}
