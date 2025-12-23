package me.bombom.api.v1.newsletter.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.domain.NewsletterDetail;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterDetailRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterPreviousPolicyRepository;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NewsletterService {

    private final NewsletterRepository newsletterRepository;
    private final NewsletterDetailRepository newsletterDetailRepository;
    private final NewsletterPreviousPolicyRepository newsletterPreviousPolicyRepository;
    private final NewsletterSubscriptionCountRepository newsletterSubscriptionCountRepository;
    private final CategoryRepository categoryRepository;

    @Transactional
    public void create(CreateNewsletterRequest request) {
        Category category = categoryRepository.findByName(request.category())
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 카테고리입니다."));

        NewsletterDetail newsletterDetail = newsletterDetailRepository.save(request.toDetailEntity());

        Newsletter newsletter = newsletterRepository.save(request.toNewsletterEntity(newsletterDetail.getId(), category.getId()));

        newsletterPreviousPolicyRepository.save(NewsletterPreviousPolicy.builder()
                .newsletterId(newsletter.getId())
                .strategy(NewsletterPreviousStrategy.INACTIVE)
                .fixedCount(0)
                .recentCount(0)
                .exposureRatio(0)
                .build());

        newsletterSubscriptionCountRepository.save(NewsletterSubscriptionCount.from(newsletter.getId()));
    }
}
