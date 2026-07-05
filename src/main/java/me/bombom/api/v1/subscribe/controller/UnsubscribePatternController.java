package me.bombom.api.v1.subscribe.controller;

import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternUpdateRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternType;
import me.bombom.api.v1.subscribe.dto.response.UnsubscribePatternResponse;
import me.bombom.api.v1.subscribe.service.UnsubscribePatternService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/unsubscribe-patterns")
public class UnsubscribePatternController implements UnsubscribePatternControllerApi {

    private final UnsubscribePatternService unsubscribePatternService;

    @Override
    @GetMapping
    public List<UnsubscribePatternResponse> getUnsubscribePatterns(
            @RequestParam(defaultValue = "AUTO_UNSUBSCRIBE") UnsubscribePatternType patternType
    ) {
        return unsubscribePatternService.getUnsubscribePatterns(patternType);
    }

    @Override
    @GetMapping("/{id}")
    public UnsubscribePatternResponse getUnsubscribePattern(@PathVariable Long id) {
        return unsubscribePatternService.getUnsubscribePattern(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createUnsubscribePattern(@RequestBody @Valid UnsubscribePatternRequest request) {
        unsubscribePatternService.createUnsubscribePattern(request);
    }

    @Override
    @PatchMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateUnsubscribePattern(
            @PathVariable Long id,
            @RequestBody @Valid UnsubscribePatternUpdateRequest request
    ) {
        unsubscribePatternService.updateUnsubscribePattern(id, request);
    }
}
