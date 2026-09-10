package me.bombom.api.v1.inquiry.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomDetailResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryRoomService {

    private final InquiryRoomRepository inquiryRoomRepository;

    @Transactional
    public void assignRoom(Long roomId, AssignInquiryRoomRequest request) {
        InquiryRoom room = getRoomById(roomId);
        room.assign(request.assigneeId());
    }

    public Page<InquiryRoomResponse> getRooms(GetInquiryRoomsRequest request, Pageable pageable) {
        return inquiryRoomRepository.findRoomsForAdmin(request, pageable)
                .map(InquiryRoomResponse::from);
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
