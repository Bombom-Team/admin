package me.bombom.api.v1.inquiry.dto.response;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.inquiry.domain.InquirerType;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InquiryRoomResponseTest {

    @Test
    @DisplayName("회원 문의방을 응답으로 변환하면 문의자 정보가 채워진다.")
    void 회원_문의방_변환() {
        // given
        var room = InquiryRoomFixture.createMemberRoom(1L, 10L);
        room.assign(100L);

        // when
        InquiryRoomResponse response = InquiryRoomResponse.of(
                room, "상추", InquirerType.MEMBER, "메이", "may@example.com", null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.assigneeNickname()).isEqualTo("상추");
            softly.assertThat(response.inquirerType()).isEqualTo(InquirerType.MEMBER);
            softly.assertThat(response.guestId()).isNull();
            softly.assertThat(response.inquirerNickname()).isEqualTo("메이");
            softly.assertThat(response.inquirerEmail()).isEqualTo("may@example.com");
            softly.assertThat(response.lastMessage()).isNull();
        });
    }

    @Test
    @DisplayName("게스트 문의방을 응답으로 변환하면 guestId가 채워지고 회원 정보는 null이다.")
    void 게스트_문의방_변환() {
        // given
        var room = InquiryRoomFixture.createGuestRoom("guest-uuid", 10L);

        // when
        InquiryRoomResponse response = InquiryRoomResponse.of(
                room, null, InquirerType.GUEST, null, null, null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.inquirerType()).isEqualTo(InquirerType.GUEST);
            softly.assertThat(response.guestId()).isEqualTo("guest-uuid");
            softly.assertThat(response.inquirerNickname()).isNull();
            softly.assertThat(response.inquirerEmail()).isNull();
        });
    }
}
