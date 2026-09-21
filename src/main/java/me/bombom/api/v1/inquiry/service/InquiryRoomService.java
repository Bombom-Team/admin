package me.bombom.api.v1.inquiry.service;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquirerType;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomDetailResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.LastMessageResponse;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryRoomService {

    private final InquiryRoomRepository inquiryRoomRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryCategoryRepository inquiryCategoryRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void assignRoom(Long roomId, AssignInquiryRoomRequest request) {
        InquiryRoom room = getRoomById(roomId);
        validateAssigneeExists(request.assigneeId());
        room.assign(request.assigneeId());
    }

    public Page<InquiryRoomResponse> getRooms(GetInquiryRoomsRequest request, Pageable pageable) {
        Page<InquiryRoom> rooms = inquiryRoomRepository.findRoomsForAdmin(request, pageable);
        List<InquiryRoom> roomContent = rooms.getContent();

        List<Long> roomIds = roomContent.stream().map(InquiryRoom::getId).toList();
        Map<Long, InquiryMessage> latestMessageByRoomId = inquiryMessageRepository
                .findLatestMessagesByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(InquiryMessage::getRoomId, Function.identity()));

        Map<Long, Member> memberById = findRelatedMembers(roomContent, latestMessageByRoomId);

        List<InquiryRoomResponse> content = roomContent.stream()
                .map(room -> toResponse(room, latestMessageByRoomId.get(room.getId()), memberById))
                .toList();

        return new PageImpl<>(content, pageable, rooms.getTotalElements());
    }

    public InquiryRoomDetailResponse getRoom(Long roomId) {
        return InquiryRoomDetailResponse.from(getRoomById(roomId));
    }

    public InquiryRoom getRoomById(Long roomId) {
        return inquiryRoomRepository.findById(roomId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryRoom"));
    }

    @Transactional
    public void changeStatus(Long roomId, UpdateInquiryRoomStatusRequest request) {
        InquiryRoom room = getRoomById(roomId);
        room.changeStatus(request.status());
    }

    @Transactional
    public void changeCategory(Long roomId, UpdateInquiryRoomCategoryRequest request) {
        InquiryRoom room = getRoomById(roomId);
        validateCategoryExists(request.categoryId());
        room.changeCategory(request.categoryId());
    }

    // 채팅방과 관련이 있는 유저(문의자, 담당자, 담당자 아닌 어드민) 정보를 조회한다
    private Map<Long, Member> findRelatedMembers(
            List<InquiryRoom> rooms, Map<Long, InquiryMessage> latestMessageByRoomId) {
        Set<Long> memberIds = new HashSet<>();
        for (InquiryRoom room : rooms) {
            if (room.getMemberId() != null) {
                memberIds.add(room.getMemberId());
            }
            if (room.getAssigneeId() != null) {
                memberIds.add(room.getAssigneeId());
            }
        }
        for (InquiryMessage message : latestMessageByRoomId.values()) {
            if (message.getAdminId() != null) {
                memberIds.add(message.getAdminId());
            }
        }
        if (memberIds.isEmpty()) {
            return Map.of();
        }
        return memberRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(Member::getId, Function.identity()));
    }

    private InquiryRoomResponse toResponse(
            InquiryRoom room,
            InquiryMessage latestMessage,
            Map<Long, Member> memberById) {
        InquirerType inquirerType = room.getMemberId() != null ? InquirerType.MEMBER : InquirerType.GUEST;
        Member inquirer = room.getMemberId() != null ? memberById.get(room.getMemberId()) : null;

        String inquirerNickname = inquirer != null ? inquirer.getNickname() : null;
        String inquirerEmail = inquirer != null ? inquirer.getEmail() : null;

        Member assignee = room.getAssigneeId() != null ? memberById.get(room.getAssigneeId()) : null;
        String assigneeNickname = assignee != null ? assignee.getNickname() : null;

        LastMessageResponse lastMessage = null;
        if (latestMessage != null) {
            String adminNickname = null;
            if (latestMessage.getSenderType() == InquirySenderType.ADMIN) {
                Member sender = memberById.get(latestMessage.getAdminId());
                adminNickname = sender != null ? sender.getNickname() : null;
            }
            lastMessage = LastMessageResponse.of(latestMessage, adminNickname);
        }

        return InquiryRoomResponse.of(room, assigneeNickname, inquirerType, inquirerNickname, inquirerEmail,
                lastMessage);
    }

    private void validateCategoryExists(Long categoryId) {
        if (!inquiryCategoryRepository.existsById(categoryId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryCategory");
        }
    }

    private void validateAssigneeExists(Long assigneeId) {
        if (!memberRepository.existsById(assigneeId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "member");
        }
    }
}
