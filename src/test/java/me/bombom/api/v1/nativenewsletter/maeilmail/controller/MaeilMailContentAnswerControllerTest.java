package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailContentAnswerService;
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

    private GetMaeilMailContentAnswerResponse createResponse() {
        return new GetMaeilMailContentAnswerResponse(1L, "자바 기초", MaeilMailTrack.BE);
    }

    private GetMaeilMailContentAnswerDetailResponse createDetailResponse() {
        return new GetMaeilMailContentAnswerDetailResponse(1L, "자바 기초", MaeilMailTrack.BE, "테스트 답변");
    }
}
