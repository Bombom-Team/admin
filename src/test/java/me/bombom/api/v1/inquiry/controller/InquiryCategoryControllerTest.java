package me.bombom.api.v1.inquiry.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.service.InquiryCategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InquiryCategoryController.class)
class InquiryCategoryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryCategoryService inquiryCategoryService;

    @Test
    @DisplayName("카테고리를 생성한다.")
    void 카테고리_생성() throws Exception {
        // given
        CreateInquiryCategoryRequest request = new CreateInquiryCategoryRequest("뉴스레터");

        // when & then
        mockMvc.perform(post("/admin/api/v1/inquiries/categories")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(inquiryCategoryService).createCategory(any(CreateInquiryCategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리를 수정한다.")
    void 카테고리_수정() throws Exception {
        // given
        UpdateInquiryCategoryRequest request = new UpdateInquiryCategoryRequest("챌린지");

        // when & then
        mockMvc.perform(patch("/admin/api/v1/inquiries/categories/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inquiryCategoryService).updateCategory(any(Long.class), any(UpdateInquiryCategoryRequest.class));
    }

    @Test
    @DisplayName("카테고리를 삭제한다.")
    void 카테고리_삭제() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/inquiries/categories/1").with(csrf()))
                .andExpect(status().isNoContent());

        verify(inquiryCategoryService).deleteCategory(any(Long.class));
    }

    @Test
    @DisplayName("사용 중인 카테고리 삭제 시 400을 반환한다.")
    void 사용중인_카테고리_삭제_실패() throws Exception {
        // given
        org.mockito.BDDMockito.willThrow(new CIllegalArgumentException(ErrorDetail.INQUIRY_CATEGORY_IN_USE))
                .given(inquiryCategoryService).deleteCategory(1L);

        // when & then
        mockMvc.perform(delete("/admin/api/v1/inquiries/categories/1").with(csrf()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("카테고리 목록을 조회한다.")
    void 카테고리_목록_조회() throws Exception {
        // given
        given(inquiryCategoryService.getCategories())
                .willReturn(List.of(new InquiryCategoryResponse(1L, "뉴스레터")));

        // when & then
        mockMvc.perform(get("/admin/api/v1/inquiries/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("뉴스레터"));
    }
}
