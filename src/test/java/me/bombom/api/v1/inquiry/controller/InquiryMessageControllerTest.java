package me.bombom.api.v1.inquiry.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.dto.request.SendAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateAdminInquiryMessageRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessagePageResponse;
import me.bombom.api.v1.inquiry.dto.response.InquiryMessageResponse;
import me.bombom.api.v1.inquiry.service.InquiryMessageService;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@WebMvcTest(controllers = InquiryMessageController.class)
@Import(InquiryMessageControllerTest.LoginMemberResolverTestConfig.class)
class InquiryMessageControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryMessageService inquiryMessageService;

    @Test
    @DisplayName("어드민 메시지를 전송한다.")
    void 메시지_전송() throws Exception {
        // given
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);
        InquiryMessageResponse response = new InquiryMessageResponse(
                1L, 1L, InquirySenderType.ADMIN, 1L, "답변입니다", List.of(), LocalDateTime.now());
        given(inquiryMessageService.sendMessage(any(Long.class), any(Long.class), any(SendAdminInquiryMessageRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(post("/admin/api/v1/inquiries/rooms/1/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("답변입니다"));
    }

    @Test
    @DisplayName("종료된 채팅방에 메시지 전송 시 400을 반환한다.")
    void 종료된_방_메시지_전송_실패() throws Exception {
        // given
        SendAdminInquiryMessageRequest request = new SendAdminInquiryMessageRequest("답변입니다", null);
        given(inquiryMessageService.sendMessage(any(Long.class), any(Long.class), any(SendAdminInquiryMessageRequest.class)))
                .willThrow(new CIllegalArgumentException(ErrorDetail.INQUIRY_ROOM_CLOSED));

        // when & then
        mockMvc.perform(post("/admin/api/v1/inquiries/rooms/1/messages")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("어드민 메시지를 수정한다.")
    void 메시지_수정() throws Exception {
        // given
        UpdateAdminInquiryMessageRequest request = new UpdateAdminInquiryMessageRequest("수정본");
        InquiryMessageResponse response = new InquiryMessageResponse(
                1L, 1L, InquirySenderType.ADMIN, 1L, "수정본", List.of(), LocalDateTime.now());
        given(inquiryMessageService.updateMessage(any(Long.class), any(Long.class), any(Long.class), any(UpdateAdminInquiryMessageRequest.class)))
                .willReturn(response);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/inquiries/rooms/1/messages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("수정본"));
    }

    @Test
    @DisplayName("본인이 작성하지 않은 메시지 수정 시 403을 반환한다.")
    void 타인_메시지_수정_실패() throws Exception {
        // given
        UpdateAdminInquiryMessageRequest request = new UpdateAdminInquiryMessageRequest("수정본");
        given(inquiryMessageService.updateMessage(any(Long.class), any(Long.class), any(Long.class), any(UpdateAdminInquiryMessageRequest.class)))
                .willThrow(new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE));

        // when & then
        mockMvc.perform(patch("/admin/api/v1/inquiries/rooms/1/messages/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("어드민 메시지를 삭제한다.")
    void 메시지_삭제() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/inquiries/rooms/1/messages/1").with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("메시지 목록을 조회한다.")
    void 메시지_목록_조회() throws Exception {
        // given
        InquiryMessageResponse response = new InquiryMessageResponse(
                1L, 1L, InquirySenderType.ADMIN, 1L, "답변입니다", List.of(), LocalDateTime.now());
        given(inquiryMessageService.getMessages(any(Long.class), any(), anyInt()))
                .willReturn(new InquiryMessagePageResponse(List.of(response), false));

        // when & then
        mockMvc.perform(get("/admin/api/v1/inquiries/rooms/1/messages"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messages[0].content").value("답변입니다"))
                .andExpect(jsonPath("$.hasNext").value(false));
    }

    @TestConfiguration
    static class LoginMemberResolverTestConfig implements WebMvcConfigurer {

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(new HandlerMethodArgumentResolver() {
                @Override
                public boolean supportsParameter(MethodParameter parameter) {
                    return parameter.hasParameterAnnotation(LoginMember.class)
                            && parameter.getParameterType().equals(Member.class);
                }

                @Override
                public Object resolveArgument(
                        MethodParameter parameter,
                        ModelAndViewContainer mavContainer,
                        NativeWebRequest webRequest,
                        WebDataBinderFactory binderFactory
                ) {
                    return Member.builder()
                            .id(1L)
                            .provider("local")
                            .providerId("local")
                            .email("local@bombom.me")
                            .nickname("local-admin")
                            .gender(Gender.MALE)
                            .roleId(2L)
                            .build();
                }
            });
        }
    }
}
