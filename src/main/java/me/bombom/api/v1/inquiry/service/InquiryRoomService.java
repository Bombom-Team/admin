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
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomDetailResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.LastMessageResponse;
import me.bombom.api.v1.inquiry.repository.InquiryMessageImageRepository;
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

    private static final int GUEST_LABEL_ID_LENGTH = 8;
    private static final String WITHDRAWN_MEMBER_LABEL = "탈퇴한 회원";

    private final InquiryRoomRepository inquiryRoomRepository;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryMessageImageRepository inquiryMessageImageRepository;
    private final MemberRepository memberRepository;

    @Transactional
    public void assignRoom(Long roomId, AssignInquiryRoomRequest request) {
        InquiryRoom room = getRoomById(roomId);
        room.assign(request.assigneeId());
    }

    public Page<InquiryRoomResponse> getRooms(GetInquiryRoomsRequest request, Pageable pageable) {
        Page<InquiryRoom> rooms = inquiryRoomRepository.findRoomsForAdmin(request, pageable);
        List<InquiryRoom> roomContent = rooms.getContent();

        List<Long> roomIds = roomContent.stream().map(InquiryRoom::getId).toList();
        Map<Long, InquiryMessage> latestMessageByRoomId = inquiryMessageRepository
                .findLatestMessagesByRoomIds(roomIds).stream()
                .collect(Collectors.toMap(InquiryMessage::getRoomId, Function.identity()));

        Set<Long> latestMessageIds = latestMessageByRoomId.values().stream()
                .map(InquiryMessage::getId)
                .collect(Collectors.toSet());
        Set<Long> messageIdsWithImages = inquiryMessageImageRepository
                .findByMessageIdInOrderBySortOrderAsc(List.copyOf(latestMessageIds)).stream()
                .map(InquiryMessageImage::getMessageId)
                .collect(Collectors.toSet());

        Map<Long, Member> memberById = findRelatedMembers(roomContent, latestMessageByRoomId);

        List<InquiryRoomResponse> content = roomContent.stream()
                .map(room -> toResponse(room, latestMessageByRoomId.get(room.getId()), messageIdsWithImages, memberById))
                .toList();

        return new PageImpl<>(content, pageable, rooms.getTotalElements());
    }

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
            Set<Long> messageIdsWithImages,
            Map<Long, Member> memberById) {
        InquirerType inquirerType = room.getMemberId() != null ? InquirerType.MEMBER : InquirerType.GUEST;
        Member inquirer = room.getMemberId() != null ? memberById.get(room.getMemberId()) : null;

        String inquirerLabel;
        String inquirerEmail = null;
        String inquirerProfileImageUrl = null;
        if (inquirerType == InquirerType.GUEST) {
            inquirerLabel = "게스트" + room.getGuestId().substring(0, GUEST_LABEL_ID_LENGTH);
        } else if (inquirer != null) {
            inquirerLabel = inquirer.getNickname();
            inquirerEmail = inquirer.getEmail();
            inquirerProfileImageUrl = inquirer.getProfileImageUrl();
        } else {
            inquirerLabel = WITHDRAWN_MEMBER_LABEL;
        }

        String assigneeNickname = null;
        if (room.getAssigneeId() != null) {
            Member assignee = memberById.get(room.getAssigneeId());
            assigneeNickname = assignee != null ? assignee.getNickname() : WITHDRAWN_MEMBER_LABEL;
        }

        LastMessageResponse lastMessage = null;
        if (latestMessage != null) {
            boolean hasImages = messageIdsWithImages.contains(latestMessage.getId());
            String adminNickname = null;
            if (latestMessage.getSenderType() == InquirySenderType.ADMIN) {
                Member sender = memberById.get(latestMessage.getAdminId());
                adminNickname = sender != null ? sender.getNickname() : WITHDRAWN_MEMBER_LABEL;
            }
            lastMessage = LastMessageResponse.of(latestMessage, hasImages, adminNickname);
        }

        return InquiryRoomResponse.of(
                room, assigneeNickname, inquirerType, inquirerLabel, inquirerEmail, inquirerProfileImageUrl, lastMessage);
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
}
