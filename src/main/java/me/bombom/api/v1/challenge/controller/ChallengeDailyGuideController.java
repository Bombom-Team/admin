package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.service.ChallengeDailyGuideService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/challenges/{challengeId}/daily-guides")
public class ChallengeDailyGuideController implements ChallengeDailyGuideControllerApi {

    private final ChallengeDailyGuideService dailyGuideService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void create(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreateDailyGuideRequest request
    ) {
        dailyGuideService.create(challengeId, request);
    }

    @Override
    @GetMapping
    public List<GetDailyGuideResponse> getDailyGuides(@PathVariable Long challengeId) {
        return dailyGuideService.getDailyGuides(challengeId);
    }

    @Override
    @GetMapping("/{guideId}")
    public GetDailyGuideResponse getDailyGuide(
            @PathVariable Long challengeId,
            @PathVariable Long guideId
    ) {
        return dailyGuideService.getDailyGuide(challengeId, guideId);
    }

    @Override
    @PatchMapping("/{guideId}")
    public void update(
            @PathVariable Long challengeId,
            @PathVariable Long guideId,
            @Valid @RequestBody UpdateDailyGuideRequest request
    ) {
        dailyGuideService.update(challengeId, guideId, request);
    }

    @Override
    @DeleteMapping("/{guideId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(
            @PathVariable Long challengeId,
            @PathVariable Long guideId
    ) {
        dailyGuideService.delete(challengeId, guideId);
    }
}
