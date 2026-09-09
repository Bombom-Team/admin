package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "InquiryCategory", description = "문의 카테고리 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface InquiryCategoryControllerApi {

    @Operation(summary = "문의 카테고리 목록 조회")
    @ApiResponses({ @ApiResponse(responseCode = "200", description = "목록 조회 성공") })
    List<InquiryCategoryResponse> getCategories();

    @Operation(summary = "문의 카테고리 생성")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이름 중복 등 잘못된 요청", content = @Content)
    })
    void createCategory(@Valid @RequestBody CreateInquiryCategoryRequest request);

    @Operation(summary = "문의 카테고리 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리", content = @Content)
    })
    void updateCategory(
            @Parameter(description = "수정할 카테고리 ID") @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateInquiryCategoryRequest request);

    @Operation(summary = "문의 카테고리 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "400", description = "사용 중인 카테고리", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 카테고리", content = @Content)
    })
    void deleteCategory(@Parameter(description = "삭제할 카테고리 ID") @PathVariable @Positive Long id);
}
