package me.bombom.api.v1.subscribe.controller;

import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "구독 해지 패턴 생성")
    @PostMapping
    void createUnsubscribePattern(@RequestBody @Valid UnsubscribePatternRequest request);

    @Operation(summary = "구독 해지 패턴 목록 조회")
    @GetMapping
    List<UnsubscribePatternResponse> getUnsubscribePatterns();

    @Operation(summary = "구독 해지 패턴 상세 조회")
    @GetMapping("/{id}")
    UnsubscribePatternResponse getUnsubscribePattern(@PathVariable Long id);

    @Operation(summary = "구독 해지 패턴 수정")
    @PatchMapping("/{id}")
    void updateUnsubscribePattern(
            @PathVariable Long id,
            @RequestBody @Valid UnsubscribePatternUpdateRequest request);
}
