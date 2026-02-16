package me.bombom.api.v1.event.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.event.dto.CreateEventNotificationScheduleRequest;
import me.bombom.api.v1.event.dto.GetEventNotificationScheduleResponse;
import me.bombom.api.v1.event.dto.UpdateEventNotificationScheduleRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "EventNotificationSchedule", description = "이벤트 알림 스케줄 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface EventNotificationScheduleControllerApi {

    @Operation(summary = "이벤트 알림 스케줄 목록 조회", description = "단일 이벤트에 대한 알림 스케줄 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트", content = @Content)
    })
    List<GetEventNotificationScheduleResponse> getEventNotificationSchedules(
            @Parameter(description = "이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);

    @Operation(summary = "이벤트 알림 스케줄 상세 조회", description = "단일 이벤트에 대한 특정 알림 스케줄 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트/스케줄", content = @Content)
    })
    GetEventNotificationScheduleResponse getEventNotificationSchedule(
            @Parameter(description = "이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "스케줄 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId);

    @Operation(summary = "이벤트 알림 스케줄 생성", description = "단일 이벤트에 대한 알림 스케줄을 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트", content = @Content)
    })
    void createEventNotificationSchedule(
            @Parameter(description = "이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody CreateEventNotificationScheduleRequest request);

    @Operation(summary = "이벤트 알림 스케줄 수정", description = "단일 이벤트에 대한 알림 스케줄 정보를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트/스케줄", content = @Content)
    })
    void updateEventNotificationSchedule(
            @Parameter(description = "이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "스케줄 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId,
            @Valid @RequestBody UpdateEventNotificationScheduleRequest request);

    @Operation(summary = "이벤트 알림 스케줄 삭제", description = "단일 이벤트에 대한 알림 스케줄을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 이벤트/스케줄", content = @Content)
    })
    void deleteEventNotificationSchedule(
            @Parameter(description = "이벤트 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Parameter(description = "스케줄 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long scheduleId);
}
