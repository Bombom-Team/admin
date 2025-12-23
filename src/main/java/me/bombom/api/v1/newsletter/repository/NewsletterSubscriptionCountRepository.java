package me.bombom.api.v1.newsletter.repository;

import java.util.Optional;
import me.bombom.api.v1.subscribe.domain.NewsletterSubscriptionCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterSubscriptionCountRepository extends JpaRepository<NewsletterSubscriptionCount, Long> {

    void deleteByNewsletterId(Long newsletterId);

    Optional<NewsletterSubscriptionCount> findByNewsletterId(Long newsletterId);
}
