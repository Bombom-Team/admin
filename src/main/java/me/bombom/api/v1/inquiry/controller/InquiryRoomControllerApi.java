package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomDetailResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "InquiryRoom", description = "문의 채팅방 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface InquiryRoomControllerApi {

    @Operation(
            summary = "문의 채팅방 목록 조회",
            description = "상태/담당자/카테고리로 필터링하여 조회합니다. "
                    + "목록은 최신 메시지 시각(없으면 방 생성 시각) 기준 내림차순으로 정렬되며, "
                    + "각 방에는 문의자 정보(회원/게스트)와 담당자 닉네임, 최근 메시지 미리보기가 함께 내려갑니다.")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "목록 조회 성공") })
    Page<InquiryRoomResponse> getRooms(
            @ParameterObject GetInquiryRoomsRequest request,
            @ParameterObject Pageable pageable);

    @Operation(summary = "문의 채팅방 상세 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content)
    })
    InquiryRoomDetailResponse getRoom(@Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId);

    @Operation(summary = "담당자 지정/변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "지정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content)
    })
    void assignRoom(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Valid @RequestBody AssignInquiryRoomRequest request);

    @Operation(summary = "채팅방 상태 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방", content = @Content)
    })
    void changeStatus(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Valid @RequestBody UpdateInquiryRoomStatusRequest request);

    @Operation(summary = "문의 카테고리 변경")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "변경 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 채팅방 또는 카테고리", content = @Content)
    })
    void changeCategory(
            @Parameter(description = "채팅방 ID") @PathVariable @Positive Long roomId,
            @Valid @RequestBody UpdateInquiryRoomCategoryRequest request);
}
