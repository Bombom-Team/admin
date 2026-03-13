package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.newsletter.dto.CreateCategoryRequest;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import me.bombom.api.v1.newsletter.dto.UpdateCategoryRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Category", description = "카테고리 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface CategoryControllerApi {

    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "카테고리 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값 또는 중복된 이름", content = @Content)
    })
    void createCategory(@Valid @RequestBody CreateCategoryRequest request);

    @Operation(summary = "카테고리 목록 조회", description = "전체 카테고리 목록을 조회합니다.")
    @ApiResponse(responseCode = "200", description = "조회 성공")
    List<GetCategoryResponse> getCategories();

    @Operation(summary = "카테고리 수정", description = "카테고리 이름을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "중복된 이름", content = @Content),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content)
    })
    void updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request);

    @Operation(summary = "카테고리 삭제", description = "카테고리를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음", content = @Content)
    })
    void deleteCategory(@PathVariable Long id);
}
