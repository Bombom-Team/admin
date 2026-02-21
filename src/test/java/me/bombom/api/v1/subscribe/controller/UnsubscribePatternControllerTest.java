package me.bombom.api.v1.subscribe.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternUpdateRequest;
import me.bombom.api.v1.subscribe.dto.response.UnsubscribePatternResponse;
import me.bombom.api.v1.subscribe.service.UnsubscribePatternService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = UnsubscribePatternController.class)
class UnsubscribePatternControllerTest extends ControllerTestSupport {

    @MockitoBean
    private UnsubscribePatternService unsubscribePatternService;

    @Test
    @DisplayName("구독_해지_패턴_목록을_조회한다")
    void 구독_해지_패턴_목록을_조회한다() throws Exception {
        // given
        List<UnsubscribePatternResponse> responses = List.of(
                new UnsubscribePatternResponse(1L, "pattern-key-1", "pattern-value-1"),
                new UnsubscribePatternResponse(2L, "pattern-key-2", "pattern-value-2")
        );
        given(unsubscribePatternService.getUnsubscribePatterns()).willReturn(responses);

        // when & then
        mockMvc.perform(get("/admin/api/v1/unsubscribe-patterns"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].patternKey").value("pattern-key-1"))
                .andExpect(jsonPath("$[0].patternValue").value("pattern-value-1"));
    }

    @Test
    @DisplayName("구독_해지_패턴_단건을_조회한다")
    void 구독_해지_패턴_단건을_조회한다() throws Exception {
        // given
        UnsubscribePatternResponse response =
                new UnsubscribePatternResponse(1L, "pattern-key", "pattern-value");
        given(unsubscribePatternService.getUnsubscribePattern(1L)).willReturn(response);

        // when & then
        mockMvc.perform(get("/admin/api/v1/unsubscribe-patterns/{id}", 1L))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.patternKey").value("pattern-key"))
                .andExpect(jsonPath("$.patternValue").value("pattern-value"));
    }

    @Test
    @DisplayName("구독_해지_패턴을_생성한다")
    void 구독_해지_패턴을_생성한다() throws Exception {
        // given
        UnsubscribePatternRequest request = new UnsubscribePatternRequest(
                "pattern-key",
                "pattern-value"
        );

        // when & then
        mockMvc.perform(post("/admin/api/v1/unsubscribe-patterns")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(unsubscribePatternService).createUnsubscribePattern(any(UnsubscribePatternRequest.class));
    }

    @Test
    @DisplayName("구독_해지_패턴을_수정한다")
    void 구독_해지_패턴을_수정한다() throws Exception {
        // given
        Long id = 1L;
        UnsubscribePatternUpdateRequest request = new UnsubscribePatternUpdateRequest("updated-pattern-value");

        // when & then
        mockMvc.perform(patch("/admin/api/v1/unsubscribe-patterns/{id}", id)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(unsubscribePatternService).updateUnsubscribePattern(eq(id),
                any(UnsubscribePatternUpdateRequest.class));
    }
}
