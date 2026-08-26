package me.bombom.api.v1.faq.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import me.bombom.api.v1.faq.dto.CreateFaqRequest;
import me.bombom.api.v1.faq.dto.GetFaqDetailResponse;
import me.bombom.api.v1.faq.dto.GetFaqResponse;
import me.bombom.api.v1.faq.dto.UpdateFaqRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Faq", description = "FAQ 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface FaqControllerApi {

    @Operation(summary = "FAQ 목록 조회", description = "FAQ 목록을 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    Page<GetFaqResponse> getFaqs(Pageable pageable);

    @Operation(summary = "FAQ 상세 조회", description = "FAQ 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "상세 조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 FAQ", content = @Content)
    })
    GetFaqDetailResponse getFaq(
            @Parameter(description = "조회할 FAQ ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);

    @Operation(summary = "FAQ 생성", description = "새로운 FAQ를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "FAQ 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    void createFaq(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "faqCategory: [INTRODUCTION, FEATURE, ACCOUNT, NEWSLETTER, ETC] 중 하나 선택") @Valid @RequestBody CreateFaqRequest request);

    @Operation(summary = "FAQ 수정", description = "기존 FAQ를 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "FAQ 수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 FAQ", content = @Content)
    })
    void updateFaq(
            @Parameter(description = "수정할 FAQ ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @Valid @RequestBody UpdateFaqRequest request);

    @Operation(summary = "FAQ 삭제", description = "기존 FAQ를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "FAQ 삭제 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    void deleteFaq(
            @Parameter(description = "삭제할 FAQ ID") @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id);
}
