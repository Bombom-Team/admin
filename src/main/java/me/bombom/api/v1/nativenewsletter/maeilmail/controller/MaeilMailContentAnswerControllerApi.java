package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.CreateMaeilMailContentAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.UpdateMaeilMailContentAnswerRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@Tag(name = "MaeilMailContentAnswer", description = "매일메일 콘텐츠 답변 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)"),
        @ApiResponse(responseCode = "403", description = "권한 없음")
})
public interface MaeilMailContentAnswerControllerApi {

    @Operation(summary = "매일메일 콘텐츠 답변 목록 조회", description = """
            매일메일 콘텐츠 답변 목록을 조회합니다.

            **필터 조건 (모두 선택):**
            - `track`: 트랙 필터링 (BE, FE)
            - `title`: 콘텐츠 제목 검색 (부분 일치)

            **페이지네이션:**
            - `page`: 페이지 번호 (0부터 시작, 기본값: 0)
            - `size`: 페이지 크기 (기본값: 20)
            - `sort`: 정렬 기준 (기본값: id, ASC)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            @ParameterObject @ModelAttribute GetMaeilMailContentAnswersRequest request,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable);

    @Operation(summary = "매일메일 콘텐츠 답변 단건 조회", description = "매일메일 콘텐츠 답변 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 답변", content = @Content)
    })
    GetMaeilMailContentAnswerDetailResponse getContentAnswer(
            @Parameter(description = "답변 ID", required = true) @PathVariable Long id);

    @Operation(summary = "매일메일 콘텐츠 답변 생성", description = """
            매일메일 콘텐츠 답변을 생성합니다.

            - 콘텐츠 1개당 답변은 1개만 등록할 수 있습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "이미 답변이 존재하는 콘텐츠", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 콘텐츠", content = @Content)
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    void createContentAnswer(@RequestBody @Valid CreateMaeilMailContentAnswerRequest request);

    @Operation(summary = "매일메일 콘텐츠 답변 수정", description = "매일메일 콘텐츠 답변을 수정합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 답변", content = @Content)
    })
    @PatchMapping("/{id}")
    void updateContentAnswer(
            @Parameter(description = "답변 ID", required = true) @PathVariable Long id,
            @RequestBody @Valid UpdateMaeilMailContentAnswerRequest request);

    @Operation(summary = "매일메일 콘텐츠 답변 삭제", description = "매일메일 콘텐츠 답변을 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 답변", content = @Content)
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteContentAnswer(
            @Parameter(description = "답변 ID", required = true) @PathVariable Long id);
}
