package me.bombom.api.v1.newsletter.domain;

import me.bombom.api.v1.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(uniqueConstraints = {
        @UniqueConstraint(name = "uk_newsletter_detail_id", columnNames = { "detail_id" })
})
public class Newsletter extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, length = 512)
    private String imageUrl;

    @Column(nullable = false, length = 60)
    private String email;

    @Column(nullable = false)
    private Long categoryId;

    @Column(nullable = false)
    private Long detailId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NewsletterPublicationStatus status = NewsletterPublicationStatus.ACTIVE;

    @Column
    private LocalDate suspendedAt;

    @Builder
    public Newsletter(
            Long id,
            @NonNull String name,
            @NonNull String description,
            @NonNull String imageUrl,
            @NonNull String email,
            @NonNull Long categoryId,
            @NonNull Long detailId,
            NewsletterPublicationStatus status,
            LocalDate suspendedAt
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.email = email;
        this.categoryId = categoryId;
        this.detailId = detailId;
        this.status = status != null ? status : NewsletterPublicationStatus.ACTIVE;
        this.suspendedAt = suspendedAt;
    }

    public void updateStatus(NewsletterPublicationStatus status, LocalDate suspendedAt) {
        this.status = status;
        if (status == NewsletterPublicationStatus.ACTIVE) {
            this.suspendedAt = null;
        } else {
            this.suspendedAt = suspendedAt != null ? suspendedAt : LocalDate.now();
        }
    }

    public void update(String name, String description, String imageUrl, String email, Long categoryId) {
        if (name != null)
            this.name = name;
        if (description != null)
            this.description = description;
        if (imageUrl != null)
            this.imageUrl = imageUrl;
        if (email != null)
            this.email = email;
        if (categoryId != null)
            this.categoryId = categoryId;
    }
}
