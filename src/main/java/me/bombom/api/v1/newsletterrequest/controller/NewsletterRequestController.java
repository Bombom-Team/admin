package me.bombom.api.v1.newsletterrequest.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.request.RejectNewsletterRequestRequest;
import me.bombom.api.v1.newsletterrequest.dto.request.UpdateNewsletterRequestDraftRequest;
import me.bombom.api.v1.newsletterrequest.dto.response.ApproveNewsletterRequestResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestDetailResponse;
import me.bombom.api.v1.newsletterrequest.dto.response.NewsletterRequestSummaryResponse;
import me.bombom.api.v1.newsletterrequest.service.NewsletterRequestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/newsletter-requests")
public class NewsletterRequestController implements NewsletterRequestControllerApi {

    private final NewsletterRequestService newsletterRequestService;

    @Override
    @GetMapping
    public List<NewsletterRequestSummaryResponse> getNewsletterRequests(
            @RequestParam(required = false) NewsletterRequestStatus status
    ) {
        return newsletterRequestService.getNewsletterRequests(status);
    }

    @Override
    @GetMapping("/{id}")
    public NewsletterRequestDetailResponse getNewsletterRequest(@PathVariable Long id) {
        return newsletterRequestService.getNewsletterRequest(id);
    }

    @Override
    @PatchMapping("/{id}/draft")
    public void updateDraft(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNewsletterRequestDraftRequest request
    ) {
        newsletterRequestService.updateDraft(id, request);
    }

    @Override
    @PostMapping("/{id}/recollect")
    public void recollect(@PathVariable Long id) {
        newsletterRequestService.recollect(id);
    }

    @Override
    @PostMapping("/{id}/approve")
    public ApproveNewsletterRequestResponse approve(@PathVariable Long id) {
        return newsletterRequestService.approve(id);
    }

    @Override
    @PostMapping("/{id}/reject")
    public void reject(
            @PathVariable Long id,
            @Valid @RequestBody RejectNewsletterRequestRequest request
    ) {
        newsletterRequestService.reject(id, request);
    }
}
