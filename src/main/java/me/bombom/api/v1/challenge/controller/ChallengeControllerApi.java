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
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Challenge", description = "챌린지 관리 API")
@ApiResponses({
                @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
                @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface ChallengeControllerApi {

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
