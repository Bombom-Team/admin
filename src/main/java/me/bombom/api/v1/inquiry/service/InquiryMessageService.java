package me.bombom.api.v1.inquiry.service;

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

    private static final int DEFAULT_PAGE_SIZE = 20;

    private final InquiryRoomService inquiryRoomService;
    private final InquiryMessageRepository inquiryMessageRepository;
    private final InquiryMessageImageRepository inquiryMessageImageRepository;

    public InquiryMessagePageResponse getMessages(Long roomId, Long cursor, Integer size) {
        int pageSize = size == null ? DEFAULT_PAGE_SIZE : size;
        List<InquiryMessage> messages = inquiryMessageRepository.findMessagesByCursor(roomId, cursor, pageSize + 1);

        boolean hasNext = messages.size() > pageSize;
        List<InquiryMessage> content = hasNext ? messages.subList(0, pageSize) : messages;

        Map<Long, List<String>> imagesByMessageId = findImagesByMessageId(content);

        List<InquiryMessageResponse> responses = content.stream()
                .map(message -> InquiryMessageResponse.of(
                        message, imagesByMessageId.getOrDefault(message.getId(), List.of())))
                .toList();

        return InquiryMessagePageResponse.of(responses, hasNext);
    }

    @Transactional
    public InquiryMessageResponse sendMessage(Long roomId, Long adminId, SendAdminInquiryMessageRequest request) {
        InquiryRoom room = inquiryRoomService.getRoomOrThrow(roomId);
        validateRoomNotClosed(room);
        assignAndActivateIfFirstResponse(room, adminId);

        InquiryMessage message = InquiryMessage.createAdminMessage(roomId, adminId, request.content());
        inquiryMessageRepository.save(message);
        List<String> imageUrls = saveImages(message.getId(), request.imageUrls());

        return InquiryMessageResponse.of(message, imageUrls);
    }

    @Transactional
    public InquiryMessageResponse updateMessage(
            Long roomId,
            Long messageId,
            Long adminId,
            UpdateAdminInquiryMessageRequest request
    ) {
        InquiryMessage message = getOwnedMessage(roomId, messageId, adminId);
        message.updateContent(request.content());
        List<String> imageUrls = findImagesByMessageId(List.of(message))
                .getOrDefault(message.getId(), List.of());
        return InquiryMessageResponse.of(message, imageUrls);
    }

    @Transactional
    public void deleteMessage(Long roomId, Long messageId, Long adminId) {
        InquiryMessage message = getOwnedMessage(roomId, messageId, adminId);
        inquiryMessageImageRepository.deleteByMessageId(message.getId());
        inquiryMessageRepository.delete(message);
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

    private List<String> saveImages(Long messageId, List<String> imageUrls) {
        if (imageUrls == null || imageUrls.isEmpty()) {
            return List.of();
        }
        List<InquiryMessageImage> images = new ArrayList<>();
        for (int i = 0; i < imageUrls.size(); i += 1) {
            images.add(new InquiryMessageImage(messageId, imageUrls.get(i), i));
        }
        inquiryMessageImageRepository.saveAll(images);
        return imageUrls;
    }

    private Map<Long, List<String>> findImagesByMessageId(List<InquiryMessage> messages) {
        List<Long> messageIds = messages.stream().map(InquiryMessage::getId).toList();
        if (messageIds.isEmpty()) {
            return Map.of();
        }
        return inquiryMessageImageRepository.findByMessageIdInOrderBySortOrderAsc(messageIds).stream()
                .collect(Collectors.groupingBy(
                        InquiryMessageImage::getMessageId,
                        Collectors.mapping(InquiryMessageImage::getImageUrl, Collectors.toList())));
    }
}
