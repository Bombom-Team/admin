package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {

    void deleteByNewsletterId(Long newsletterId);
}
