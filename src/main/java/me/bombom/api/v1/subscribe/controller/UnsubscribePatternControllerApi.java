package me.bombom.api.v1.subscribe.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternUpdateRequest;
import me.bombom.api.v1.subscribe.dto.response.UnsubscribePatternResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Unsubscribe Pattern", description = "구독 해지 패턴 관리 API")
public interface UnsubscribePatternControllerApi {

    @Operation(summary = "구독 해지 패턴 생성", description = "새로운 구독 해지 자동화를 위한 패턴을 생성합니다. patternKey는 고유해야 합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 또는 중복된 patternKey")
    })
    @PostMapping
    void createUnsubscribePattern(@RequestBody @Valid UnsubscribePatternRequest request);

    @Operation(summary = "구독 해지 패턴 목록 조회", description = "시스템에 등록된 모든 구독 해지 패턴 목록을 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    @GetMapping
    List<UnsubscribePatternResponse> getUnsubscribePatterns();

    @Operation(summary = "구독 해지 패턴 상세 조회", description = "ID를 통해 특정 구독 해지 패턴의 상세 정보를 조회합니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 패턴 ID")
    })
    @GetMapping("/{id}")
    UnsubscribePatternResponse getUnsubscribePattern(
            @Parameter(description = "패턴 식별자 ID", example = "1") @PathVariable Long id);

    @Operation(summary = "구독 해지 패턴 수정", description = "기존 패턴의 내용을 수정합니다. patternKey는 수정이 불가능하며 patternValue만 변경할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 패턴 ID")
    })
    @PatchMapping("/{id}")
    void updateUnsubscribePattern(
            @Parameter(description = "패턴 식별자 ID", example = "1") @PathVariable Long id,
            @RequestBody @Valid UnsubscribePatternUpdateRequest request);
}
