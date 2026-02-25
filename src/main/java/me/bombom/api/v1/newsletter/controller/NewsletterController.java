package me.bombom.api.v1.newsletter.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterStatusRequest;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/newsletters")
public class NewsletterController implements NewsletterControllerApi {

    private final NewsletterService newsletterService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createNewsletter(@Valid @RequestBody CreateNewsletterRequest request) {
        newsletterService.create(request);
    }

    @Override
    @GetMapping
    public List<GetNewsletterSummaryResponse> getNewsletters(GetNewslettersRequest request) {
        return newsletterService.getNewsletters(request);
    }

    @Override
    @GetMapping("/{id}")
    public GetNewsletterResponse getNewsletterDetail(@PathVariable Long id) {
        return newsletterService.getNewsletterDetail(id);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateNewsletter(@PathVariable Long id, @RequestBody UpdateNewsletterRequest request) {
        newsletterService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNewsletter(@PathVariable Long id) {
        newsletterService.delete(id);
    }

    @Override
    @PatchMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNewsletterStatusRequest request
    ) {
        newsletterService.updateStatus(id, request);
    }
}
