package me.bombom.api.v1.notice.domain;

import me.bombom.api.v1.common.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "notice_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_notice_representative_notice"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Notice notice;

    public static NoticeRepresentative of(Notice notice) {
        return new NoticeRepresentative(notice);
    }

    public void changeTo(Notice notice) {
        this.notice = notice;
    }

    private NoticeRepresentative(Notice notice) {
        this.id = SINGLETON_ID;
        this.notice = notice;
    }
}
