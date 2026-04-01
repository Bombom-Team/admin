package me.bombom.api.v1.blog.repository;

import java.util.List;
import me.bombom.api.v1.blog.domain.BlogPost;
import me.bombom.api.v1.blog.domain.BlogPostStatus;
import me.bombom.api.v1.blog.dto.BlogDraftListItemResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    @Query("""
            select new me.bombom.api.v1.blog.dto.BlogDraftListItemResponse(
                blogPost.id,
                blogPost.title,
                blogPost.updatedAt
            )
            from BlogPost blogPost
            where blogPost.memberId = :memberId
                and blogPost.status = :status
            order by blogPost.updatedAt desc
            """)
    List<BlogDraftListItemResponse> findAllDraftListItemByMemberIdAndStatus(
            Long memberId,
            BlogPostStatus status
    );
}
