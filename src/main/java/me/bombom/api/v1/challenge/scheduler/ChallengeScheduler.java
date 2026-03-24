package me.bombom.api.v1.challenge.scheduler;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.service.ChallengeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChallengeScheduler {

    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String ASIA_SEOUL = "Asia/Seoul";
    private final ChallengeService challengeService;

    @Scheduled(cron = DAILY_CRON, zone = ASIA_SEOUL)
    public void assignTeamsForTodayStartChallenges() {
        challengeService.assignTeamsForTodayStartChallenges();
    }
}
