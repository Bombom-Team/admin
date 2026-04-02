package me.bombom.api.v1.blog.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogImageAsset;
import me.bombom.api.v1.blog.domain.BlogImageAssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlogImageAssetRepository extends JpaRepository<BlogImageAsset, Long> {

    List<BlogImageAsset> findAllByBlogPostId(Long blogPostId);

    List<BlogImageAsset> findAllByBlogPostIdAndStatusOrderById(
            Long blogPostId,
            BlogImageAssetStatus status
    );

    List<BlogImageAsset> findAllByIdIn(Collection<Long> ids);
}
