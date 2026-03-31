package me.bombom.api.v1.newsletter.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.GetNewsletterGroupResponse;
import me.bombom.api.v1.newsletter.service.NewsletterGroupService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/newsletter-groups")
public class NewsletterGroupController implements NewsletterGroupControllerApi {

    private final NewsletterGroupService newsletterGroupService;

    @Override
    @GetMapping
    public List<GetNewsletterGroupResponse> getNewsletterGroups() {
        return newsletterGroupService.getNewsletterGroups();
    }
}
