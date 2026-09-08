package me.bombom.api.v1.holiday.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.holiday.dto.CreateHolidayRequest;
import me.bombom.api.v1.holiday.dto.CreateHolidaysRequest;
import me.bombom.api.v1.holiday.dto.GetHolidayResponse;
import me.bombom.api.v1.holiday.dto.GetHolidaysRequest;
import me.bombom.api.v1.holiday.dto.UpdateHolidayRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@Tag(name = "Holiday", description = "공휴일 수동 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface HolidayControllerApi {

    @Operation(summary = "공휴일 목록 조회", description = "공휴일 목록을 날짜 오름차순으로 조회합니다. year를 지정하지 않으면 전체를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    List<GetHolidayResponse> getHolidays(@ParameterObject GetHolidaysRequest request);

    @Operation(summary = "공휴일 등록", description = "공휴일을 등록합니다. 이미 등록된 날짜는 등록할 수 없습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "공휴일 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 또는 이미 등록된 날짜", content = @Content)
    })
    void createHoliday(@Valid @RequestBody CreateHolidayRequest request);

    @Operation(summary = "공휴일 일괄 등록", description = """
            공휴일을 한 번에 등록합니다. (최대 100건)
            요청 내에 중복된 날짜가 있거나 이미 등록된 날짜가 하나라도 포함되면 전체가 등록되지 않습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "공휴일 일괄 등록 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값, 요청 내 중복 날짜 또는 이미 등록된 날짜", content = @Content)
    })
    void createHolidays(@Valid @RequestBody CreateHolidaysRequest request);

    @Operation(summary = "공휴일 수정", description = "등록된 공휴일의 날짜와 이름을 수정합니다. null인 필드는 수정하지 않습니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "공휴일 수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 또는 이미 등록된 날짜", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공휴일", content = @Content)
    })
    void updateHoliday(
            @Parameter(description = "수정할 공휴일 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody UpdateHolidayRequest request);

    @Operation(summary = "공휴일 삭제", description = "등록된 공휴일을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "공휴일 삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 공휴일", content = @Content)
    })
    void deleteHoliday(
            @Parameter(description = "삭제할 공휴일 ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);
}
