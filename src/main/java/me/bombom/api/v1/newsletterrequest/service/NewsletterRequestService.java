package me.bombom.api.v1.newsletterrequest.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.request.RejectNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.request.UpdateNewsletterRequestDraftRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.ApproveNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestDetailResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestDraftResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSummaryResponse;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestDraftRepository;
import me.bombom.api.v1.newsletterrequest.repository.NewsletterRequestRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterRequestService {

    private final NewsletterRequestRepository newsletterRequestRepository;
    private final NewsletterRequestDraftRepository newsletterRequestDraftRepository;
    private final CategoryRepository categoryRepository;
    private final NewsletterService newsletterService;

    public List<NewsletterRequestSummaryResponse> getNewsletterRequests(NewsletterRequestStatus status) {
        List<NewsletterRequest> newsletterRequests = findNewsletterRequests(status);
        Map<Long, NewsletterRequestDraft> draftsByRequestId = newsletterRequestDraftRepository
                .findAllByNewsletterRequestIdIn(newsletterRequests.stream().map(NewsletterRequest::getId).toList())
                .stream()
                .collect(Collectors.toMap(NewsletterRequestDraft::getNewsletterRequestId, Function.identity()));
        return newsletterRequests.stream()
                .map(newsletterRequest -> NewsletterRequestSummaryResponse.of(
                        newsletterRequest,
                        draftsByRequestId.get(newsletterRequest.getId())
                ))
                .toList();
    }

    public NewsletterRequestDetailResponse getNewsletterRequest(Long id) {
        return NewsletterRequestDetailResponse.of(
                findNewsletterRequest(id),
                NewsletterRequestDraftResponse.from(findDraft(id))
        );
    }

    @Transactional
    public void updateDraft(Long id, UpdateNewsletterRequestDraftRequest request) {
        if (request.categoryId() != null) {
            findCategory(request.categoryId());
        }
        findDraft(id).update(request.toDraftContent(), request.email());
    }

    @Transactional
    public void recollect(Long id) {
        validateInProgress(findNewsletterRequest(id));
        findDraft(id).resetToPending();
    }

    /**
     * 초안 값으로 뉴스레터를 만들고 신청을 등록 완료로 바꾼다. 기존 뉴스레터 생성 로직을 그대로 쓴다.
     */
    @Transactional
    public ApproveNewsletterRequestResponse approve(Long id) {
        NewsletterRequest newsletterRequest = findNewsletterRequest(id);
        validateInProgress(newsletterRequest);
        NewsletterRequestDraft draft = findDraft(id);
        validateRequiredFields(draft);

        Long newsletterId = newsletterService.create(toCreateNewsletterRequest(draft));
        newsletterRequest.approve(newsletterId);
        return new ApproveNewsletterRequestResponse(newsletterId);
    }

    @Transactional
    public void reject(Long id, RejectNewsletterRequestRequest request) {
        findNewsletterRequest(id).reject(request.reason().strip());
    }

    private List<NewsletterRequest> findNewsletterRequests(NewsletterRequestStatus status) {
        if (status == null) {
            return newsletterRequestRepository.findAllByOrderByIdDesc();
        }
        return newsletterRequestRepository.findAllByStatusOrderByIdDesc(status);
    }

    private CreateNewsletterRequest toCreateNewsletterRequest(NewsletterRequestDraft draft) {
        return new CreateNewsletterRequest(
                draft.getName(),
                draft.getDescription(),
                draft.getImageUrl(),
                draft.getEmail(),
                findCategory(draft.getCategoryId()).getName(),
                draft.getMainPageUrl(),
                draft.getSubscribeUrl(),
                draft.getIssueCycle(),
                draft.getSender(),
                draft.getPreviousNewsletterUrl(),
                draft.getSubscribeMethod(),
                NewsletterPreviousStrategy.INACTIVE,
                0,
                0,
                0
        );
    }

    private void validateInProgress(NewsletterRequest newsletterRequest) {
        if (newsletterRequest.getStatus().isClosed()) {
            throw new CIllegalArgumentException(ErrorDetail.RESOURCE_CONFLICT)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                    .addContext("newsletterRequestId", newsletterRequest.getId())
                    .addContext("status", newsletterRequest.getStatus());
        }
    }

    private void validateRequiredFields(NewsletterRequestDraft draft) {
        List<String> missingFields = draft.findMissingRequiredFields();
        if (missingFields.isEmpty()) {
            return;
        }
        throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequestDraft")
                .addContext("missingFields", missingFields);
    }

    private NewsletterRequest findNewsletterRequest(Long id) {
        return newsletterRequestRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequest")
                        .addContext("newsletterRequestId", id));
    }

    private NewsletterRequestDraft findDraft(Long newsletterRequestId) {
        return newsletterRequestDraftRepository.findByNewsletterRequestId(newsletterRequestId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletterRequestDraft")
                        .addContext("newsletterRequestId", newsletterRequestId));
    }

    private Category findCategory(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "category")
                        .addContext("categoryId", categoryId));
    }
}
