package me.bombom.api.v1.blog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import me.bombom.api.v1.common.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        uniqueConstraints = @UniqueConstraint(
                name = "uk_blog_image_asset_object_key",
                columnNames = "object_key"
        )
)
public class BlogImageAsset extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long blogPostId;

    @Column(nullable = false, length = 500)
    private String objectKey;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private BlogImageAssetStatus status;

    private LocalDateTime deleteRequestedAt;

    @Builder
    public BlogImageAsset(
            Long id,
            @NonNull Long blogPostId,
            @NonNull String objectKey,
            @NonNull String imageUrl,
            @NonNull BlogImageAssetStatus status,
            LocalDateTime deleteRequestedAt
    ) {
        this.id = id;
        this.blogPostId = blogPostId;
        this.objectKey = objectKey;
        this.imageUrl = imageUrl;
        this.status = status;
        this.deleteRequestedAt = deleteRequestedAt;
    }
}
