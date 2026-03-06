package me.bombom.api.v1.article.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.article.dto.CreatePreviousArticleRequest;
import me.bombom.api.v1.article.dto.GetPreviousArticleResponse;
import me.bombom.api.v1.article.dto.UpdatePreviousArticleRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "PreviousArticle", description = "지난 뉴스레터 아티클 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface PreviousArticleControllerApi {

    @Operation(
            summary = "지난 아티클 추가",
            description = """
                    뉴스레터에 지난 아티클을 추가합니다.<br><br>
                    **사용자 입력 필드**<br>
                    - `title`: 아티클 제목<br>
                    - `contents`: HTML 원본 (mediumtext)<br>
                    - `arrivedDateTime`: 발행 일시<br>
                    - `isFixed`: 고정 아티클 여부<br><br>
                    **자동 계산 필드** (contents HTML 파싱)<br>
                    - `contentsSummary`: 텍스트 추출 후 앞 100자<br>
                    - `expectedReadTime`: 단어 수 / 200 (최소 1분)"""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "아티클 생성 성공"),
            @ApiResponse(responseCode = "400", description = "필수 값 누락 또는 공백", content = @Content),
            @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
    })
    void createPreviousArticle(
            @Parameter(description = "뉴스레터 ID", required = true) @PathVariable Long newsletterId,
            @Valid @RequestBody CreatePreviousArticleRequest request
    );

    @Operation(
            summary = "지난 아티클 목록 조회",
            description = "뉴스레터에 등록된 지난 아티클 전체 목록을 조회합니다."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터를 찾을 수 없음", content = @Content)
    })
    List<GetPreviousArticleResponse> getPreviousArticles(
            @Parameter(description = "뉴스레터 ID", required = true) @PathVariable Long newsletterId
    );

    @Operation(
            summary = "지난 아티클 상세 조회",
            description = """
                    지난 아티클 상세 정보를 조회합니다.<br>
                    아티클이 해당 뉴스레터에 속하지 않으면 404를 반환합니다."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터 또는 아티클을 찾을 수 없음", content = @Content)
    })
    GetPreviousArticleResponse getPreviousArticle(
            @Parameter(description = "뉴스레터 ID", required = true) @PathVariable Long newsletterId,
            @Parameter(description = "아티클 ID", required = true) @PathVariable Long id
    );

    @Operation(
            summary = "지난 아티클 수정",
            description = """
                    지난 아티클 정보를 부분 수정합니다. (null 필드는 변경하지 않음)<br><br>
                    **주의:** `contents`를 변경하면 `contentsSummary`와 `expectedReadTime`이 자동 재계산됩니다."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터 또는 아티클을 찾을 수 없음", content = @Content)
    })
    void updatePreviousArticle(
            @Parameter(description = "뉴스레터 ID", required = true) @PathVariable Long newsletterId,
            @Parameter(description = "아티클 ID", required = true) @PathVariable Long id,
            @RequestBody UpdatePreviousArticleRequest request
    );

    @Operation(
            summary = "지난 아티클 삭제",
            description = """
                    지난 아티클을 삭제합니다.<br>
                    아티클이 해당 뉴스레터에 속하지 않으면 404를 반환합니다."""
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "뉴스레터 또는 아티클을 찾을 수 없음", content = @Content)
    })
    void deletePreviousArticle(
            @Parameter(description = "뉴스레터 ID", required = true) @PathVariable Long newsletterId,
            @Parameter(description = "아티클 ID", required = true) @PathVariable Long id
    );
}
