package me.bombom.api.v1.article.repository;

import me.bombom.api.v1.article.domain.PreviousArticle;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PreviousArticleRepository extends JpaRepository<PreviousArticle, Long> {

    List<PreviousArticle> findByNewsletterIdOrderByArrivedDateTimeDesc(Long newsletterId);
}
