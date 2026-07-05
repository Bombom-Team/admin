package me.bombom.api.v1.article.controller;

import me.bombom.api.v1.article.dto.CreatePreviousArticleRequest;
import me.bombom.api.v1.article.dto.GetPreviousArticleResponse;
import me.bombom.api.v1.article.dto.UpdatePreviousArticleRequest;
import me.bombom.api.v1.article.service.PreviousArticleService;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

@WebMvcTest(PreviousArticleController.class)
class PreviousArticleControllerTest extends ControllerTestSupport {

    private static final String BASE_URL = "/admin/api/v1/newsletters/{newsletterId}/articles/previous";

    @MockitoBean
    protected PreviousArticleService previousArticleService;

    @Test
    @DisplayName("지난 아티클 추가 성공")
    void createPreviousArticle_success() throws Exception {
        // given
        CreatePreviousArticleRequest request = new CreatePreviousArticleRequest(
                "테스트 아티클",
                "<p>내용입니다</p>",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false
        );

        doNothing().when(previousArticleService).create(eq(1L), any(CreatePreviousArticleRequest.class));

        // when & then
        mockMvc.perform(post(BASE_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("제목이 없으면 400 에러")
    void createPreviousArticle_blankTitle() throws Exception {
        // given
        CreatePreviousArticleRequest request = new CreatePreviousArticleRequest(
                "",
                "<p>내용입니다</p>",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false
        );

        // when & then
        mockMvc.perform(post(BASE_URL, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("존재하지 않는 뉴스레터에 아티클 추가 시 404 에러")
    void createPreviousArticle_newsletterNotFound() throws Exception {
        // given
        CreatePreviousArticleRequest request = new CreatePreviousArticleRequest(
                "테스트 아티클",
                "<p>내용입니다</p>",
                LocalDateTime.of(2025, 1, 1, 9, 0),
                false
        );

        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("newsletterId", 999L))
                .when(previousArticleService).create(eq(999L), any(CreatePreviousArticleRequest.class));

        // when & then
        mockMvc.perform(post(BASE_URL, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M003"));
    }

    @Test
    @DisplayName("지난 아티클 목록 조회 성공")
    void getPreviousArticles_success() throws Exception {
        // given
        GetPreviousArticleResponse response1 = new GetPreviousArticleResponse(
                1L, "아티클1", "<p>내용1</p>", "내용1", 1,
                LocalDateTime.of(2025, 1, 1, 9, 0), false, 1L);
        GetPreviousArticleResponse response2 = new GetPreviousArticleResponse(
                2L, "아티클2", "<p>내용2</p>", "내용2", 2,
                LocalDateTime.of(2025, 2, 1, 9, 0), true, 1L);

        given(previousArticleService.getPreviousArticles(1L))
                .willReturn(List.of(response1, response2));

        // when & then
        mockMvc.perform(get(BASE_URL, 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("지난 아티클 목록 조회 시 뉴스레터 없으면 404 에러")
    void getPreviousArticles_newsletterNotFound() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("newsletterId", 999L))
                .when(previousArticleService).getPreviousArticles(999L);

        // when & then
        mockMvc.perform(get(BASE_URL, 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M003"));
    }

    @Test
    @DisplayName("지난 아티클 상세 조회 성공")
    void getPreviousArticle_success() throws Exception {
        // given
        GetPreviousArticleResponse response = new GetPreviousArticleResponse(
                1L, "아티클1", "<p>내용1</p>", "내용1", 1,
                LocalDateTime.of(2025, 1, 1, 9, 0), false, 1L);

        given(previousArticleService.getPreviousArticle(1L, 1L)).willReturn(response);

        // when & then
        mockMvc.perform(get(BASE_URL + "/{id}", 1L, 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.title").value("아티클1"));
    }

    @Test
    @DisplayName("존재하지 않는 아티클 상세 조회 시 404 에러")
    void getPreviousArticle_notFound() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("id", 999L))
                .when(previousArticleService).getPreviousArticle(1L, 999L);

        // when & then
        mockMvc.perform(get(BASE_URL + "/{id}", 1L, 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M003"));
    }

    @Test
    @DisplayName("지난 아티클 수정 성공")
    void updatePreviousArticle_success() throws Exception {
        // given
        UpdatePreviousArticleRequest request = new UpdatePreviousArticleRequest(
                "수정된 제목", null, null, null);

        doNothing().when(previousArticleService).update(eq(1L), eq(1L), any(UpdatePreviousArticleRequest.class));

        // when & then
        mockMvc.perform(patch(BASE_URL + "/{id}", 1L, 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 아티클 수정 시 404 에러")
    void updatePreviousArticle_notFound() throws Exception {
        // given
        UpdatePreviousArticleRequest request = new UpdatePreviousArticleRequest(
                "수정된 제목", null, null, null);

        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("id", 999L))
                .when(previousArticleService).update(eq(1L), eq(999L), any(UpdatePreviousArticleRequest.class));

        // when & then
        mockMvc.perform(patch(BASE_URL + "/{id}", 1L, 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("지난 아티클 삭제 성공")
    void deletePreviousArticle_success() throws Exception {
        // given
        doNothing().when(previousArticleService).delete(1L, 1L);

        // when & then
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L, 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 아티클 삭제 시 404 에러")
    void deletePreviousArticle_notFound() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("id", 999L))
                .when(previousArticleService).delete(1L, 999L);

        // when & thenㅈ
        mockMvc.perform(delete(BASE_URL + "/{id}", 1L, 999L))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
