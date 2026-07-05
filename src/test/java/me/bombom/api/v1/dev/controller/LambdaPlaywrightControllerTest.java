package me.bombom.api.v1.dev.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceRequest;
import me.bombom.api.v1.dev.service.LambdaPlaywrightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = LambdaPlaywrightController.class)
class LambdaPlaywrightControllerTest extends ControllerTestSupport {

    @MockitoBean
    private LambdaPlaywrightService lambdaPlaywrightService;

    @Test
    @DisplayName("Lambda_Playwright_스크립트를_조회한다")
    void Lambda_Playwright_스크립트를_조회한다() throws Exception {
        // given
        String content = "console.log('hello');";
        given(lambdaPlaywrightService.getSource()).willReturn(content);

        // when & then
        mockMvc.perform(get("/admin/api/v1/lambda-playwright/source"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json("""
                        {"content":"console.log('hello');"}
                        """));
    }

    @Test
    @DisplayName("Lambda_Playwright_스크립트를_수정한다")
    void Lambda_Playwright_스크립트를_수정한다() throws Exception {
        // given
        LambdaPlaywrightSourceRequest request = new LambdaPlaywrightSourceRequest("console.log('updated');");

        // when & then
        mockMvc.perform(put("/admin/api/v1/lambda-playwright/source")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(lambdaPlaywrightService).updateSource(any(LambdaPlaywrightSourceRequest.class));
    }
}
