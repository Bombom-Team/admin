package me.bombom.api.v1.newsletterrequest.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.newsletterrequest.config.NewsletterRequestCollectProperties;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.response.CollectTarget;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestDraftRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({
        NewsletterRequestCollectService.class,
        NewsletterRequestCollectProperties.class,
        QuerydslConfig.class,
        TimeConfig.class
})
class NewsletterRequestCollectServiceTest {

    @Autowired
    private NewsletterRequestCollectService newsletterRequestCollectService;

    @Autowired
    private NewsletterRequestRepository newsletterRequestRepository;

    @Autowired
    private NewsletterRequestDraftRepository newsletterRequestDraftRepository;

    @Autowired
    private Clock clock;

    @Test
    void 대기_중이거나_오래_멈춘_초안만_선점한다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock);
        NewsletterRequestDraft pending = saveDraft("pending.com", DraftCollectStatus.PENDING, null);
        NewsletterRequestDraft stale = saveDraft("stale.com", DraftCollectStatus.COLLECTING, now.minusMinutes(30));
        saveDraft("collecting.com", DraftCollectStatus.COLLECTING, now.minusMinutes(1));
        saveDraft("success.com", DraftCollectStatus.SUCCESS, null);

        // when
        List<CollectTarget> targets = newsletterRequestCollectService.claimTargets();

        // then
        assertThat(targets).extracting(CollectTarget::draftId)
                .containsExactly(pending.getId(), stale.getId());
    }

    @Test
    void 선점하면_수집_중으로_바꾸고_시도_횟수를_올린다() {
        // given
        NewsletterRequestDraft pending = saveDraft("pending.com", DraftCollectStatus.PENDING, null);

        // when
        newsletterRequestCollectService.claimTargets();

        // then
        NewsletterRequestDraft claimed = newsletterRequestDraftRepository.findById(pending.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(claimed.getCollectStatus()).isEqualTo(DraftCollectStatus.COLLECTING);
            softly.assertThat(claimed.getCollectAttemptCount()).isEqualTo(1);
            softly.assertThat(claimed.getCollectStartedAt()).isNotNull();
        });
    }

    @Test
    void 이미_선점된_초안은_다시_선점하지_않는다() {
        // given
        saveDraft("pending.com", DraftCollectStatus.PENDING, null);
        newsletterRequestCollectService.claimTargets();

        // when
        List<CollectTarget> targets = newsletterRequestCollectService.claimTargets();

        // then
        assertThat(targets).isEmpty();
    }

    @Test
    void 수집을_마치면_초안을_채우고_신청을_확인_중으로_바꾼다() {
        // given
        NewsletterRequestDraft draft = saveDraft("weeklydev.com", DraftCollectStatus.COLLECTING, LocalDateTime.now(clock));

        // when
        newsletterRequestCollectService.complete(draft.getId(), DraftContent.builder()
                .name("주간 개발 노트")
                .issueCycle("매주 월요일")
                .build());

        // then
        NewsletterRequestDraft completed = newsletterRequestDraftRepository.findById(draft.getId()).orElseThrow();
        NewsletterRequest newsletterRequest = newsletterRequestRepository.findById(draft.getNewsletterRequestId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(completed.getCollectStatus()).isEqualTo(DraftCollectStatus.SUCCESS);
            softly.assertThat(completed.getName()).isEqualTo("주간 개발 노트");
            softly.assertThat(newsletterRequest.getStatus()).isEqualTo(NewsletterRequestStatus.REVIEWING);
        });
    }

    @Test
    void 최대_시도_횟수_전에_실패하면_다시_대기로_둔다() {
        // given
        NewsletterRequestDraft draft = saveDraft("weeklydev.com", DraftCollectStatus.PENDING, null);
        newsletterRequestCollectService.claimTargets();

        // when
        newsletterRequestCollectService.fail(draft.getId(), "timeout");

        // then
        NewsletterRequestDraft failed = newsletterRequestDraftRepository.findById(draft.getId()).orElseThrow();
        NewsletterRequest newsletterRequest = newsletterRequestRepository.findById(draft.getNewsletterRequestId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(failed.getCollectStatus()).isEqualTo(DraftCollectStatus.PENDING);
            softly.assertThat(failed.getFailureReason()).isEqualTo("timeout");
            softly.assertThat(newsletterRequest.getStatus()).isEqualTo(NewsletterRequestStatus.RECEIVED);
        });
    }

    @Test
    void 최대_시도_횟수만큼_실패하면_실패로_두고_운영자에게_넘긴다() {
        // given
        NewsletterRequestDraft draft = newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(saveRequest("weeklydev.com").getId())
                .collectStatus(DraftCollectStatus.COLLECTING)
                .collectAttemptCount(3)
                .collectStartedAt(LocalDateTime.now(clock))
                .build());

        // when
        newsletterRequestCollectService.fail(draft.getId(), "timeout");

        // then
        NewsletterRequestDraft failed = newsletterRequestDraftRepository.findById(draft.getId()).orElseThrow();
        NewsletterRequest newsletterRequest = newsletterRequestRepository.findById(draft.getNewsletterRequestId())
                .orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(failed.getCollectStatus()).isEqualTo(DraftCollectStatus.FAILED);
            softly.assertThat(newsletterRequest.getStatus()).isEqualTo(NewsletterRequestStatus.REVIEWING);
        });
    }

    private NewsletterRequestDraft saveDraft(
            String normalizedUrl,
            DraftCollectStatus collectStatus,
            LocalDateTime collectStartedAt
    ) {
        return newsletterRequestDraftRepository.save(NewsletterRequestDraft.builder()
                .newsletterRequestId(saveRequest(normalizedUrl).getId())
                .collectStatus(collectStatus)
                .collectStartedAt(collectStartedAt)
                .build());
    }

    private NewsletterRequest saveRequest(String normalizedUrl) {
        return newsletterRequestRepository.save(NewsletterRequest.builder()
                .requestedName("주간 개발 노트")
                .requestedUrl("https://" + normalizedUrl)
                .normalizedUrl(normalizedUrl)
                .requesterMemberId(1L)
                .build());
    }
}
