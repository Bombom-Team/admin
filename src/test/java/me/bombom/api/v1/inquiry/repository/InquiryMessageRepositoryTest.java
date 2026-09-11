package me.bombom.api.v1.inquiry.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.fixture.InquiryMessageFixture;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(QuerydslConfig.class)
class InquiryMessageRepositoryTest {

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private InquiryMessageRepository inquiryMessageRepository;

    @Test
    @DisplayName("방 ID 목록으로 방마다 최신 메시지 1건씩만 조회한다.")
    void 방별_최신_메시지_조회() {
        // given
        InquiryRoom roomWithMessages = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryRoom roomWithoutMessages = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(2L, 10L));

        inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(roomWithMessages.getId(), 100L, "older"));
        InquiryMessage newer = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(roomWithMessages.getId(), 100L, "newer"));

        // when
        List<InquiryMessage> result = inquiryMessageRepository.findLatestMessagesByRoomIds(
                List.of(roomWithMessages.getId(), roomWithoutMessages.getId()));

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(newer.getId());
        assertThat(result.get(0).getContent()).isEqualTo("newer");
    }

    @Test
    @DisplayName("방 ID 목록이 비어있으면 빈 리스트를 반환한다.")
    void 방_ID_목록_없으면_빈_리스트() {
        // when
        List<InquiryMessage> result = inquiryMessageRepository.findLatestMessagesByRoomIds(List.of());

        // then
        assertThat(result).isEmpty();
    }
}
