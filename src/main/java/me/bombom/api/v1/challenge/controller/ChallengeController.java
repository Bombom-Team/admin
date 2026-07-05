package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.AssignTeamsRequest;
import me.bombom.api.v1.challenge.dto.CreateChallengeTeamsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeDayResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeDetailResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeTeamResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.UpdateParticipantTeamRequest;
import me.bombom.api.v1.challenge.dto.request.CreateChallengeRequest;
import me.bombom.api.v1.challenge.dto.request.GrantShieldRequest;
import me.bombom.api.v1.challenge.dto.request.UpdateChallengeRequest;
import me.bombom.api.v1.challenge.service.ChallengeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/challenges")
public class ChallengeController implements ChallengeControllerApi {

    private final ChallengeService challengeService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createChallenge(@Valid @RequestBody CreateChallengeRequest request) {
        challengeService.createChallenge(request);
    }

    @Override
    @PatchMapping("/{challengeId}")
    public void updateChallenge(
            @PathVariable Long challengeId,
            @Valid @RequestBody UpdateChallengeRequest request
    ) {
        challengeService.updateChallenge(challengeId, request);
    }

    @Override
    @DeleteMapping("/{challengeId}")
    public void deleteChallenge(@PathVariable Long challengeId) {
        challengeService.deleteChallenge(challengeId);
    }

    @Override
    @GetMapping
    public Page<GetChallengeResponse> getChallenges(
            @ModelAttribute GetChallengesRequest request,
            @PageableDefault(sort = "id", direction = Direction.ASC) Pageable pageable) {
        return challengeService.getChallenges(request, pageable);
    }

    @Override
    @GetMapping("/{challengeId}")
    public GetChallengeDetailResponse getChallenge(@PathVariable Long challengeId) {
        return challengeService.getChallenge(challengeId);
    }

    @Override
    @GetMapping("/{challengeId}/schedule")
    public List<GetChallengeDayResponse> getChallengeSchedule(@PathVariable Long challengeId) {
        return challengeService.getChallengeSchedule(challengeId);
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
    @PostMapping("/{challengeId}/teams")
    public void createChallengeTeams(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreateChallengeTeamsRequest request
    ) {
        challengeService.createChallengeTeams(challengeId, request);
    }

    @Override
    @DeleteMapping("/{challengeId}/teams/{teamId}")
    public void deleteChallengeTeam(
            @PathVariable Long challengeId,
            @PathVariable Long teamId
    ) {
        challengeService.deleteChallengeTeam(challengeId, teamId);
    }

    @Override
    @PostMapping("/{challengeId}/teams/assignment")
    public void assignTeams(
            @PathVariable Long challengeId,
            @Valid @RequestBody AssignTeamsRequest request) {
        challengeService.assignTeams(challengeId, request);
    }

    @Override
    @PatchMapping("/{challengeId}/participants/{participantId}/team")
    public void updateParticipantTeam(
            @PathVariable Long challengeId,
            @PathVariable Long participantId,
            @Valid @RequestBody UpdateParticipantTeamRequest request) {
        challengeService.updateParticipantTeam(challengeId, participantId, request);
    }

    @Override
    @PostMapping("/{challengeId}/participants/shield")
    public void grantShield(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long challengeId,
            @Valid @RequestBody GrantShieldRequest request) {
        challengeService.grantShield(challengeId, request);
    }
}
