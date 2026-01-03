package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/challenges")
public class ChallengeController implements ChallengeControllerApi {

    private final ChallengeService challengeService;

    @Override
    @GetMapping
    public Page<GetChallengeResponse> getChallenges(
            @ModelAttribute GetChallengesRequest request,
            @PageableDefault(sort = "id", direction = Direction.ASC) Pageable pageable) {
        return challengeService.getChallenges(request, pageable);
    }

    @Override
    @GetMapping("/{challengeId}")
    public me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse getChallenge(@PathVariable Long challengeId) {
        return challengeService.getChallenge(challengeId);
    }

    @Override
    @GetMapping("/{challengeId}/participants")
    public Page<GetChallengeParticipantResponse> getChallengeParticipants(
            @PathVariable Long challengeId,
            @ModelAttribute GetChallengeParticipantsRequest request,
            @PageableDefault(sort = "id", direction = Direction.ASC) Pageable pageable) {
        return challengeService.getChallengeParticipants(challengeId, request, pageable);
    }

    @Override
    @GetMapping("/{challengeId}/teams")
    public List<GetChallengeTeamResponse> getChallengeTeams(@PathVariable Long challengeId) {
        return challengeService.getChallengeTeams(challengeId);
    }

    @Override
    @PostMapping("/{challengeId}/teams/assignment")
    public void assignTeams(
            @PathVariable Long challengeId,
            @Valid @RequestBody AssignTeamsRequest request
    ) {
        challengeService.assignTeams(challengeId, request);
    }

    @Override
    @PatchMapping("/{challengeId}/participants/{memberId}/team")
    public void updateParticipantTeam(
            @PathVariable Long challengeId,
            @PathVariable Long memberId,
            @Valid @RequestBody UpdateParticipantTeamRequest request) {
        challengeService.updateParticipantTeam(challengeId, memberId, request);
    }
}
