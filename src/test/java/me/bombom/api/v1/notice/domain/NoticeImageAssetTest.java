package me.bombom.api.v1.notice.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NoticeImageAssetTest {

    @Test
    @DisplayName("업로드된 이미지를 참조 상태로 전환하면 삭제 요청 시각이 비워진다.")
    void attach_업로드_이미지를_참조_상태로_전환한다() {
        // given
        NoticeImageAsset noticeImageAsset = createNoticeImageAsset(
                NoticeImageAssetStatus.DELETE_PENDING,
                LocalDateTime.of(2026, 9, 5, 10, 0)
        );

        // when
        noticeImageAsset.attach();

        // then
        assertSoftly(softly -> {
            softly.assertThat(noticeImageAsset.getStatus()).isEqualTo(NoticeImageAssetStatus.ATTACHED);
            softly.assertThat(noticeImageAsset.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    @DisplayName("참조가 끊긴 이미지를 삭제 대기 상태로 전환하면 삭제 요청 시각이 기록된다.")
    void markDeletePending_삭제_대기_상태로_전환하고_시각을_기록한다() {
        // given
        NoticeImageAsset noticeImageAsset = createNoticeImageAsset(NoticeImageAssetStatus.ATTACHED, null);
        LocalDateTime deleteRequestedAt = LocalDateTime.of(2026, 9, 5, 12, 30);

        // when
        noticeImageAsset.markDeletePending(deleteRequestedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(noticeImageAsset.getStatus()).isEqualTo(NoticeImageAssetStatus.DELETE_PENDING);
            softly.assertThat(noticeImageAsset.getDeleteRequestedAt()).isEqualTo(deleteRequestedAt);
        });
    }

    private NoticeImageAsset createNoticeImageAsset(
            NoticeImageAssetStatus status,
            LocalDateTime deleteRequestedAt
    ) {
        return NoticeImageAsset.builder()
                .noticeId(1L)
                .objectKey("notices/202609/image.png")
                .imageUrl("https://cdn.bombom.me/notices/202609/image.png")
                .status(status)
                .deleteRequestedAt(deleteRequestedAt)
                .build();
    }
}
