package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.NewsletterSortType;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterSubscriptionCountRepository;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ NewsletterService.class, QuerydslConfig.class })
class NewsletterServiceTest {

        @Autowired
        private NewsletterService newsletterService;

        @Autowired
        private NewsletterRepository newsletterRepository;

        @Autowired
        private NewsletterDetailRepository newsletterDetailRepository;

        @Autowired
        private NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;

        @Autowired
        private CategoryRepository categoryRepository;

        @Autowired
        private NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;

        @Test
        @DisplayName("뉴스레터를 생성한다.")
        void createNewsletter() {
                // given
                Category category = categoryRepository.save(Category.builder()
                                .name("경제")
                                .build());

                CreateNewsletterRequest request = new CreateNewsletterRequest(
                                "테스트 뉴스레터",
                                "설명",
                                "image.png",
                                "test@test.com",
                                "경제", // Category Name
                                "main.com",
                                "sub.com",
                                "weekly",
                                "sender",
                                null,
                                "email");

                // when
                newsletterService.create(request);

                // then
                assertSoftly(softly -> {
                        softly.assertThat(newsletterRepository.findAll()).hasSize(1);
                        softly.assertThat(newsletterDetailRepository.findAll()).hasSize(1);
                        softly.assertThat(newsletterPreviousPolicyRepository.findAll()).hasSize(1);

                        Newsletter newsletter = newsletterRepository.findAll().getFirst();
                        softly.assertThat(newsletter.getCategoryId()).isEqualTo(category.getId());
                });
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 생성 시 예외가 발생한다.")
        void createNewsletter_invalidCategory() {
                // given
                CreateNewsletterRequest request = new CreateNewsletterRequest(
                                "테스트 뉴스레터",
                                "설명",
                                "image.png",
                                "test@test.com",
                                "없는카테고리",
                                "main.com",
                                "sub.com",
                                "weekly",
                                "sender",
                                null,
                                "email");

                // when & then
                assertThatThrownBy(() -> newsletterService.create(request))
                                .isInstanceOf(CIllegalArgumentException.class)
                                .hasMessage("존재하지 않는 데이터입니다.");
        }

        @Test
        @DisplayName("뉴스레터 목록을 조회한다.")
        void getNewslettersDetail() {
                // given
                Category category = categoryRepository.save(Category.builder().name("테크").build());

                for (int i = 0; i < 30; i++) {
                        NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                                        .mainPageUrl("https://example.com/" + i)
                                        .subscribeUrl("https://subscribe.com/" + i)
                                        .issueCycle("매주 월요일")
                                        .sender("보내는 사람 " + i)
                                        .build());

                        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                                        .name("뉴스레터 " + i)
                                        .description("설명 " + i)
                                        .imageUrl("https://image.com/" + i)
                                        .email("test" + i + "@example.com")
                                        .detailId(detail.getId())
                                        .categoryId(category.getId())
                                        .build());

                        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.builder()
                                        .newsletterId(newsletter.getId())
                                        .total(100)
                                        .build());
                }

                GetNewslettersRequest request = new GetNewslettersRequest(null, null, null);

                // when
                List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

                // then
                // then
                assertSoftly(softly -> {
                        softly.assertThat(result).hasSize(30);
                        // Default sort desc by createdAt
                        // Since we created them in a loop, the last one created (id 29) should be first
                        softly.assertThat(result.getFirst().name()).isEqualTo("뉴스레터 29");
                        softly.assertThat(result.getFirst().issueCycle()).isEqualTo("매주 월요일");
                });
        }

        @Test
        @DisplayName("뉴스레터 목록을 인기순(구독자수)으로 정렬한다.")
        void getNewsletters_sortByPopularity() {
                // given
                Category category = categoryRepository.save(Category.builder().name("테크").build());

                // Create 3 newsletters with different subscription counts
                createNewsletterWithSubscriberCount(category, "뉴스레터 1", 100);
                createNewsletterWithSubscriberCount(category, "뉴스레터 2", 300);
                createNewsletterWithSubscriberCount(category, "뉴스레터 3", 200);

                GetNewslettersRequest request = new GetNewslettersRequest(null, null, NewsletterSortType.POPULAR);

                // when
                List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

                // then
                // then
                assertSoftly(softly -> {
                        softly.assertThat(result).hasSize(3);
                        softly.assertThat(result.get(0).name()).isEqualTo("뉴스레터 2"); // 300
                        softly.assertThat(result.get(1).name()).isEqualTo("뉴스레터 3"); // 200
                        softly.assertThat(result.get(2).name()).isEqualTo("뉴스레터 1"); // 100
                });
        }

        @Test
        @DisplayName("뉴스레터 목록을 카테고리로 필터링한다.")
        void getNewsletters_filterByCategoryDetail() {
                // given
                Category tech = categoryRepository.save(Category.builder().name("테크").build());
                Category economy = categoryRepository.save(Category.builder().name("경제").build());

                createNewsletterWithCategory(tech, "테크 뉴스레터", "매주 월요일");
                createNewsletterWithCategory(economy, "경제 뉴스레터", "매주 화요일");

                GetNewslettersRequest request = new GetNewslettersRequest(null, "테크", null);

                // when
                List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

                // then
                assertSoftly(softly -> {
                        softly.assertThat(result).hasSize(1);
                        softly.assertThat(result.getFirst().categoryName()).isEqualTo("테크");
                });
        }

        @Test
        @DisplayName("뉴스레터 목록을 발행 주기로 검색한다.")
        void getNewsletters_searchByIssueCycleDetail() {
                // given
                Category tech = categoryRepository.save(Category.builder().name("테크").build());

                createNewsletterWithCategory(tech, "월요 뉴스레터", "매주 월요일");
                createNewsletterWithCategory(tech, "화요 뉴스레터", "매주 화요일");

                GetNewslettersRequest request = new GetNewslettersRequest("월요일", null, null);

                // when
                List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

                // then
                assertSoftly(softly -> {
                        softly.assertThat(result).hasSize(1);
                        softly.assertThat(result.getFirst().issueCycle()).isEqualTo("매주 월요일");
                });
        }

        @Test
        @DisplayName("뉴스레터 상세 정보를 조회한다.")
        void getNewsletterDetail() {
                // given
                Category category = categoryRepository.save(Category.builder().name("테크").build());
                NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                                .mainPageUrl("https://example.com")
                                .subscribeUrl("https://subscribe.com")
                                .issueCycle("매주 월요일")
                                .sender("sender")
                                .build());

                Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                                .name("테크 뉴스레터")
                                .description("desc")
                                .imageUrl("img")
                                .email("email")
                                .detailId(detail.getId())
                                .categoryId(category.getId())
                                .build());

                newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.builder()
                                .newsletterId(newsletter.getId())
                                .total(100)
                                .build());

                // when
                GetNewsletterResponse response = newsletterService.getNewsletterDetail(newsletter.getId());

                // then
                assertSoftly(softly -> {
                        softly.assertThat(response.name()).isEqualTo("테크 뉴스레터");
                        softly.assertThat(response.categoryName()).isEqualTo("테크");
                        softly.assertThat(response.sender()).isEqualTo("sender");
                        softly.assertThat(response.subscribeCount()).isEqualTo(100);
                });
        }

        @Test
        @DisplayName("존재하지 않는 뉴스레터 조회 시 예외가 발생한다.")
        void getNewsletter_Detail_notFound() {
                // when & then
                assertThatThrownBy(() -> newsletterService.getNewsletterDetail(999L))
                                .isInstanceOf(CIllegalArgumentException.class);
        }

        private void createNewsletterWithCategory(Category category, String name, String issueCycle) {
                NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                                .mainPageUrl("https://example.com")
                                .subscribeUrl("https://subscribe.com")
                                .issueCycle(issueCycle)
                                .sender("sender")
                                .build());

                Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                                .name(name)
                                .description("desc")
                                .imageUrl("img")
                                .email("email")
                                .detailId(detail.getId())
                                .categoryId(category.getId())
                                .build());

                newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(newsletter.getId()));
        }

        private void createNewsletterWithSubscriberCount(Category category, String name, int count) {
                NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                                .mainPageUrl("https://example.com")
                                .subscribeUrl("https://subscribe.com")
                                .issueCycle("weekly")
                                .sender("sender")
                                .build());

                Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                                .name(name)
                                .description("desc")
                                .imageUrl("img")
                                .email("email")
                                .detailId(detail.getId())
                                .categoryId(category.getId())
                                .build());

                newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.builder()
                                .newsletterId(newsletter.getId())
                                .total(count)
                                .build());
        }
}
