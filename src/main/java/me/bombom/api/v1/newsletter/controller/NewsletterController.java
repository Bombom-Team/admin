package me.bombom.api.v1.newsletter.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.springframework.http.HttpStatus;
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
}
