package me.bombom.api.v1.dev.controller;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import me.bombom.api.v1.dev.dto.LambdaPlaywrightSourceRequest;
import me.bombom.api.v1.dev.service.LambdaPlaywrightService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = LambdaPlaywrightController.class)
class LambdaPlaywrightControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(lambdaPlaywrightService).updateSource(request);
    }
}
