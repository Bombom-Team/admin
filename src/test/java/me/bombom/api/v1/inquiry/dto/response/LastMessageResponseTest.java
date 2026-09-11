package me.bombom.api.v1.inquiry.dto.response;

import static org.assertj.core.api.Assertions.assertThat;

import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.fixture.InquiryMessageFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LastMessageResponseTest {

    @Test
    @DisplayName("어드민이 보낸 메시지는 adminNickname을 채운다.")
    void 어드민_메시지_변환() {
        // given
        InquiryMessage message = InquiryMessageFixture.createAdminMessage(1L, 100L, "안녕하세요");

        // when
        LastMessageResponse response = LastMessageResponse.of(message, "상추");

        // then
        assertThat(response.content()).isEqualTo("안녕하세요");
        assertThat(response.adminNickname()).isEqualTo("상추");
    }
}
