package me.bombom.api.v1.challenge.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import me.bombom.api.v1.challenge.service.ChallengeDailyGuideService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/challenges/{challengeId}/daily-guides")
public class ChallengeDailyGuideController implements ChallengeDailyGuideControllerApi {

    private final ChallengeDailyGuideService dailyGuideService;

    @Override
    @GetMapping("/images")
    public List<String> getChallengeImages() {
        return dailyGuideService.getChallengeImages();
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void create(
            @PathVariable Long challengeId,
            @RequestPart("image") MultipartFile image,
            @Valid @RequestPart("request") CreateDailyGuideRequest request
    ) {
        dailyGuideService.create(challengeId, image, request);
    }

    @Override
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public void createFromImage(
            @PathVariable Long challengeId,
            @Valid @RequestBody CreateDailyGuideFromImageRequest request
    ) {
        dailyGuideService.createFromImage(challengeId, request);
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
    @PatchMapping(value = "/{guideId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public void update(
            @PathVariable Long challengeId,
            @PathVariable Long guideId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") UpdateDailyGuideRequest request
    ) {
        dailyGuideService.update(challengeId, guideId, image, request);
    }

    @Override
    @PatchMapping(value = "/{guideId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public void updateFromImage(
            @PathVariable Long challengeId,
            @PathVariable Long guideId,
            @Valid @RequestBody UpdateDailyGuideFromImageRequest request
    ) {
        dailyGuideService.updateFromImage(challengeId, guideId, request);
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
