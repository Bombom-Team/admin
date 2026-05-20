package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.ModelAttribute;

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
            - `sort`: 정렬 기준 (기본값: id,DESC)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "목록 조회 성공")
    })
    Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            @ParameterObject @ModelAttribute GetMaeilMailContentAnswersRequest request,
            @ParameterObject @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable);
}
