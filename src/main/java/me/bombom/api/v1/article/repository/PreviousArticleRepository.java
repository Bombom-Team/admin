package me.bombom.api.v1.article.repository;

import java.util.List;
import me.bombom.api.v1.article.domain.PreviousArticle;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PreviousArticleRepository extends JpaRepository<PreviousArticle, Long> {

    List<PreviousArticle> findByNewsletterId(Long newsletterId);
}
