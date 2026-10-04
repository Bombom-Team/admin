package me.bombom.api.v1.newsletterrequest.repository;

import java.util.List;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NewsletterRequestRepository extends JpaRepository<NewsletterRequest, Long> {

    List<NewsletterRequest> findAllByOrderByIdDesc();

    List<NewsletterRequest> findAllByStatusOrderByIdDesc(NewsletterRequestStatus status);
}
