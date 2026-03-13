package me.bombom.api.v1.newsletter.controller;

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

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.newsletter.dto.CreateCategoryRequest;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import me.bombom.api.v1.newsletter.dto.UpdateCategoryRequest;
import me.bombom.api.v1.newsletter.service.CategoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(CategoryController.class)
class CategoryControllerTest extends ControllerTestSupport {

    @MockitoBean
    private CategoryService categoryService;

    @Test
    @DisplayName("카테고리 생성 성공")
    void createCategory_success() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("테크");
        doNothing().when(categoryService).create(any(CreateCategoryRequest.class));

        // when & then
        mockMvc.perform(post("/admin/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("카테고리 이름이 빈값이면 400 에러")
    void createCategory_blankName() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("");

        // when & then
        mockMvc.perform(post("/admin/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("중복된 카테고리 이름으로 생성 시 400 에러")
    void createCategory_duplicateName() throws Exception {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("테크");
        doThrow(new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                .addContext("name", "테크"))
                .when(categoryService).create(any(CreateCategoryRequest.class));

        // when & then
        mockMvc.perform(post("/admin/api/v1/categories")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("M009"));
    }

    @Test
    @DisplayName("카테고리 목록 조회 성공")
    void getCategories_success() throws Exception {
        // given
        given(categoryService.getCategories()).willReturn(List.of(
                new GetCategoryResponse(1L, "테크"),
                new GetCategoryResponse(2L, "경제")
        ));

        // when & then
        mockMvc.perform(get("/admin/api/v1/categories"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("테크"))
                .andExpect(jsonPath("$[1].name").value("경제"));
    }

    @Test
    @DisplayName("카테고리 수정 성공")
    void updateCategory_success() throws Exception {
        // given
        UpdateCategoryRequest request = new UpdateCategoryRequest("경제");
        doNothing().when(categoryService).update(eq(1L), any(UpdateCategoryRequest.class));

        // when & then
        mockMvc.perform(patch("/admin/api/v1/categories/{id}", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 404 에러")
    void updateCategory_notFound() throws Exception {
        // given
        UpdateCategoryRequest request = new UpdateCategoryRequest("경제");
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("categoryId", 999L))
                .when(categoryService).update(eq(999L), any(UpdateCategoryRequest.class));

        // when & then
        mockMvc.perform(patch("/admin/api/v1/categories/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M003"));
    }

    @Test
    @DisplayName("카테고리 삭제 성공")
    void deleteCategory_success() throws Exception {
        // given
        doNothing().when(categoryService).delete(1L);

        // when & then
        mockMvc.perform(delete("/admin/api/v1/categories/{id}", 1L))
                .andDo(print())
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 404 에러")
    void deleteCategory_notFound() throws Exception {
        // given
        doThrow(new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext("categoryId", 999L))
                .when(categoryService).delete(999L);

        // when & then
        mockMvc.perform(delete("/admin/api/v1/categories/{id}", 999L))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("M003"));
    }
}
