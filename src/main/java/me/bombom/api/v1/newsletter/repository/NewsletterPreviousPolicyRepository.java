package me.bombom.api.v1.newsletter.repository;

import java.util.Optional;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterPreviousPolicyRepository extends JpaRepository<NewsletterPreviousPolicy, Long> {

    void deleteByNewsletterId(Long newsletterId);

    Optional<NewsletterPreviousPolicy> findByNewsletterId(Long newsletterId);
}
