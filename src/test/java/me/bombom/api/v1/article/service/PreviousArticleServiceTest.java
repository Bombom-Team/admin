package me.bombom.api.v1.article.service;

import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.dto.CreatePreviousArticleRequest;
import me.bombom.api.v1.article.dto.GetPreviousArticleResponse;
import me.bombom.api.v1.article.dto.UpdatePreviousArticleRequest;
import me.bombom.api.v1.article.fixture.PreviousArticleFixture;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.fixture.NewsletterFixture;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@Import({ PreviousArticleService.class, QuerydslConfig.class })
class PreviousArticleServiceTest {

    @Autowired
    private PreviousArticleService previousArticleService;

    @Autowired
    private PreviousArticleRepository previousArticleRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Test
    @DisplayName("지난 아티클을 생성한다.")
    void create() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        CreatePreviousArticleRequest request = new CreatePreviousArticleRequest(
                "테스트 아티클",
                "<p>테스트 내용입니다.</p>",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false
        );

        // when
        previousArticleService.create(newsletter.getId(), request);

        // then
        List<PreviousArticle> articles = previousArticleRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(articles).hasSize(1);
            softly.assertThat(articles.getFirst().getTitle()).isEqualTo("테스트 아티클");
            softly.assertThat(articles.getFirst().getContentsSummary()).isNotBlank();
            softly.assertThat(articles.getFirst().getExpectedReadTime()).isGreaterThanOrEqualTo(1);
            softly.assertThat(articles.getFirst().getNewsletterId()).isEqualTo(newsletter.getId());
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터에 아티클 생성 시 예외가 발생한다.")
    void create_newsletterNotFound() {
        // given
        CreatePreviousArticleRequest request = new CreatePreviousArticleRequest(
                "테스트 아티클",
                "<p>내용</p>",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false
        );

        // when & then
        assertThatThrownBy(() -> previousArticleService.create(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("지난 아티클 목록을 조회한다.")
    void getPreviousArticles() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));
        previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));

        // when
        List<GetPreviousArticleResponse> result = previousArticleService.getPreviousArticles(newsletter.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.getFirst().newsletterId()).isEqualTo(newsletter.getId());
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터의 아티클 목록 조회 시 예외가 발생한다.")
    void getPreviousArticles_newsletterNotFound() {
        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticles(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("지난 아티클 상세를 조회한다.")
    void getPreviousArticle() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        PreviousArticle article = previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));

        // when
        GetPreviousArticleResponse result = previousArticleService.getPreviousArticle(newsletter.getId(), article.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(article.getId());
            softly.assertThat(result.newsletterId()).isEqualTo(newsletter.getId());
        });
    }

    @Test
    @DisplayName("존재하지 않는 아티클 조회 시 예외가 발생한다.")
    void getPreviousArticle_notFound() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));

        // when & then
        assertThatThrownBy(() -> previousArticleService.getPreviousArticle(newsletter.getId(), 999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("지난 아티클을 수정한다.")
    void update() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        PreviousArticle article = previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));

        UpdatePreviousArticleRequest request = new UpdatePreviousArticleRequest(
                "수정된 제목",
                "<p>수정된 내용입니다.</p>",
                LocalDateTime.of(2025, 6, 1, 9, 0),
                true
        );

        // when
        previousArticleService.update(newsletter.getId(), article.getId(), request);

        // then
        PreviousArticle updated = previousArticleRepository.findById(article.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getTitle()).isEqualTo("수정된 제목");
            softly.assertThat(updated.getContents()).isEqualTo("<p>수정된 내용입니다.</p>");
            softly.assertThat(updated.getContentsSummary()).isNotBlank();
            softly.assertThat(updated.isFixed()).isTrue();
        });
    }

    @Test
    @DisplayName("contents 수정 시 contentsSummary와 expectedReadTime이 재계산된다.")
    void update_recalculatesHtmlFields() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        PreviousArticle article = previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));
        String originalSummary = article.getContentsSummary();

        UpdatePreviousArticleRequest request = new UpdatePreviousArticleRequest(
                null,
                "<p>완전히 새로운 긴 내용입니다. 이 내용은 기존 요약과 달라야 합니다.</p>",
                null,
                null
        );

        // when
        previousArticleService.update(newsletter.getId(), article.getId(), request);

        // then
        PreviousArticle updated = previousArticleRepository.findById(article.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getContentsSummary()).isNotEqualTo(originalSummary);
            softly.assertThat(updated.getExpectedReadTime()).isGreaterThanOrEqualTo(1);
        });
    }

    @Test
    @DisplayName("존재하지 않는 아티클 수정 시 예외가 발생한다.")
    void update_notFound() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        UpdatePreviousArticleRequest request = new UpdatePreviousArticleRequest("수정", null, null, null);

        // when & then
        assertThatThrownBy(() -> previousArticleService.update(newsletter.getId(), 999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("지난 아티클을 삭제한다.")
    void delete() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));
        PreviousArticle article = previousArticleRepository.save(PreviousArticleFixture.create(newsletter.getId()));

        // when
        previousArticleService.delete(newsletter.getId(), article.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(previousArticleRepository.existsById(article.getId())).isFalse();
        });
    }

    @Test
    @DisplayName("존재하지 않는 아티클 삭제 시 예외가 발생한다.")
    void delete_notFound() {
        // given
        Newsletter newsletter = newsletterRepository.save(NewsletterFixture.createNewsletter(0L, 0L, "테스트 뉴스레터", "email@test.com"));

        // when & then
        assertThatThrownBy(() -> previousArticleService.delete(newsletter.getId(), 999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }
}
