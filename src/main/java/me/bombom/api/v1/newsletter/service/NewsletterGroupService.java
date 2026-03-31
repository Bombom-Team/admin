package me.bombom.api.v1.newsletter.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.GetNewsletterGroupResponse;
import me.bombom.api.v1.newsletter.repository.NewsletterGroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterGroupService {

    private final NewsletterGroupRepository newsletterGroupRepository;

    public List<GetNewsletterGroupResponse> getNewsletterGroups() {
        return newsletterGroupRepository.findAllAsResponse();
    }
}
