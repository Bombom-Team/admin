package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.NewsletterSortType;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterStatusRequest;
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
                "email",
                NewsletterPreviousStrategy.INACTIVE,
                0,
                0,
                0
        );

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
                "email",
                NewsletterPreviousStrategy.INACTIVE,
                0,
                0,
                0
        );

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

        GetNewslettersRequest request = new GetNewslettersRequest(null, null, null, null);

        // when
        List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

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

        GetNewslettersRequest request = new GetNewslettersRequest(null, null, null, NewsletterSortType.POPULAR);

        // when
        List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

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

        GetNewslettersRequest request = new GetNewslettersRequest(null, "테크", null, null);

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

        GetNewslettersRequest request = new GetNewslettersRequest("월요일", null, null, null);

        // when
        List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getFirst().issueCycle()).isEqualTo("매주 월요일");
        });
    }

    @Test
    @DisplayName("뉴스레터 목록을 지난 뉴스레터 전략으로 필터링한다.")
    void getNewsletters_filterByPreviousStrategy() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                .mainPageUrl("url")
                .subscribeUrl("sub")
                .issueCycle("weekly")
                .sender("sender")
                .build());

        // Newsletter 1: INACTIVE (default)
        Newsletter n1 = newsletterRepository.save(Newsletter.builder()
                .name("n1")
                .description("desc")
                .imageUrl("img")
                .email("email1")
                .detailId(detail.getId())
                .categoryId(category.getId())
                .build());
        newsletterPreviousPolicyRepository.save(me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy
                .builder()
                .newsletterId(n1.getId())
                .strategy(me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.INACTIVE)
                .fixedCount(0)
                .recentCount(0)
                .exposureRatio(0)
                .build());

        NewsletterDetail detail2 = newsletterDetailRepository.save(NewsletterDetail.builder()
                .mainPageUrl("url2")
                .subscribeUrl("sub2")
                .issueCycle("weekly")
                .sender("sender2")
                .build());

        // Newsletter 2: RECENT_ONLY
        Newsletter n2 = newsletterRepository.save(Newsletter.builder()
                .name("n2")
                .description("desc")
                .imageUrl("img")
                .email("email2")
                .detailId(detail2.getId())
                .categoryId(category.getId())
                .build());
        newsletterPreviousPolicyRepository.save(me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy
                .builder()
                .newsletterId(n2.getId())
                .strategy(me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.RECENT_ONLY)
                .fixedCount(0)
                .recentCount(5)
                .exposureRatio(0)
                .build());

        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(n1.getId()));
        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(n2.getId()));

        GetNewslettersRequest request = new GetNewslettersRequest(null, null,
                me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.RECENT_ONLY, null);

        // when
        List<GetNewsletterSummaryResponse> result = newsletterService.getNewsletters(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(1);
            softly.assertThat(result.getFirst().name()).isEqualTo("n2");
            softly.assertThat(result.getFirst().previousStrategy()).isEqualTo("RECENT_ONLY");
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
            softly.assertThat(response.subscriptionCount()).isEqualTo(100);
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터 조회 시 예외가 발생한다.")
    void getNewsletter_Detail_notFound() {
        // when & then
        assertThatThrownBy(() -> newsletterService.getNewsletterDetail(999L))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    @DisplayName("뉴스레터 정보를 수정한다.")
    void updateNewsletter() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        Category newCategory = categoryRepository.save(Category.builder().name("경제").build());

        NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                .mainPageUrl("old.com")
                .subscribeUrl("old-sub.com")
                .issueCycle("weekly")
                .sender("sender")
                .previousAllowed(true)
                .build());

        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                .name("Old Name")
                .description("Old Desc")
                .imageUrl("old.img")
                .email("old@email.com")
                .detailId(detail.getId())
                .categoryId(category.getId())
                .build());
        newsletterPreviousPolicyRepository.save(me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy
                .builder()
                .newsletterId(newsletter.getId())
                .strategy(me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.INACTIVE)
                .fixedCount(0)
                .recentCount(0)
                .exposureRatio(0)
                .build());

        UpdateNewsletterRequest request = new UpdateNewsletterRequest(
                "New Name",
                "New Desc",
                "new.img",
                "new@email.com",
                "경제",
                "new.com",
                "new-sub.com",
                "monthly",
                "new sender",
                "new-prev.com",
                false,
                "kakao",
                me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.FIXED_ONLY,
                10,
                null,
                null);

        // when
        newsletterService.update(newsletter.getId(), request);

        // then
        Newsletter updatedNewsletter = newsletterRepository.findById(newsletter.getId()).orElseThrow();
        NewsletterDetail updatedDetail = newsletterDetailRepository.findById(detail.getId()).orElseThrow();
        me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy updatedPolicy = newsletterPreviousPolicyRepository
                .findByNewsletterId(newsletter.getId()).orElseThrow();

        assertSoftly(softly -> {
            softly.assertThat(updatedNewsletter.getName()).isEqualTo("New Name");
            softly.assertThat(updatedNewsletter.getDescription()).isEqualTo("New Desc");
            softly.assertThat(updatedNewsletter.getCategoryId()).isEqualTo(newCategory.getId());
            softly.assertThat(updatedDetail.getMainPageUrl()).isEqualTo("new.com");
            softly.assertThat(updatedDetail.getSubscribeMethod()).isEqualTo("kakao");
            softly.assertThat(updatedPolicy.getStrategy()).isEqualTo(
                    me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy.FIXED_ONLY);
            softly.assertThat(updatedPolicy.getFixedCount()).isEqualTo(10);
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터 수정 시 예외가 발생한다.")
    void updateNewsletter_notFound() {
        // given
        UpdateNewsletterRequest request = new UpdateNewsletterRequest(
                "New Name", null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null);

        // when & then
        assertThatThrownBy(() -> newsletterService.update(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("뉴스레터를 삭제한다.")
    void deleteNewsletter() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                .mainPageUrl("url")
                .subscribeUrl("sub")
                .issueCycle("weekly")
                .sender("sender")
                .build());

        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                .name("Delete Me")
                .description("desc")
                .imageUrl("img")
                .email("email")
                .detailId(detail.getId())
                .categoryId(category.getId())
                .build());

        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(newsletter.getId()));

        // when
        newsletterService.delete(newsletter.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(newsletterRepository.existsById(newsletter.getId())).isFalse();
            softly.assertThat(newsletterDetailRepository.existsById(detail.getId())).isFalse();
            softly.assertThat(newsletterSubscriptionCountRepository.findByNewsletterId(newsletter.getId()))
                    .isEmpty();
        });
    }

    @Test
    @DisplayName("뉴스레터 상태를 SUSPENDED로 변경하면 status와 suspendedAt이 설정된다.")
    void updateStatus_toSuspended() {
        // given
        Newsletter newsletter = saveMinimalNewsletter();
        LocalDate suspendedAt = LocalDate.of(2025, 11, 1);
        UpdateNewsletterStatusRequest request = new UpdateNewsletterStatusRequest(
                NewsletterPublicationStatus.SUSPENDED, suspendedAt);

        // when
        newsletterService.updateStatus(newsletter.getId(), request);

        // then
        Newsletter updated = newsletterRepository.findById(newsletter.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getStatus()).isEqualTo(NewsletterPublicationStatus.SUSPENDED);
            softly.assertThat(updated.getSuspendedAt()).isEqualTo(suspendedAt);
        });
    }

    @Test
    @DisplayName("SUSPENDED 변경 시 suspendedAt 미입력이면 오늘 날짜로 설정된다.")
    void updateStatus_toSuspended_nullSuspendedAtDefaultsToToday() {
        // given
        Newsletter newsletter = saveMinimalNewsletter();
        UpdateNewsletterStatusRequest request = new UpdateNewsletterStatusRequest(
                NewsletterPublicationStatus.SUSPENDED, null);

        // when
        newsletterService.updateStatus(newsletter.getId(), request);

        // then
        Newsletter updated = newsletterRepository.findById(newsletter.getId()).orElseThrow();
        assertThat(updated.getSuspendedAt()).isEqualTo(LocalDate.now());
    }

    @Test
    @DisplayName("뉴스레터 상태를 ACTIVE로 변경하면 suspendedAt이 null로 초기화된다.")
    void updateStatus_toActive_clearsSuspendedAt() {
        // given
        Newsletter newsletter = newsletterRepository.save(Newsletter.builder()
                .name("test").description("desc").imageUrl("img").email("email@test.com")
                .detailId(0L).categoryId(0L)
                .status(NewsletterPublicationStatus.SUSPENDED)
                .suspendedAt(LocalDate.of(2025, 1, 1))
                .build());
        UpdateNewsletterStatusRequest request = new UpdateNewsletterStatusRequest(
                NewsletterPublicationStatus.ACTIVE, null);

        // when
        newsletterService.updateStatus(newsletter.getId(), request);

        // then
        Newsletter updated = newsletterRepository.findById(newsletter.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getStatus()).isEqualTo(NewsletterPublicationStatus.ACTIVE);
            softly.assertThat(updated.getSuspendedAt()).isNull();
        });
    }

    @Test
    @DisplayName("뉴스레터 상태를 DISCONTINUED로 변경하면 suspendedAt이 설정된다.")
    void updateStatus_toDiscontinued() {
        // given
        Newsletter newsletter = saveMinimalNewsletter();
        LocalDate discontinuedAt = LocalDate.of(2025, 6, 1);
        UpdateNewsletterStatusRequest request = new UpdateNewsletterStatusRequest(
                NewsletterPublicationStatus.DISCONTINUED, discontinuedAt);

        // when
        newsletterService.updateStatus(newsletter.getId(), request);

        // then
        Newsletter updated = newsletterRepository.findById(newsletter.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getStatus()).isEqualTo(NewsletterPublicationStatus.DISCONTINUED);
            softly.assertThat(updated.getSuspendedAt()).isEqualTo(discontinuedAt);
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터 상태 변경 시 예외가 발생한다.")
    void updateStatus_notFound() {
        // given
        UpdateNewsletterStatusRequest request = new UpdateNewsletterStatusRequest(
                NewsletterPublicationStatus.ACTIVE, null);

        // when & then
        assertThatThrownBy(() -> newsletterService.updateStatus(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("발행중 뉴스레터 조회 시 status는 ACTIVE이다.")
    void getNewsletterDetail_statusIsActive() {
        // given
        Newsletter newsletter = createNewsletterWithStatus(NewsletterPublicationStatus.ACTIVE, null);

        // when
        GetNewsletterResponse response = newsletterService.getNewsletterDetail(newsletter.getId());

        // then
        assertThat(response.status()).isEqualTo("ACTIVE");
    }

    @Test
    @DisplayName("휴재 뉴스레터의 suspendedAt이 6개월 이내이면 status는 SUSPENDED_VISIBLE이다.")
    void getNewsletterDetail_statusIsSuspendedVisible() {
        // given
        LocalDate recentSuspendedAt = LocalDate.now().minusMonths(3);
        Newsletter newsletter = createNewsletterWithStatus(NewsletterPublicationStatus.SUSPENDED, recentSuspendedAt);

        // when
        GetNewsletterResponse response = newsletterService.getNewsletterDetail(newsletter.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.status()).isEqualTo("SUSPENDED_VISIBLE");
            softly.assertThat(response.suspendedAt()).isEqualTo(recentSuspendedAt);
        });
    }

    @Test
    @DisplayName("휴재 뉴스레터의 suspendedAt이 6개월 초과이면 status는 SUSPENDED_HIDDEN이다.")
    void getNewsletterDetail_statusIsSuspendedHidden() {
        // given
        LocalDate oldSuspendedAt = LocalDate.now().minusMonths(9);
        Newsletter newsletter = createNewsletterWithStatus(NewsletterPublicationStatus.SUSPENDED, oldSuspendedAt);

        // when
        GetNewsletterResponse response = newsletterService.getNewsletterDetail(newsletter.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.status()).isEqualTo("SUSPENDED_HIDDEN");
            softly.assertThat(response.suspendedAt()).isEqualTo(oldSuspendedAt);
        });
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터 삭제 시 예외가 발생한다.")
    void deleteNewsletter_notFound() {
        // when & then
        assertThatThrownBy(() -> newsletterService.delete(0L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    private Newsletter saveMinimalNewsletter() {
        return newsletterRepository.save(Newsletter.builder()
                .name("test").description("desc").imageUrl("img").email("email@test.com")
                .detailId(0L).categoryId(0L)
                .build());
    }

    private Newsletter createNewsletterWithStatus(NewsletterPublicationStatus status, LocalDate suspendedAt) {
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        NewsletterDetail detail = newsletterDetailRepository.save(NewsletterDetail.builder()
                .mainPageUrl("url").subscribeUrl("sub").issueCycle("weekly").sender("sender")
                .build());
        return newsletterRepository.save(Newsletter.builder()
                .name("test").description("desc").imageUrl("img").email("email@test.com")
                .detailId(detail.getId()).categoryId(category.getId())
                .status(status).suspendedAt(suspendedAt)
                .build());
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
