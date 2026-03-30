package me.bombom.api.v1.newsletter.repository;

import java.util.List;
import me.bombom.api.v1.newsletter.domain.NewsletterGroup;
import me.bombom.api.v1.newsletter.dto.GetNewsletterGroupResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface NewsletterGroupRepository extends JpaRepository<NewsletterGroup, Long> {

    @Query("""
        SELECT new me.bombom.api.v1.newsletter.dto.GetNewsletterGroupResponse(g.id, g.name)
        FROM NewsletterGroup g
        ORDER BY g.id ASC
    """)
    List<GetNewsletterGroupResponse> findAllAsResponse();
}
