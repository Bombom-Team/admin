package me.bombom.api.v1.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.event.dto.CreateEventRequest;
import me.bombom.api.v1.event.dto.GetEventDetailResponse;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import me.bombom.api.v1.event.dto.UpdateEventRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Event", description = "이벤트 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface EventControllerApi {

    @Operation(summary = "이벤트 목록 조회", description = """
            이벤트 목록을 조회합니다.

            - **keyword**: 이벤트 이름 검색
            - **status**: 이벤트 상태 필터링 (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    Page<GetEventResponse> getEvents(
            @ParameterObject @ModelAttribute GetEventsRequest request,
            @ParameterObject Pageable pageable);

    @Operation(summary = "이벤트 상세 조회", description = "이벤트 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트", content = @Content)
    })
    GetEventDetailResponse getEvent(
            @Parameter(description = "조회할 이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);

    @Operation(summary = "이벤트 생성", description = "새로운 이벤트를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "이벤트 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    void createEvent(@Valid @RequestBody CreateEventRequest request);

    @Operation(summary = "이벤트 수정", description = "기존 이벤트의 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "이벤트 수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트", content = @Content)
    })
    void updateEvent(
            @Parameter(description = "수정할 이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @RequestBody UpdateEventRequest request);

    @Operation(summary = "이벤트 삭제", description = "기존 이벤트를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "이벤트 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    void deleteEvent(
            @Parameter(description = "삭제할 이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);
}

