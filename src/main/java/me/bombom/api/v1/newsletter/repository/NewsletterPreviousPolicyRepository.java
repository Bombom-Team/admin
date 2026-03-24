package me.bombom.api.v1.newsletter.repository;

import me.bombom.api.v1.newsletter.domain.NewsletterPreviousPolicy;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface NewsletterPreviousPolicyRepository extends JpaRepository<NewsletterPreviousPolicy, Long> {

    void deleteByNewsletterId(Long newsletterId);

    Optional<NewsletterPreviousPolicy> findByNewsletterId(Long newsletterId);
}
