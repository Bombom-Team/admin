package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지")
        })
        me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse getChallenge(
                        @PathVariable Long challengeId);

        @Operation(summary = "챌린지 참여자 목록 조회", description = "챌린지 별 참여자 목록을 조회합니다. (팀별 필터링, 팀 미배정자 조회 지원)")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
        })
        Page<GetChallengeParticipantResponse> getChallengeParticipants(
                        @PathVariable Long challengeId,
                        @ParameterObject @ModelAttribute GetChallengeParticipantsRequest request,
                        @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable);

        @Operation(summary = "챌린지 팀 목록 조회", description = "챌린지에 포함된 모든 팀 목록을 조회합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "목록 조회 성공")
        })
        List<GetChallengeTeamResponse> getChallengeTeams(@PathVariable Long challengeId);

        @Operation(summary = "챌린지 참여자 팀 변경", description = "챌린지 참여자의 팀을 수동으로 변경합니다.")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "변경 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지/팀/참여자")
        })
        @PatchMapping("/{challengeId}/participants/{memberId}/team")
        void updateParticipantTeam(
                        @PathVariable Long challengeId,
                        @PathVariable Long memberId,
                        @RequestBody @Valid UpdateParticipantTeamRequest request
        );

        @Operation(summary = "챌린지 팀 자동 배정", description = """
                        챌린지 참여자를 대상으로 팀을 자동으로 배정합니다.
                        - 목표 팀원 수: 15명
                        - 전체 참여자를 무작위로 섞은 후, 균등하게 배분합니다.
                        """)
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "배정 성공"),
                        @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지")
        })
        void assignTeams(@PathVariable Long challengeId);
}
