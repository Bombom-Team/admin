package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.request.SendAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.service.InquiryMessageService;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/inquiries/rooms/{roomId}/messages")
public class InquiryMessageController implements InquiryMessageControllerApi {

    private final InquiryMessageService inquiryMessageService;

    @Override
    @GetMapping
    public InquiryMessagePageResponse getMessages(
            @PathVariable @Positive Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return inquiryMessageService.getMessages(roomId, cursor, size);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryMessageResponse sendMessage(
            @PathVariable @Positive Long roomId,
            @LoginMember Member admin,
            @Valid @RequestBody SendAdminInquiryMessageRequest request) {
        return inquiryMessageService.sendMessage(roomId, admin.getId(), request);
    }

    @Override
    @PatchMapping("/{messageId}")
    public InquiryMessageResponse updateMessage(
            @PathVariable @Positive Long roomId,
            @PathVariable @Positive Long messageId,
            @LoginMember Member admin,
            @Valid @RequestBody UpdateAdminInquiryMessageRequest request) {
        return inquiryMessageService.updateMessage(roomId, messageId, admin.getId(), request);
    }

    @Override
    @DeleteMapping("/{messageId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(
            @PathVariable @Positive Long roomId,
            @PathVariable @Positive Long messageId,
            @LoginMember Member admin) {
        inquiryMessageService.deleteMessage(roomId, messageId, admin.getId());
    }
}
