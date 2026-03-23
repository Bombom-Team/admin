package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Challenge Daily Guide", description = "챌린지 데일리 가이드 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface ChallengeDailyGuideControllerApi {

    @Operation(summary = "데일리 가이드 생성", description = "챌린지에 데일리 가이드를 생성합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void create(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @RequestBody @Valid CreateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 목록 조회", description = "챌린지의 데일리 가이드 목록을 dayIndex 오름차순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    List<GetDailyGuideResponse> getDailyGuides(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

    @Operation(summary = "데일리 가이드 단건 조회", description = "데일리 가이드 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    GetDailyGuideResponse getDailyGuide(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);

    @Operation(summary = "데일리 가이드 수정", description = "데일리 가이드 정보를 수정합니다. 전달한 필드만 업데이트됩니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void update(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId,
            @RequestBody @Valid UpdateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 삭제", description = "데일리 가이드를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void delete(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);
}
