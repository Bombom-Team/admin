package me.bombom.api.v1.newsletter.controller;

import static org.mockito.ArgumentMatchers.any;
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

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.UpdateNewsletterRequest;
import me.bombom.api.v1.newsletter.service.NewsletterService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(NewsletterController.class)
class NewsletterControllerTest extends ControllerTestSupport {

        @MockitoBean
        protected NewsletterService newsletterService;

        @Test
        @DisplayName("뉴스레터 생성 성공")
        void createNewsletter_success() throws Exception {
                // given
                CreateNewsletterRequest request = new CreateNewsletterRequest(
                                "테스트 뉴스레터",
                                "설명입니다",
                                "http://image.url",
                                "test@email.com",
                                "경제",
                                "http://main.url",
                                "http://sub.url",
                                "매주 월요일",
                                "뉴스레터 발송자",
                                null,
                                "이메일 구독");

                doNothing().when(newsletterService).create(any(CreateNewsletterRequest.class));

                // when & then
                mockMvc.perform(post("/admin/api/v1/newsletters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("존재하지 않는 카테고리로 생성 시 400 에러")
        void createNewsletter_invalidCategory() throws Exception {
                // given
                CreateNewsletterRequest request = new CreateNewsletterRequest(
                                "테스트 뉴스레터",
                                "설명입니다",
                                "http://image.url",
                                "test@email.com",
                                "없는카테고리",
                                "http://main.url",
                                "http://sub.url",
                                "매주 월요일",
                                "뉴스레터 발송자",
                                null,
                                "이메일 구독");

                doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                .addContext("category", "없는카테고리"))
                                .when(newsletterService).create(any(CreateNewsletterRequest.class));

                // when & then
                mockMvc.perform(post("/admin/api/v1/newsletters")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("M003"))
                                .andExpect(jsonPath("$.message").value("존재하지 않는 데이터입니다."));
        }

        @Test
        @DisplayName("뉴스레터 목록 조회 성공")
        void getNewsletters_success() throws Exception {
                // given
                GetNewsletterSummaryResponse response1 = new GetNewsletterSummaryResponse(
                                1L, "뉴스레터1", "img1", "테크", "매주", 10);
                GetNewsletterSummaryResponse response2 = new GetNewsletterSummaryResponse(
                                2L, "뉴스레터2", "img2", "경제", "격주", 20);

                given(newsletterService.getNewsletters(any(GetNewslettersRequest.class)))
                                .willReturn(List.of(response1, response2));

                // when & then
                mockMvc.perform(get("/admin/api/v1/newsletters")
                                .param("sort", "LATEST"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$").isArray())
                                .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        @DisplayName("뉴스레터 상세 조회 성공")
        void getNewsletterDetail_success() throws Exception {
                // given
                GetNewsletterResponse response = new GetNewsletterResponse(
                                1L,
                                "뉴스레터1",
                                "설명1",
                                "img1",
                                "email@test.com",
                                "테크",
                                "mainUrl",
                                "subUrl",
                                "매주",
                                100,
                                "sender",
                                "prevUrl",
                                true,
                                "email");

                given(newsletterService.getNewsletterDetail(1L))
                                .willReturn(response);

                // when & then
                mockMvc.perform(get("/admin/api/v1/newsletters/{id}", 1L))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(1L))
                                .andExpect(jsonPath("$.name").value("뉴스레터1"))
                                .andExpect(jsonPath("$.subscribeCount").value(100L));
        }

        @Test
        @DisplayName("존재하지 않는 뉴스레터 상세 조회 시 404 에러")
        void getNewsletterDetail_notFound() throws Exception {
                // given
                doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                                .addContext("newsletterId", 999L))
                                .when(newsletterService).getNewsletterDetail(999L);

                // when & then
                mockMvc.perform(get("/admin/api/v1/newsletters/{id}", 999L))
                                .andDo(print())
                                .andExpect(status().isNotFound())
                                .andExpect(jsonPath("$.code").value("M003"))
                                .andExpect(jsonPath("$.message").value("존재하지 않는 데이터입니다."));
        }

        @Test
        @DisplayName("뉴스레터 수정 성공")
        void updateNewsletter_success() throws Exception {
                // given
                UpdateNewsletterRequest request = new UpdateNewsletterRequest(
                                "수정된 이름",
                                "수정된 설명",
                                "http://updated.image",
                                "updated@email.com",
                                "경제",
                                "http://updated.main",
                                "http://updated.sub",
                                "매월",
                                "수정된 발송자",
                                "http://updated.prev",
                                false,
                                "카카오톡");

                doNothing().when(newsletterService).update(1L, request);

                // when & then
                mockMvc.perform(patch("/admin/api/v1/newsletters/{id}", 1L)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                                .andDo(print())
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("뉴스레터 삭제 성공")
        void deleteNewsletter_success() throws Exception {
                // given
                doNothing().when(newsletterService).delete(1L);

                // when & then
                mockMvc.perform(delete("/admin/api/v1/newsletters/{id}", 1L))
                                .andDo(print())
                                .andExpect(status().isNoContent());
        }
}
