package me.bombom.api.v1.newsletterrequest.repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.newsletterrequest.domain.DraftCollectStatus;
import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestDraft;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NewsletterRequestDraftRepository extends JpaRepository<NewsletterRequestDraft, Long> {

    Optional<NewsletterRequestDraft> findByNewsletterRequestId(Long newsletterRequestId);

    List<NewsletterRequestDraft> findAllByNewsletterRequestIdIn(Collection<Long> newsletterRequestIds);

    /**
     * 수집할 초안 후보. 대기 중이거나, 수집 중인데 오래 멈춘 초안을 고른다.
     */
    @Query("""
            SELECT d.id
            FROM NewsletterRequestDraft d
            WHERE d.collectStatus = :pending
               OR (d.collectStatus = :collecting AND d.collectStartedAt < :staleBefore)
            ORDER BY d.id
            """)
    List<Long> findCollectTargetIds(
            @Param("pending") DraftCollectStatus pending,
            @Param("collecting") DraftCollectStatus collecting,
            @Param("staleBefore") LocalDateTime staleBefore,
            Pageable pageable
    );

    /**
     * 초안을 수집 중으로 선점한다. 영향받은 행이 1이면 이 서버가 선점에 성공한 것이다.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            UPDATE NewsletterRequestDraft d
            SET d.collectStatus = :collecting,
                d.collectStartedAt = :now,
                d.collectAttemptCount = d.collectAttemptCount + 1
            WHERE d.id = :id
              AND (d.collectStatus = :pending
                   OR (d.collectStatus = :collecting AND d.collectStartedAt < :staleBefore))
            """)
    int claim(
            @Param("id") Long id,
            @Param("pending") DraftCollectStatus pending,
            @Param("collecting") DraftCollectStatus collecting,
            @Param("now") LocalDateTime now,
            @Param("staleBefore") LocalDateTime staleBefore
    );
}
