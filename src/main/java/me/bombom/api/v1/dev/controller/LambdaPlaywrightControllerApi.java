package me.bombom.api.v1.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceRequest;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceResponse;

@Tag(name = "Dev Lambda Playwright API", description = "Lambda Playwright 스크립트 관리용 API (운영 환경 사용 금지)")
public interface LambdaPlaywrightControllerApi {

    @Operation(summary = "Lambda Playwright 스크립트 조회", description = "GitHub에 저장된 index.js 내용을 조회합니다.")
    LambdaPlaywrightSourceResponse getSource();

    @Operation(summary = "Lambda Playwright 스크립트 수정", description = "GitHub에 저장된 index.js 내용을 수정하고 커밋합니다.")
    void updateSource(LambdaPlaywrightSourceRequest request);
}
