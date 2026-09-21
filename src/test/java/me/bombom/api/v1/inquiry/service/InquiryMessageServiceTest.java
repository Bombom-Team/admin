package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageArrivalNotification;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.SendAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.fixture.InquiryMessageFixture;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import me.bombom.api.v1.inquiry.repository.InquiryMessageArrivalNotificationRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageImageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({InquiryMessageService.class, InquiryRoomService.class, QuerydslConfig.class, TimeConfig.class})
class InquiryMessageServiceTest {

    @Autowired
    private InquiryMessageService inquiryMessageService;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Autowired
    private InquiryMessageRepository inquiryMessageRepository;

    @Autowired
    private InquiryMessageImageRepository inquiryMessageImageRepository;

    @Autowired
    private InquiryMessageArrivalNotificationRepository inquiryMessageArrivalNotificationRepository;

    @Autowired
    private TestEntityManager testEntityManager;

    @Test
    @DisplayName("담당자 없는 방에 어드민이 처음 답변하면 담당자로 자동 배정되고 상태가 진행중으로 바뀐다.")
    void 최초_답변_시_담당자_자동배정_및_상태_자동전환() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);

        // when
        inquiryMessageService.sendMessage(room.getId(), 100L, request);

        // then
        InquiryRoom updated = inquiryRoomRepository.findById(room.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getAssigneeId()).isEqualTo(100L);
            softly.assertThat(updated.getStatus()).isEqualTo(InquiryStatus.IN_PROGRESS);
        });
    }

    @Test
    @DisplayName("이미 담당자가 있는 방에 다른 어드민이 답변해도 담당자는 바뀌지 않는다.")
    void 담당자_있는_방은_자동배정되지_않음() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        room.assign(999L);
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);

        // when
        inquiryMessageService.sendMessage(room.getId(), 100L, request);

        // then
        InquiryRoom updated = inquiryRoomRepository.findById(room.getId()).orElseThrow();
        assertThat(updated.getAssigneeId()).isEqualTo(999L);
    }

    @Test
    @DisplayName("회원 문의방에 어드민이 답변하면 알림 아웃박스가 PENDING 상태로 저장된다.")
    void 회원_문의방_답변_시_알림_저장() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        String content = "가나다라마바사아자차카타파하가나다라마바사아자차카타파하";
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest(content, null);

        // when
        inquiryMessageService.sendMessage(room.getId(), 100L, request);

        // then
        List<InquiryMessageArrivalNotification> notifications = inquiryMessageArrivalNotificationRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(notifications).hasSize(1);
            InquiryMessageArrivalNotification notification = notifications.get(0);
            softly.assertThat(notification.getMemberId()).isEqualTo(1L);
            softly.assertThat(notification.getRoomId()).isEqualTo(room.getId());
            softly.assertThat(notification.getStatus()).isEqualTo("PENDING");
            softly.assertThat(notification.getContent()).isEqualTo(content.substring(0, 20));
        });
    }

    @Test
    @DisplayName("게스트 문의방에 어드민이 답변해도 알림 아웃박스는 저장되지 않는다.")
    void 게스트_문의방_답변_시_알림_저장되지_않는다() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createGuestRoom("guest-1", 10L));
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);

        // when
        inquiryMessageService.sendMessage(room.getId(), 100L, request);

        // then
        assertThat(inquiryMessageArrivalNotificationRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("종료된 채팅방에는 메시지를 보낼 수 없다.")
    void 종료된_방_메시지_전송_실패() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        room.changeStatus(InquiryStatus.DONE);
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);

        // when & then
        assertThatThrownBy(() -> inquiryMessageService.sendMessage(room.getId(), 100L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.INQUIRY_ROOM_CLOSED.getMessage());
    }

    @Test
    @DisplayName("보류 상태의 채팅방에도 메시지를 보낼 수 없다.")
    void 보류_상태_방_메시지_전송_실패() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        room.changeStatus(InquiryStatus.ON_HOLD);
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);

        // when & then
        assertThatThrownBy(() -> inquiryMessageService.sendMessage(room.getId(), 100L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.INQUIRY_ROOM_CLOSED.getMessage());
    }

    @Test
    @DisplayName("본인이 작성한 메시지를 수정한다.")
    void 본인_메시지_수정_성공() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "원본"));

        // when
        InquiryMessageResponse response = inquiryMessageService.updateMessage(
                room.getId(), message.getId(), 100L, new UpdateAdminInquiryMessageRequest("수정본"));

        // then
        assertThat(response.content()).isEqualTo("수정본");
    }

    @Test
    @DisplayName("본인이 작성하지 않은 메시지를 수정하려 하면 예외가 발생한다.")
    void 타인_메시지_수정_실패() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "원본"));

        // when & then
        assertThatThrownBy(() -> inquiryMessageService.updateMessage(
                room.getId(), message.getId(), 999L, new UpdateAdminInquiryMessageRequest("수정본")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.FORBIDDEN_RESOURCE.getMessage());
    }

    @Test
    @DisplayName("다른 방의 메시지 ID로 수정하려 하면 존재하지 않는 메시지로 처리된다.")
    void 다른_방_메시지_수정_실패() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryRoom otherRoom = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(2L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(otherRoom.getId(), 100L, "원본"));

        // when & then
        assertThatThrownBy(() -> inquiryMessageService.updateMessage(
                room.getId(), message.getId(), 100L, new UpdateAdminInquiryMessageRequest("수정본")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("본인이 작성한 메시지를 삭제하면 물리적으로 삭제되지 않고 deletedAt만 채워진다.")
    void 본인_메시지_삭제_성공() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "원본"));

        // when
        inquiryMessageService.deleteMessage(room.getId(), message.getId(), 100L);
        testEntityManager.flush();
        testEntityManager.clear();

        // then
        assertThat(inquiryMessageRepository.findById(message.getId())).isEmpty();
    }

    @Test
    @DisplayName("메시지를 삭제하면 연결된 이미지도 함께 삭제된다.")
    void 메시지_삭제_시_이미지도_삭제() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "이미지 첨부"));
        inquiryMessageImageRepository.save(new InquiryMessageImage(message.getId(), "http://image.url/1", 0));

        // when
        inquiryMessageService.deleteMessage(room.getId(), message.getId(), 100L);

        // then
        assertThat(inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(List.of(message.getId())))
                .isEmpty();
    }

    @Test
    @DisplayName("삭제된 메시지는 목록 조회에 나타나지 않는다.")
    void 삭제된_메시지는_조회되지_않는다() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "삭제될 메시지"));
        inquiryMessageService.deleteMessage(room.getId(), message.getId(), 100L);

        // when
        var result = inquiryMessageService.getMessages(room.getId(), null, 20);

        // then
        assertThat(result.messages()).isEmpty();
    }

    @Test
    @DisplayName("메시지 목록을 커서 기반으로 조회한다.")
    void 메시지_커서_조회() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        for (int i = 0; i < 3; i += 1) {
            inquiryMessageRepository.save(
                    InquiryMessageFixture.createAdminMessage(room.getId(), 100L, "메시지" + i));
        }

        // when
        var result = inquiryMessageService.getMessages(room.getId(), null, 2);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.messages()).hasSize(2);
            softly.assertThat(result.hasNext()).isTrue();
        });
    }
}
