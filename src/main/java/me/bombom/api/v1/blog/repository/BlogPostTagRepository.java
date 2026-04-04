package me.bombom.api.v1.blog.repository;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogPostTagRepository extends JpaRepository<BlogPostTag, Long> {

    void deleteAllByBlogPostId(Long blogPostId);

    List<BlogPostTag> findAllByBlogPostId(Long blogPostId);

    List<BlogPostTag> findAllByBlogPostIdOrderByBlogHashtagId(Long blogPostId);
}
