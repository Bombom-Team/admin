package me.bombom.api.v1.challenge.fixture;

import static org.instancio.Select.field;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import org.instancio.Instancio;

public class ChallengeFixture {

    public static Challenge createChallenge() {
        LocalDate startDate = LocalDate.now().plusDays(1);
        return Instancio.of(Challenge.class)
                .set(field(Challenge::getId), null)
                .set(field(Challenge::getName), "Test Challenge")
                .set(field(Challenge::getGeneration), 1)
                .set(field(Challenge::getStartDate), startDate)
                .set(field(Challenge::getEndDate), startDate.plusDays(30))
                .set(field(Challenge::getTotalDays), 30)
                .create();
    }

    public static ChallengeParticipant createParticipant(Long challengeId, Long memberId) {
        return Instancio.of(ChallengeParticipant.class)
                .set(field(ChallengeParticipant::getId), null)
                .set(field(ChallengeParticipant::getChallengeId), challengeId)
                .set(field(ChallengeParticipant::getMemberId), memberId)
                .set(field(ChallengeParticipant::getChallengeTeamId), null)
                .set(field(ChallengeParticipant::getCompletedDays), 0)
                .set(field(ChallengeParticipant::isSurvived), true)
                .set(field(ChallengeParticipant::getShield), 0)
                .create();
    }

    public static ChallengeParticipant createParticipant(Long challengeId, Long memberId, boolean isSurvived,
            int shield) {
        return Instancio.of(ChallengeParticipant.class)
                .set(field(ChallengeParticipant::getId), null)
                .set(field(ChallengeParticipant::getChallengeId), challengeId)
                .set(field(ChallengeParticipant::getMemberId), memberId)
                .set(field(ChallengeParticipant::isSurvived), isSurvived)
                .set(field(ChallengeParticipant::getShield), shield)
                .create();
    }

    public static ChallengeTeam createTeam(Long challengeId) {
        return Instancio.of(ChallengeTeam.class)
                .set(field(ChallengeTeam::getId), null)
                .set(field(ChallengeTeam::getChallengeId), challengeId)
                .set(field(ChallengeTeam::getProgress), 0)
                .create();
    }

    public static ChallengeTeam createTeam(Long challengeId, int progress) {
        return Instancio.of(ChallengeTeam.class)
                .set(field(ChallengeTeam::getId), null)
                .set(field(ChallengeTeam::getChallengeId), challengeId)
                .set(field(ChallengeTeam::getProgress), progress)
                .create();
    }
}
