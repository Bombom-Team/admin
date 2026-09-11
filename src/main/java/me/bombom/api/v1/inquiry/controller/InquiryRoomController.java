package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomDetailResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.service.InquiryRoomService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/inquiries/rooms")
public class InquiryRoomController implements InquiryRoomControllerApi {

    private final InquiryRoomService inquiryRoomService;

    @Override
    @GetMapping
    public Page<InquiryRoomResponse> getRooms(
            @ModelAttribute GetInquiryRoomsRequest request,
            @PageableDefault(size = 20) Pageable pageable) {
        return inquiryRoomService.getRooms(request, pageable);
    }

    @Override
    @GetMapping("/{roomId}")
    public InquiryRoomDetailResponse getRoom(@PathVariable @Positive Long roomId) {
        return inquiryRoomService.getRoom(roomId);
    }

    @Override
    @PatchMapping("/{roomId}/assignee")
    public void assignRoom(
            @PathVariable @Positive Long roomId,
            @Valid @RequestBody AssignInquiryRoomRequest request) {
        inquiryRoomService.assignRoom(roomId, request);
    }

    @Override
    @PatchMapping("/{roomId}/status")
    public void changeStatus(
            @PathVariable @Positive Long roomId,
            @Valid @RequestBody UpdateInquiryRoomStatusRequest request) {
        inquiryRoomService.changeStatus(roomId, request);
    }

    @Override
    @PatchMapping("/{roomId}/category")
    public void changeCategory(
            @PathVariable @Positive Long roomId,
            @Valid @RequestBody UpdateInquiryRoomCategoryRequest request) {
        inquiryRoomService.changeCategory(roomId, request);
    }
}
