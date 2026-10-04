package me.bombom.api.v1.newsletterrequest.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletterrequest.config.NewsletterRequestCollectProperties;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.dto.response.CollectTarget;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestDraftRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 수집 상태를 DB에 반영한다. 외부 호출(페이지 수집, LLM, S3)은 트랜잭션 밖의 NewsletterRequestCollector가 맡는다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NewsletterRequestCollectService {

    private final Clock clock;
    private final NewsletterRequestCollectProperties properties;
    private final NewsletterRequestRepository newsletterRequestRepository;
    private final NewsletterRequestDraftRepository newsletterRequestDraftRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 수집할 초안을 선점한다. 다른 서버가 먼저 선점한 초안은 건너뛴다.
     */
    public List<CollectTarget> claimTargets() {
        LocalDateTime now = LocalDateTime.now(clock);
        LocalDateTime staleBefore = now.minusMinutes(properties.getStaleMinutes());
        List<Long> candidateIds = newsletterRequestDraftRepository.findCollectTargetIds(
                DraftCollectStatus.PENDING,
                DraftCollectStatus.COLLECTING,
                staleBefore,
                PageRequest.of(0, properties.getBatchSize())
        );
        return candidateIds.stream()
                .filter(draftId -> claim(draftId, now, staleBefore))
                .map(this::toCollectTarget)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> getCategoryNames() {
        return categoryRepository.findAll()
                .stream()
                .map(Category::getName)
                .toList();
    }

    public void complete(
            Long draftId,
            DraftContent content
    ) {
        NewsletterRequestDraft draft = findDraft(draftId);
        draft.applyCollected(content);
        findNewsletterRequest(draft.getNewsletterRequestId()).markReviewing();
    }

    public void fail(Long draftId, String reason) {
        NewsletterRequestDraft draft = findDraft(draftId);
        draft.recordCollectFailure(reason, properties.getMaxAttemptCount());
        if (draft.isCollectFinished()) {
            findNewsletterRequest(draft.getNewsletterRequestId()).markReviewing();
        }
    }

    @Transactional(readOnly = true)
    public Long findCategoryId(String categoryName) {
        if (categoryName == null) {
            return null;
        }
        return categoryRepository.findByName(categoryName)
                .map(Category::getId)
                .orElse(null);
    }

    private boolean claim(
            Long draftId,
            LocalDateTime now,
            LocalDateTime staleBefore
    ) {
        int updatedCount = newsletterRequestDraftRepository.claim(
                draftId,
                DraftCollectStatus.PENDING,
                DraftCollectStatus.COLLECTING,
                now,
                staleBefore
        );
        return updatedCount == 1;
    }

    private CollectTarget toCollectTarget(Long draftId) {
        NewsletterRequestDraft draft = findDraft(draftId);
        NewsletterRequest newsletterRequest = findNewsletterRequest(draft.getNewsletterRequestId());
        return CollectTarget.of(draft, newsletterRequest);
    }

    private NewsletterRequestDraft findDraft(Long draftId) {
        return newsletterRequestDraftRepository.findById(draftId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequestDraft")
                        .addContext("draftId", draftId));
    }

    private NewsletterRequest findNewsletterRequest(Long newsletterRequestId) {
        return newsletterRequestRepository.findById(newsletterRequestId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                        .addContext("newsletterRequestId", newsletterRequestId));
    }
}
