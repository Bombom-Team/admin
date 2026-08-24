package me.bombom.api.v1.faq.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.faq.domain.FaqCategory;
import me.bombom.api.v1.faq.dto.CreateFaqRequest;
import me.bombom.api.v1.faq.dto.GetFaqDetailResponse;
import me.bombom.api.v1.faq.dto.GetFaqResponse;
import me.bombom.api.v1.faq.dto.UpdateFaqRequest;
import me.bombom.api.v1.faq.service.FaqService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = FaqController.class)
class FaqControllerTest extends ControllerTestSupport {

    @MockitoBean
    private FaqService faqService;

    @Test
    @DisplayName("FAQ를 등록한다.")
    void createFaq() throws Exception {
        // given
        String requestBody = """
                {
                    "question": "질문",
                    "answer": "답변",
                    "faqCategory": "FEATURE"
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/faqs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated());

        verify(faqService).createFaq(any(CreateFaqRequest.class));
    }

    @Test
    @DisplayName("FAQ를 수정한다.")
    void updateFaq() throws Exception {
        // given
        UpdateFaqRequest updateFaqRequest = new UpdateFaqRequest("수정 질문", "수정 답변", FaqCategory.ACCOUNT);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/faqs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFaqRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(faqService).updateFaq(any(Long.class), any(UpdateFaqRequest.class));
    }

    @Test
    @DisplayName("FAQ를 일부만 수정한다.")
    void updateFaq_partial() throws Exception {
        // given
        UpdateFaqRequest updateFaqRequest = new UpdateFaqRequest("수정 질문", null, null);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/faqs/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateFaqRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(faqService).updateFaq(any(Long.class), any(UpdateFaqRequest.class));
    }

    @Test
    @DisplayName("FAQ를 삭제한다.")
    void deleteFaq() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/faqs/1")
                        .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(faqService).deleteFaq(any(Long.class));
    }

    @Test
    @DisplayName("FAQ 목록을 조회한다.")
    void getFaqs() throws Exception {
        // given
        GetFaqResponse response = new GetFaqResponse(1L, "질문", "기능", java.time.LocalDate.now());
        PageImpl<GetFaqResponse> result = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        given(faqService.getFaqs(any(Pageable.class)))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/admin/api/v1/faqs")
                        .param("page", "0")
                        .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].question").value("질문"));
    }

    @Test
    @DisplayName("FAQ 상세 정보를 조회한다.")
    void getFaq() throws Exception {
        // given
        GetFaqDetailResponse response = new GetFaqDetailResponse(
                "질문",
                FaqCategory.FEATURE,
                "답변",
                java.time.LocalDate.now());

        given(faqService.getFaq(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/faqs/1"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.question").value("질문"))
                .andExpect(jsonPath("$.faqCategory").value("FEATURE"))
                .andExpect(jsonPath("$.answer").value("답변"));
    }

    @Test
    @DisplayName("존재하지 않는 FAQ 상세 조회 시 404를 반환한다.")
    void getFaq_notFound() throws Exception {
        // given
        given(faqService.getFaq(999L))
                .willThrow(new me.bombom.api.v1.common.exception.CIllegalArgumentException(
                        me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND));

        // when & then
        mockMvc.perform(get("/admin/api/v1/faqs/999"))
                .andDo(print())
                .andExpect(status().isNotFound());
    }
}
