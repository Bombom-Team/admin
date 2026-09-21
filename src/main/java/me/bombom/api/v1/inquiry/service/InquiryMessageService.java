package me.bombom.api.v1.inquiry.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;
import me.bombom.api.v1.inquiry.domain.InquiryMessageImage;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.SendAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.repository.InquiryMessageImageRepository;
import me.bombom.api.v1.inquiry.repository.InquiryMessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryMessageService {

    private final InquiryRoomService inquiryRoomService;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryMessageImageRepository inquiryMessageImageRepository;
    private final Clock clock;

    @Transactional
    public InquiryMessageResponse sendMessage(Long roomId, Long adminId, SendAdminInquiryMessageRequest request) {
        InquiryRoom room = inquiryRoomService.getRoomById(roomId);
        validateRoomNotClosed(room);
        assignAndActivateIfFirstResponse(room, adminId);

        InquiryMessage message = inquiryMessageRepository.save(
                InquiryMessage.createAdminMessage(roomId, adminId, request.content()));
        List<InquiryMessageImage> images = saveImages(message.getId(), request.imageUrls());

        return InquiryMessageResponse.of(message, images);
    }

    public InquiryMessagePageResponse getMessages(Long roomId, Long cursor, int size) {
        List<InquiryMessage> messages = inquiryMessageRepository.findMessagesByCursor(roomId, cursor, size + 1);

        boolean hasNext = messages.size() > size;
        List<InquiryMessage> pageMessages = hasNext ? messages.subList(0, size) : messages;

        List<Long> messageIds = pageMessages.stream().map(InquiryMessage::getId).toList();
        List<InquiryMessageImage> images = inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(
                messageIds);
        Map<Long, List<InquiryMessageImage>> imagesByMessageId = images.stream()
                .collect(Collectors.groupingBy(InquiryMessageImage::getMessageId));

        return InquiryMessagePageResponse.of(pageMessages, imagesByMessageId, hasNext);
    }

    @Transactional
    public InquiryMessageResponse updateMessage(
            Long roomId, Long messageId, Long adminId, UpdateAdminInquiryMessageRequest request
    ) {
        InquiryMessage message = getOwnedMessage(roomId, messageId, adminId);

        message.updateContent(request.content());
        List<InquiryMessageImage> images =
                inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(List.of(messageId));
        return InquiryMessageResponse.of(message, images);
    }

    @Transactional
    public void deleteMessage(Long roomId, Long messageId, Long adminId) {
        InquiryMessage message = getOwnedMessage(roomId, messageId, adminId);
        message.delete(LocalDateTime.now(clock));
    }

    private void validateRoomNotClosed(InquiryRoom room) {
        if (room.isClosed()) {
            throw new CIllegalArgumentException(ErrorDetail.INQUIRY_ROOM_CLOSED)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryRoom");
        }
    }

    private void assignAndActivateIfFirstResponse(InquiryRoom room, Long adminId) {
        if (room.isUnassigned()) {
            room.assign(adminId);
        }
        if (room.getStatus() == InquiryStatus.UNCONFIRMED) {
            room.changeStatus(InquiryStatus.IN_PROGRESS);
        }
    }

    private InquiryMessage getOwnedMessage(Long roomId, Long messageId, Long adminId) {
        InquiryMessage message = inquiryMessageRepository.findById(messageId)
                .filter(found -> found.getRoomId().equals(roomId))
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryMessage"));
        if (!message.isWrittenByAdmin(adminId)) {
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "inquiryMessage");
        }
        return message;
    }

    private List<InquiryMessageImage> saveImages(Long messageId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<InquiryMessageImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i += 1) {
            images.add(inquiryMessageImageRepository.save(new InquiryMessageImage(messageId, imageUrls.get(i), i)));
        }
        return images;
    }
}
