package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({ InquiryRoomService.class, QuerydslConfig.class })
class InquiryRoomServiceTest {

    @Autowired
    private InquiryRoomService inquiryRoomService;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Test
    @DisplayName("상태로 채팅방 목록을 필터링한다.")
    void 채팅방_목록_상태_필터_조회() {
        // given
        InquiryRoom room1 = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryRoom room2 = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(2L, 10L));
        room2.changeStatus(InquiryStatus.DONE);

        GetInquiryRoomsRequest request = new GetInquiryRoomsRequest(InquiryStatus.UNCONFIRMED, null, null);
        Pageable pageable = PageRequest.of(0, 10);

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(request, pageable);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getContent()).hasSize(1);
            softly.assertThat(result.getContent().get(0).id()).isEqualTo(room1.getId());
        });
    }

    @Test
    @DisplayName("담당자로 채팅방 목록을 필터링한다.")
    void 채팅방_목록_담당자_필터_조회() {
        // given
        InquiryRoom room1 = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        room1.assign(100L);
        inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(2L, 10L));

        GetInquiryRoomsRequest request = new GetInquiryRoomsRequest(null, 100L, null);

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(request, PageRequest.of(0, 10));

        // then
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("채팅방 상세를 조회한다.")
    void 채팅방_상세_조회() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createGuestRoom("guest-uuid", 10L));

        // when
        var response = inquiryRoomService.getRoom(room.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.guestId()).isEqualTo("guest-uuid");
            softly.assertThat(response.status()).isEqualTo(InquiryStatus.UNCONFIRMED);
        });
    }

    @Test
    @DisplayName("존재하지 않는 채팅방 조회 시 예외가 발생한다.")
    void 존재하지_않는_채팅방_조회_실패() {
        // when & then
        assertThatThrownBy(() -> inquiryRoomService.getRoom(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("담당자를 지정한다.")
    void 담당자_지정() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));

        // when
        inquiryRoomService.assignRoom(room.getId(), new AssignInquiryRoomRequest(100L));

        // then
        InquiryRoom updated = inquiryRoomRepository.findById(room.getId()).orElseThrow();
        assertThat(updated.getAssigneeId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("채팅방 상태를 변경한다.")
    void 상태_변경() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));

        // when
        inquiryRoomService.changeStatus(room.getId(), new UpdateInquiryRoomStatusRequest(InquiryStatus.ON_HOLD));

        // then
        InquiryRoom updated = inquiryRoomRepository.findById(room.getId()).orElseThrow();
        assertThat(updated.getStatus()).isEqualTo(InquiryStatus.ON_HOLD);
    }
}
