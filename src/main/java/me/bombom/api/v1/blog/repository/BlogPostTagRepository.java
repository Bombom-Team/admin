package me.bombom.api.v1.blog.repository;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPostTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface BlogPostTagRepository extends JpaRepository<BlogPostTag, Long> {

    @Modifying(flushAutomatically = true)
    @Query("""
            delete
            from BlogPostTag blogPostTag
            where blogPostTag.blogPostId = :blogPostId
            """)
    void deleteAllByBlogPostId(Long blogPostId);

    List<BlogPostTag> findAllByBlogPostId(Long blogPostId);

    List<BlogPostTag> findAllByBlogPostIdOrderByBlogHashtagId(Long blogPostId);
}
