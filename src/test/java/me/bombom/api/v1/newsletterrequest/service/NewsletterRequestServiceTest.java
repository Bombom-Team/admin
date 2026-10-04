package me.bombom.api.v1.newsletterrequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.request.RejectNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.request.UpdateNewsletterRequestDraftRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.ApproveNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestDetailResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSummaryResponse;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestDraftRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({NewsletterRequestService.class, NewsletterService.class, QuerydslConfig.class, TimeConfig.class})
class NewsletterRequestServiceTest {

    @Autowired
    private NewsletterRequestService newsletterRequestService;

    @Autowired
    private NewsletterRequestRepository newsletterRequestRepository;

    @Autowired
    private NewsletterRequestDraftRepository newsletterRequestDraftRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private NewsletterRepository newsletterRepository;

    @Test
    void 초안이_완성된_신청을_승인하면_뉴스레터를_만들고_신청을_등록_완료로_바꾼다() {
        // given
        Category category = categoryRepository.save(Category.builder().name("IT/테크").build());
        NewsletterRequest newsletterRequest = saveRequest("weeklydev.stibee.com", NewsletterRequestStatus.REVIEWING);
        saveCompletedDraft(newsletterRequest.getId(), category.getId(), "news@weeklydev.com");

        // when
        ApproveNewsletterRequestResponse response = newsletterRequestService.approve(newsletterRequest.getId());

        // then
        Newsletter newsletter = newsletterRepository.findById(response.newsletterId()).orElseThrow();
        NewsletterRequest approved = newsletterRequestRepository.findById(newsletterRequest.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(newsletter.getName()).isEqualTo("주간 개발 노트");
            softly.assertThat(newsletter.getEmail()).isEqualTo("news@weeklydev.com");
            softly.assertThat(newsletter.getCategoryId()).isEqualTo(category.getId());
            softly.assertThat(approved.getStatus()).isEqualTo(NewsletterRequestStatus.APPROVED);
            softly.assertThat(approved.getNewsletterId()).isEqualTo(response.newsletterId());
        });
    }

    @Test
    void 발신_이메일이_비어_있으면_승인할_수_없다() {
        // given
        Category category = categoryRepository.save(Category.builder().name("IT/테크").build());
        NewsletterRequest newsletterRequest = saveRequest("weeklydev.stibee.com", NewsletterRequestStatus.REVIEWING);
        saveCompletedDraft(newsletterRequest.getId(), category.getId(), null);

        // when & then
        assertThatThrownBy(() -> newsletterRequestService.approve(newsletterRequest.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    void 이미_반려된_신청은_승인할_수_없다() {
        // given
        NewsletterRequest newsletterRequest = saveRequest("rejected.com", NewsletterRequestStatus.REJECTED);
        newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequest.getId())
                .build());

        // when & then
        assertThatThrownBy(() -> newsletterRequestService.approve(newsletterRequest.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.RESOURCE_CONFLICT);
    }

    @Test
    void 신청을_반려하면_반려_사유를_남긴다() {
        // given
        NewsletterRequest newsletterRequest = saveRequest("ad.com", NewsletterRequestStatus.REVIEWING);

        // when
        newsletterRequestService.reject(newsletterRequest.getId(), new RejectNewsletterRequestRequest(" 광고성 메일 "));

        // then
        NewsletterRequest rejected = newsletterRequestRepository.findById(newsletterRequest.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(rejected.getStatus()).isEqualTo(NewsletterRequestStatus.REJECTED);
            softly.assertThat(rejected.getRejectReason()).isEqualTo("광고성 메일");
        });
    }

    @Test
    void 초안_수정은_보낸_필드만_바꾼다() {
        // given
        Category category = categoryRepository.save(Category.builder().name("IT/테크").build());
        NewsletterRequest newsletterRequest = saveRequest("weeklydev.stibee.com", NewsletterRequestStatus.REVIEWING);
        saveCompletedDraft(newsletterRequest.getId(), category.getId(), null);
        UpdateNewsletterRequestDraftRequest request = new UpdateNewsletterRequestDraftRequest(
                null, null, null, "news@weeklydev.com", null, null, null, null, null, null, null
        );

        // when
        newsletterRequestService.updateDraft(newsletterRequest.getId(), request);

        // then
        NewsletterRequestDraft draft = newsletterRequestDraftRepository.findByNewsletterRequestId(newsletterRequest.getId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(draft.getEmail()).isEqualTo("news@weeklydev.com");
            softly.assertThat(draft.getName()).isEqualTo("주간 개발 노트");
            softly.assertThat(draft.findMissingRequiredFields()).isEmpty();
        });
    }

    @Test
    void 존재하지_않는_카테고리로_초안을_수정할_수_없다() {
        // given
        NewsletterRequest newsletterRequest = saveRequest("weeklydev.stibee.com", NewsletterRequestStatus.REVIEWING);
        newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequest.getId())
                .build());
        UpdateNewsletterRequestDraftRequest request = new UpdateNewsletterRequestDraftRequest(
                null, null, null, null, 999L, null, null, null, null, null, null
        );

        // when & then
        assertThatThrownBy(() -> newsletterRequestService.updateDraft(newsletterRequest.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 재수집하면_초안을_수집_대기로_되돌린다() {
        // given
        NewsletterRequest newsletterRequest = saveRequest("weeklydev.stibee.com", NewsletterRequestStatus.REVIEWING);
        newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequest.getId())
                .collectStatus(DraftCollectStatus.FAILED)
                .collectAttemptCount(3)
                .build());

        // when
        newsletterRequestService.recollect(newsletterRequest.getId());

        // then
        NewsletterRequestDraft draft = newsletterRequestDraftRepository.findByNewsletterRequestId(newsletterRequest.getId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(draft.getCollectStatus()).isEqualTo(DraftCollectStatus.PENDING);
            softly.assertThat(draft.getCollectAttemptCount()).isZero();
        });
    }

    @Test
    void 상태로_신청_목록을_거르고_최신_신청부터_보여준다() {
        // given
        NewsletterRequest first = saveRequest("first.com", NewsletterRequestStatus.REVIEWING);
        saveRequest("rejected.com", NewsletterRequestStatus.REJECTED);
        NewsletterRequest third = saveRequest("third.com", NewsletterRequestStatus.REVIEWING);

        // when
        List<NewsletterRequestSummaryResponse> responses =
                newsletterRequestService.getNewsletterRequests(NewsletterRequestStatus.REVIEWING);

        // then
        assertThat(responses).extracting(NewsletterRequestSummaryResponse::id)
                .containsExactly(third.getId(), first.getId());
    }

    @Test
    void 상세_조회는_신청자의_추천_이유와_초안을_함께_보여준다() {
        // given
        NewsletterRequest newsletterRequest = newsletterRequestRepository.save(NewsletterRequest.builder()
                .requestedName("주간 개발 노트")
                .requestedUrl("https://weeklydev.stibee.com")
                .normalizedUrl("weeklydev.stibee.com")
                .requesterMemberId(1L)
                .reason("출근길에 읽기 좋아요")
                .likeCount(3)
                .build());
        newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequest.getId())
                .build());

        // when
        NewsletterRequestDetailResponse response = newsletterRequestService.getNewsletterRequest(newsletterRequest.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.reason()).isEqualTo("출근길에 읽기 좋아요");
            softly.assertThat(response.likeCount()).isEqualTo(3);
            softly.assertThat(response.draft().collectStatus()).isEqualTo(DraftCollectStatus.PENDING);
            softly.assertThat(response.draft().missingFields()).contains("name", "email", "categoryId");
        });
    }

    private NewsletterRequest saveRequest(String normalizedUrl, NewsletterRequestStatus status) {
        return newsletterRequestRepository.save(NewsletterRequest.builder()
                .requestedName("주간 개발 노트")
                .requestedUrl("https://" + normalizedUrl)
                .normalizedUrl(normalizedUrl)
                .requesterMemberId(1L)
                .status(status)
                .build());
    }

    private void saveCompletedDraft(
            Long newsletterRequestId,
            Long categoryId,
            String email
    ) {
        NewsletterRequestDraft draft = NewsletterRequestDraft.builder()
                .newsletterRequestId(newsletterRequestId)
                .build();
        draft.applyCollected(DraftContent.builder()
                .name("주간 개발 노트")
                .description("매주 월요일 개발 글 5편을 보내드려요.")
                .imageUrl("https://cdn.bombom.news/newsletters/weeklydev.png")
                .categoryId(categoryId)
                .mainPageUrl("https://weeklydev.stibee.com")
                .subscribeUrl("https://weeklydev.stibee.com/subscribe")
                .issueCycle("매주 월요일")
                .sender("주간 개발 노트 편집팀")
                .build());
        draft.update(DraftContent.builder().build(), email);
        newsletterRequestDraftRepository.save(draft);
    }
}
