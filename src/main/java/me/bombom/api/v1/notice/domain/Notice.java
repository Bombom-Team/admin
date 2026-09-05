package me.bombom.api.v1.notice.domain;

import me.bombom.api.v1.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Column(columnDefinition = "mediumtext")
    private String content;

    @Enumerated(value = EnumType.STRING)
    private NoticeCategory noticeCategory;

    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private NoticeVisibility visibility;

    @Column(nullable = false)
    private boolean isRepresentative;

    @Builder
    public Notice(
            Long id,
            String title,
            String content,
            NoticeCategory noticeCategory,
            NoticeVisibility visibility,
            Boolean isRepresentative
    ) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.noticeCategory = noticeCategory;
        this.visibility = visibility != null ? visibility : NoticeVisibility.PRIVATE;
        this.isRepresentative = isRepresentative != null && isRepresentative;
    }

    public void update(
            String title,
            String content,
            NoticeCategory noticeCategory,
            NoticeVisibility visibility,
            Boolean isRepresentative
    ) {
        if (title != null) {
            this.title = title;
        }
        if (content != null) {
            this.content = content;
        }
        if (noticeCategory != null) {
            this.noticeCategory = noticeCategory;
        }
        if (visibility != null) {
            this.visibility = visibility;
        }
        if (isRepresentative != null) {
            this.isRepresentative = isRepresentative;
        }
    }

    public void demoteFromRepresentative() {
        this.isRepresentative = false;
    }
}
