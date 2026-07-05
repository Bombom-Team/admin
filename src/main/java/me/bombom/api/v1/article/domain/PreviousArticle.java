package me.bombom.api.v1.article.domain;

import me.bombom.api.v1.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PreviousArticle extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "mediumtext")
    private String contents;

    @Column(columnDefinition = "tinyint")
    private int expectedReadTime;

    @Column(nullable = false)
    private String contentsSummary;

    @Column(nullable = false)
    private Long newsletterId;

    @Column(nullable = false)
    private LocalDateTime arrivedDateTime;

    @Column(nullable = false)
    private boolean isFixed;

    @Builder
    public PreviousArticle (
            Long id,
            @NonNull String title,
            @NonNull String contents,
            int expectedReadTime,
            @NonNull String contentsSummary,
            @NonNull Long newsletterId,
            @NonNull LocalDateTime arrivedDateTime,
            boolean isFixed
    ) {
        this.id = id;
        this.title = title;
        this.contents = contents;
        this.expectedReadTime = expectedReadTime;
        this.contentsSummary = contentsSummary;
        this.newsletterId = newsletterId;
        this.arrivedDateTime = arrivedDateTime;
        this.isFixed = isFixed;
    }

    public void update(
            String title,
            String contents,
            String contentsSummary,
            int expectedReadTime,
            LocalDateTime arrivedDateTime,
            Boolean isFixed
    ) {
        if (title != null) this.title = title;
        if (contents != null) {
            this.contents = contents;
            this.contentsSummary = contentsSummary;
            this.expectedReadTime = expectedReadTime;
        }
        if (arrivedDateTime != null) this.arrivedDateTime = arrivedDateTime;
        if (isFixed != null) this.isFixed = isFixed;
    }
}
