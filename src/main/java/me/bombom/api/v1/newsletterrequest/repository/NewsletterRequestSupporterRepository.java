package me.bombom.api.v1.newsletterrequest.repository;

import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestSupporter;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRequestSupporterRepository extends JpaRepository<NewsletterRequestSupporter, Long> {

    List<NewsletterRequestSupporter> findAllByNewsletterRequestIdOrderById(Long newsletterRequestId);
}
