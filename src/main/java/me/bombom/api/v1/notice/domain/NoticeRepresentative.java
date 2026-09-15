package me.bombom.api.v1.notice.domain;

import me.bombom.api.v1.common.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 대표 공지는 전체에서 최대 1건이므로 id를 1로 고정한 단일 행으로 관리
 * 행 없음 == 대표 공지가 없는 상태
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NoticeRepresentative extends BaseEntity {

    public static final byte SINGLETON_ID = 1;

    @Id
    private Byte id;

    @Column(nullable = false)
    private Long noticeId;

    private NoticeRepresentative(Long noticeId) {
        this.id = SINGLETON_ID;
        this.noticeId = noticeId;
    }

    public static NoticeRepresentative of(Long noticeId) {
        return new NoticeRepresentative(noticeId);
    }

    public void changeTo(Long noticeId) {
        this.noticeId = noticeId;
    }
}
