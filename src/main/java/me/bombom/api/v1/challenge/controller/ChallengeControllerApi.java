package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.CreateChallengeTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeDayResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeRequest;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "Challenge", description = "챌린지 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface ChallengeControllerApi {

        @Operation(summary = "챌린지 생성", description = """
                        챌린지를 생성합니다.

                        - `totalDays`는 입력한 startDate ~ endDate 범위 내 **주말(토·일)을 제외한 평일 수**로 자동 계산됩니다. 별도로 전달하지 않아도 됩니다.

                        **요청 필드:**
                        - `name` (필수): 챌린지 이름
                        - `generation` (필수, 양수): 챌린지 기수
                        - `startDate` (필수): 챌린지 시작일 (yyyy-MM-dd)
                        - `endDate` (필수): 챌린지 종료일 (yyyy-MM-dd)
                        - `newsletterGroupId` (필수): 뉴스레터 그룹 ID
                        - `totalDays`는 입력한 startDate ~ endDate 범위 내 **주말(토·일)을 제외한 평일 수**로 자동 계산됩니다
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "201", description = "생성 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 입력 (필수 필드 누락, generation이 0 이하 등)", content = @Content)
        })
        @PostMapping
        @ResponseStatus(HttpStatus.CREATED)
        void createChallenge(@RequestBody @Valid CreateChallengeRequest request);

        @Operation(summary = "챌린지 수정", description = """
                        챌린지 정보를 부분 수정합니다.

                        - 전달하지 않은 필드(null)는 기존 값이 유지됩니다.
                        - `startDate` 또는 `endDate` 중 하나만 변경해도 `totalDays`는 두 값 모두를 기준으로 **자동 재계산**됩니다.

                        **요청 필드 (모두 선택):**
                        - `name`: 챌린지 이름
                        - `generation`: 챌린지 기수 (양수)
                        - `startDate`: 챌린지 시작일 (yyyy-MM-dd)
                        - `endDate`: 챌린지 종료일 (yyyy-MM-dd)
                        - `newsletterGroupId`: 뉴스레터 그룹 ID
                        - `totalDays`는 입력한 startDate ~ endDate 범위 내 **주말(토·일)을 제외한 평일 수**로 자동 계산됩니다
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "수정 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        @PatchMapping("/{challengeId}")
        void updateChallenge(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @RequestBody @Valid UpdateChallengeRequest request);

        @Operation(summary = "챌린지 삭제", description = """
                        챌린지를 삭제합니다.

                        - **참여자가 한 명이라도 존재하면 삭제할 수 없습니다.** (400 반환)
                        - 삭제 전 참여자 목록을 확인한 후 호출하세요.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "삭제 성공"),
                        @ApiResponse(responseCode = "400", description = "참여자가 존재하여 삭제 불가", content = @Content),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        @DeleteMapping("/{challengeId}")
        void deleteChallenge(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

        @Operation(summary = "챌린지 목록 조회", description = """
                        챌린지 목록을 조회합니다.

                        - **status**: 챌린지 진행 상태 필터링 (BEFORE_START, ONGOING, COMPLETED)
                        - **sort**: 정렬 기준 (기본값: id,DESC)
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
        })
        Page<GetChallengeResponse> getChallenges(
                        @ParameterObject @ModelAttribute GetChallengesRequest request,
                        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable);

        @Operation(summary = "챌린지 단건 조회", description = "챌린지 상세 정보를 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse getChallenge(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

        @Operation(summary = "챌린지 일정 조회", description = """
                        챌린지 전체 기간의 일자별 날짜, 요일, dayIndex를 반환합니다.

                        - 주말(토/일)은 `dayIndex: 0`으로 반환됩니다.
                        - `dayIndex`는 startDate 기준 경과 일수 + 1 입니다. (주말 포함 달력 기준)

                        **응답 예시:**
                        ```json
                        [
                          { "date": "2024-01-01", "dayOfWeek": "MONDAY", "dayIndex": 1 },
                          { "date": "2024-01-06", "dayOfWeek": "SATURDAY", "dayIndex": 0 },
                          { "date": "2024-01-08", "dayOfWeek": "MONDAY", "dayIndex": 8 }
                        ]
                        ```
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "조회 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        List<GetChallengeDayResponse> getChallengeSchedule(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

        @Operation(summary = "챌린지 참여자 목록 조회", description = """
                        챌린지 별 참여자 목록을 조회합니다.

                        **Frontend Tip:**
                        - **teamId 필터링**: 특정 팀의 멤버만 보고 싶을 때 사용하세요.
                        - **hasTeam 필터링**: `false`로 설정하면 '팀 미배정자'만 조회할 수 있습니다. (수동 배정 대상 찾기에 유용)
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        Page<GetChallengeParticipantResponse> getChallengeParticipants(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @ParameterObject @ModelAttribute GetChallengeParticipantsRequest request,
                        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable);

        @Operation(summary = "챌린지 팀 목록 조회", description = """
                        챌린지에 포함된 모든 팀 목록을 조회합니다.

                        **Frontend Tip:**
                        - 수동 팀 배정 시 **팀 선택 드롭다운**을 구성하는 데 사용하세요.
                        - `id` (팀 ID)와 `progress` (진행도) 정보를 제공합니다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        List<GetChallengeTeamResponse> getChallengeTeams(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

        @Operation(summary = "챌린지 참여자 팀 변경", description = """
                        챌린지 참여자의 팀을 수동으로 변경합니다.

                        **Frontend Tip:**
                        - `challengeTeamId`는 반드시 **해당 챌린지에 속한 팀의 ID**여야 합니다. (팀 목록 조회 API 활용)
                        - 유효하지 않은 팀 ID를 보낼 경우 `400 Bad Request` 에러가 발생하므로, 에러 메시지를 사용자에게 보여주세요.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "변경 성공"),
                        @ApiResponse(responseCode = "400", description = "잘못된 입력 (유효하지 않은 팀, 현재 팀과 동일 등)", content = @Content),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지/팀/참여자", content = @Content)
        })
        @PatchMapping("/{challengeId}/participants/{memberId}/team")
        void updateParticipantTeam(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @Parameter(description = "참여자 ID (Member ID)", required = true) @PathVariable Long memberId,
                        @RequestBody @Valid UpdateParticipantTeamRequest request);

        @Operation(summary = "챌린지 생존자 쉴드 일괄 지급", description = """
                        특정 챌린지의 모든 생존 참여자에게 쉴드를 일괄 지급합니다.
                        - DB 부하 최소화를 위해 벌크 업데이트(Bulk Update) 방식으로 처리됩니다.
                        - 지급할 쉴드 개수를 지정할 수 있습니다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "지급 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        @PostMapping("/{challengeId}/participants/shield")
        void grantShield(
                        @Parameter(description = "챌린지 ID", required = true) @Positive(message = "id는 1 이상의 값이어야 합니다.") @PathVariable Long challengeId,
                        @RequestBody @Valid GrantShieldRequest request
        );

        @Operation(summary = "챌린지 팀 자동 배정", description = """
                        챌린지 참여자를 대상으로 팀을 자동으로 배정합니다.
                        - 목표 팀원 수: 15명
                        - 전체 참여자를 무작위로 섞은 후, 균등하게 배분합니다.

                        **Frontend Tip:**
                        - 이미 팀이 배정된 상태에서 호출하면 **기존 배정이 초기화되고 재배정**됩니다. (주의 메시지 필요)
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "배정 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        void assignTeams(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @RequestBody @Valid AssignTeamsRequest request);

        @Operation(summary = "챌린지 팀 일괄 생성", description = "지정된 개수만큼 챌린지 팀을 일괄 생성합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "생성 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
        })
        @PostMapping("/{challengeId}/teams")
        void createChallengeTeams(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @RequestBody @Valid CreateChallengeTeamsRequest request);

        @Operation(summary = "챌린지 팀 삭제", description = """
                        챌린지 팀을 삭제합니다.

                        **Frontend Tip:**
                        - 삭제 시 해당 팀에 속해있던 참여자들은 자동으로 **'팀 미배정'** 상태가 됩니다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "삭제 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지/팀", content = @Content)
        })
        @DeleteMapping("/{challengeId}/teams/{teamId}")
        void deleteChallengeTeam(
                        @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
                        @Parameter(description = "챌린지 팀 ID", required = true) @PathVariable Long teamId);
}
