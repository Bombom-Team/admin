package me.bombom.api.v1.inquiry.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.fixture.InquiryMessageFixture;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import(QuerydslConfig.class)
class InquiryRoomRepositoryTest {

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private InquiryMessageRepository inquiryMessageRepository;

    @Test
    @DisplayName("최신 메시지가 있는 방이 먼저, 없는 방은 방 생성일시 기준으로 정렬된다.")
    void 방_목록_최신_메시지_기준_정렬() throws InterruptedException {
        // given
        InquiryRoom roomWithOldMessage = inquiryRoomRepository.save(
                InquiryRoomFixture.createMemberRoom(1L, 10L));
        inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(roomWithOldMessage.getId(), 100L, "old message"));

        Thread.sleep(10);
        InquiryRoom newRoomWithoutMessage = inquiryRoomRepository.save(
                InquiryRoomFixture.createMemberRoom(2L, 10L));

        Thread.sleep(10);
        InquiryRoom roomWithRecentMessage = inquiryRoomRepository.save(
                InquiryRoomFixture.createMemberRoom(3L, 10L));
        inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(roomWithRecentMessage.getId(), 100L, "recent message"));

        GetInquiryRoomsRequest request = new GetInquiryRoomsRequest(null, null, null);

        // when
        Page<InquiryRoom> result = inquiryRoomRepository.findRoomsForAdmin(request, PageRequest.of(0, 10));

        // then
        List<Long> orderedIds = result.getContent().stream().map(InquiryRoom::getId).toList();
        assertThat(orderedIds).containsExactly(
                roomWithRecentMessage.getId(),
                newRoomWithoutMessage.getId(),
                roomWithOldMessage.getId());
    }
}
