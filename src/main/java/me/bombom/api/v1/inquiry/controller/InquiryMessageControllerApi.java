package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.inquiry.dto.request.SendAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@Tag(name = "InquiryMessage", description = "문의 채팅 메시지(어드민) API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface InquiryMessageControllerApi {

    @Operation(summary = "문의 채팅 메시지 커서 조회")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "조회 성공") })
    InquiryMessagePageResponse getMessages(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Parameter(description = "커서(마지막으로 받은 메시지 ID)") @RequestParam(required = false) Long cursor,
            @Parameter(description = "페이지 크기") @RequestParam(required = false) Integer size);

    @Operation(summary = "어드민 메시지 전송", description = "담당자가 없으면 자동 배정되고, 상태가 미확인이면 진행중으로 전환됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "전송 성공"),
            @ApiResponse(responseCode = "400", description = "종료된 채팅방", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content)
    })
    InquiryMessageResponse sendMessage(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @LoginMember Member admin,
            @Valid @RequestBody SendAdminInquiryMessageRequest request);

    @Operation(summary = "어드민 메시지 수정", description = "본인이 작성한 메시지만 수정 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 작성한 메시지가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 메시지", content = @Content)
    })
    InquiryMessageResponse updateMessage(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Parameter(description = "메시지 ID") @PathVariable @Positive Long messageId,
            @LoginMember Member admin,
            @Valid @RequestBody UpdateAdminInquiryMessageRequest request);

    @Operation(summary = "어드민 메시지 삭제", description = "본인이 작성한 메시지만 삭제 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "403", description = "본인이 작성한 메시지가 아님", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 메시지", content = @Content)
    })
    void deleteMessage(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Parameter(description = "메시지 ID") @PathVariable @Positive Long messageId,
            @LoginMember Member admin);
}
