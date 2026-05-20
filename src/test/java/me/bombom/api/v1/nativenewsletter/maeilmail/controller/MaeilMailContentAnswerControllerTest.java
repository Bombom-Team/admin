package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailContentAnswerService;
import org.springframework.http.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = MaeilMailContentAnswerController.class)
class MaeilMailContentAnswerControllerTest extends ControllerTestSupport {

    @MockitoBean
    private MaeilMailContentAnswerService contentAnswerService;

    @Test
    void 답변_목록을_조회한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswers(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(createResponse())));

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers"))
                .andExpect(status().isOk());
    }

    @Test
    void track_필터로_답변_목록을_조회한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswers(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(createResponse())));

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers")
                        .param("track", "BE"))
                .andExpect(status().isOk());
    }

    @Test
    void title_검색으로_답변_목록을_조회한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswers(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(createResponse())));

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers")
                        .param("title", "자바"))
                .andExpect(status().isOk());
    }

    @Test
    void track과_title로_답변_목록을_조회한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswers(any(), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(createResponse())));

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers")
                        .param("track", "BE")
                        .param("title", "자바"))
                .andExpect(status().isOk());
    }

    @Test
    void 유효하지_않은_track_값이면_400을_반환한다() throws Exception {
        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers")
                        .param("track", "INVALID"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 답변_단건을_조회한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswer(any()))
                .willReturn(createDetailResponse());

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers/1"))
                .andExpect(status().isOk());
    }

    @Test
    void 존재하지_않는_답변_조회_시_404를_반환한다() throws Exception {
        // given
        given(contentAnswerService.getContentAnswer(any()))
                .willThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/admin/api/v1/maeil-mail/content-answers/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void 답변을_생성한다() throws Exception {
        // given
        String requestBody = """
                {
                    "track": "BE",
                    "title": "자바 기초",
                    "content": "자바 콘텐츠 내용",
                    "contentsText": "자바 콘텐츠 텍스트",
                    "contentsSummary": "자바 요약",
                    "expectedReadTime": 5,
                    "answer": "테스트 답변입니다."
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/maeil-mail/content-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void 답변_생성_시_필수_필드가_없으면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {
                    "track": "BE",
                    "answer": "테스트 답변입니다."
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/maeil-mail/content-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 답변_생성_시_answer가_blank면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {
                    "track": "BE",
                    "title": "자바 기초",
                    "content": "자바 콘텐츠 내용",
                    "contentsText": "자바 콘텐츠 텍스트",
                    "contentsSummary": "자바 요약",
                    "expectedReadTime": 5,
                    "answer": ""
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/maeil-mail/content-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_track으로_생성_시_404를_반환한다() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                .when(contentAnswerService).createContentAnswer(any());
        String requestBody = """
                {
                    "track": "BE",
                    "title": "자바 기초",
                    "content": "자바 콘텐츠 내용",
                    "contentsText": "자바 콘텐츠 텍스트",
                    "contentsSummary": "자바 요약",
                    "expectedReadTime": 5,
                    "answer": "테스트 답변입니다."
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/maeil-mail/content-answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void 답변을_수정한다() throws Exception {
        // given
        String requestBody = """
                {
                    "answer": "수정된 답변입니다."
                }
                """;

        // when & then
        mockMvc.perform(patch("/admin/api/v1/maeil-mail/content-answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void 답변_수정_시_answer가_blank면_400을_반환한다() throws Exception {
        // given
        String requestBody = """
                {
                    "answer": ""
                }
                """;

        // when & then
        mockMvc.perform(patch("/admin/api/v1/maeil-mail/content-answers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 존재하지_않는_답변_수정_시_404를_반환한다() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                .when(contentAnswerService).updateContentAnswer(any(), any());
        String requestBody = """
                {
                    "answer": "수정된 답변입니다."
                }
                """;

        // when & then
        mockMvc.perform(patch("/admin/api/v1/maeil-mail/content-answers/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isNotFound());
    }

    @Test
    void 답변을_삭제한다() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/maeil-mail/content-answers/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void 존재하지_않는_답변_삭제_시_404를_반환한다() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND))
                .when(contentAnswerService).deleteContentAnswer(any());

        // when & then
        mockMvc.perform(delete("/admin/api/v1/maeil-mail/content-answers/999"))
                .andExpect(status().isNotFound());
    }

    private GetMaeilMailContentAnswerResponse createResponse() {
        return new GetMaeilMailContentAnswerResponse(1L, "자바 기초", MaeilMailTrack.BE);
    }

    private GetMaeilMailContentAnswerDetailResponse createDetailResponse() {
        return new GetMaeilMailContentAnswerDetailResponse(1L, "자바 기초", MaeilMailTrack.BE, "테스트 답변");
    }
}
