package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquirerType;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.fixture.InquiryCategoryFixture;
import me.bombom.api.v1.inquiry.fixture.InquiryMessageFixture;
import me.bombom.api.v1.inquiry.fixture.InquiryRoomFixture;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.fixture.MemberFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
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

    @Autowired
    private InquiryMessageRepository inquiryMessageRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private InquiryCategoryRepository inquiryCategoryRepository;

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

    @Test
    @DisplayName("채팅방의 문의 카테고리를 변경한다.")
    void 카테고리_변경() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));
        var newCategory = inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("환불"));

        // when
        inquiryRoomService.changeCategory(room.getId(), new UpdateInquiryRoomCategoryRequest(newCategory.getId()));

        // then
        InquiryRoom updated = inquiryRoomRepository.findById(room.getId()).orElseThrow();
        assertThat(updated.getCategoryId()).isEqualTo(newCategory.getId());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리로 변경하려 하면 예외가 발생한다.")
    void 존재하지_않는_카테고리로_변경_실패() {
        // given
        InquiryRoom room = inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(1L, 10L));

        // when & then
        assertThatThrownBy(() -> inquiryRoomService.changeCategory(
                room.getId(), new UpdateInquiryRoomCategoryRequest(999L)))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("회원 문의방 목록에는 문의자 닉네임/이메일과 담당자 닉네임, 최근 메시지가 채워진다.")
    void 회원_문의방_목록_상세_정보_포함() {
        // given
        Member inquirer = memberRepository.save(MemberFixture.createMember("메이"));
        Member assignee = memberRepository.save(MemberFixture.createMemberWithRole("상추", 2L));

        InquiryRoom room = inquiryRoomRepository.save(
                InquiryRoomFixture.createMemberRoom(inquirer.getId(), 10L));
        room.assign(assignee.getId());

        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), assignee.getId(), "안녕하세요"));

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(
                new GetInquiryRoomsRequest(null, null, null), PageRequest.of(0, 10));

        // then
        InquiryRoomResponse response = result.getContent().get(0);
        assertSoftly(softly -> {
            softly.assertThat(response.inquirerType()).isEqualTo(InquirerType.MEMBER);
            softly.assertThat(response.guestId()).isNull();
            softly.assertThat(response.inquirerNickname()).isEqualTo("메이");
            softly.assertThat(response.inquirerEmail()).isEqualTo(inquirer.getEmail());
            softly.assertThat(response.assigneeNickname()).isEqualTo("상추");
            softly.assertThat(response.lastMessage()).isNotNull();
            softly.assertThat(response.lastMessage().content()).isEqualTo("안녕하세요");
            softly.assertThat(response.lastMessage().senderType()).isEqualTo(InquirySenderType.ADMIN);
            softly.assertThat(response.lastMessage().adminNickname()).isEqualTo("상추");
        });
        assertThat(message.getId()).isNotNull();
    }

    @Test
    @DisplayName("게스트 문의방은 guestId가 채워지고 회원 관련 필드는 null이다.")
    void 게스트_문의방_raw_데이터() {
        // given
        inquiryRoomRepository.save(
                InquiryRoomFixture.createGuestRoom("abcdefgh-1111-2222-3333-444444444444", 10L));

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(
                new GetInquiryRoomsRequest(null, null, null), PageRequest.of(0, 10));

        // then
        InquiryRoomResponse response = result.getContent().get(0);
        assertSoftly(softly -> {
            softly.assertThat(response.inquirerType()).isEqualTo(InquirerType.GUEST);
            softly.assertThat(response.guestId()).isEqualTo("abcdefgh-1111-2222-3333-444444444444");
            softly.assertThat(response.inquirerNickname()).isNull();
            softly.assertThat(response.inquirerEmail()).isNull();
            softly.assertThat(response.lastMessage()).isNull();
        });
    }

    @Test
    @DisplayName("담당자가 없거나 메시지가 없는 방은 관련 필드가 null로 채워진다.")
    void 담당자_미배정_메시지_없음() {
        // given
        Member inquirer = memberRepository.save(MemberFixture.createMember("강철원"));
        inquiryRoomRepository.save(InquiryRoomFixture.createMemberRoom(inquirer.getId(), 10L));

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(
                new GetInquiryRoomsRequest(null, null, null), PageRequest.of(0, 10));

        // then
        InquiryRoomResponse response = result.getContent().get(0);
        assertSoftly(softly -> {
            softly.assertThat(response.assigneeId()).isNull();
            softly.assertThat(response.assigneeNickname()).isNull();
            softly.assertThat(response.lastMessage()).isNull();
        });
    }

    @Test
    @DisplayName("문의자/담당자/발신자 Member가 이미 삭제된 경우 관련 닉네임 필드가 null로 내려간다.")
    void 삭제된_회원_참조_시_닉네임_null() {
        // given
        Long withdrawnMemberId = 999L;
        InquiryRoom room = inquiryRoomRepository.save(
                InquiryRoomFixture.createMemberRoom(withdrawnMemberId, 10L));
        room.assign(withdrawnMemberId);
        inquiryMessageRepository.save(
                InquiryMessageFixture.createAdminMessage(room.getId(), withdrawnMemberId, "안녕하세요"));

        // when
        Page<InquiryRoomResponse> result = inquiryRoomService.getRooms(
                new GetInquiryRoomsRequest(null, null, null), PageRequest.of(0, 10));

        // then
        InquiryRoomResponse response = result.getContent().get(0);
        assertSoftly(softly -> {
            softly.assertThat(response.inquirerType()).isEqualTo(InquirerType.MEMBER);
            softly.assertThat(response.inquirerNickname()).isNull();
            softly.assertThat(response.inquirerEmail()).isNull();
            softly.assertThat(response.assigneeNickname()).isNull();
            softly.assertThat(response.lastMessage().adminNickname()).isNull();
        });
    }
}
