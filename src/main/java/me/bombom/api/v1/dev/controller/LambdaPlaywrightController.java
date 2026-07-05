package me.bombom.api.v1.dev.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceRequest;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceResponse;
import me.bombom.api.v1.dev.service.LambdaPlaywrightService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/lambda-playwright")
public class LambdaPlaywrightController implements LambdaPlaywrightControllerApi {

    private final LambdaPlaywrightService lambdaPlaywrightService;

    @Override
    @GetMapping("/source")
    public LambdaPlaywrightSourceResponse getSource() {
        return new LambdaPlaywrightSourceResponse(lambdaPlaywrightService.getSource());
    }

    @Override
    @PutMapping("/source")
    public void updateSource(@Valid @RequestBody LambdaPlaywrightSourceRequest request) {
        lambdaPlaywrightService.updateSource(request);
    }
}
