package me.bombom.api.v1.inquiry.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.inquiry.domain.InquirerType;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.AssignInquiryRoomRequest;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryRoomStatusRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryRoomResponse;
import me.bombom.api.v1.inquiry.dto.response.LastMessageResponse;
import me.bombom.api.v1.inquiry.domain.InquirySenderType;
import me.bombom.api.v1.inquiry.service.InquiryRoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InquiryRoomController.class)
class InquiryRoomControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryRoomService inquiryRoomService;

    @Test
    @DisplayName("채팅방 목록을 조회한다.")
    void 채팅방_목록_조회() throws Exception {
        // given
        LastMessageResponse lastMessage = new LastMessageResponse(
                "안녕하세요", false, InquirySenderType.ADMIN, "상추", LocalDateTime.now());
        InquiryRoomResponse response = new InquiryRoomResponse(
                1L, 10L, InquiryStatus.UNCONFIRMED, 100L, "상추",
                InquirerType.MEMBER, "메이", "may@example.com", "https://img/1", lastMessage,
                LocalDateTime.now(), null);
        given(inquiryRoomService.getRooms(any(GetInquiryRoomsRequest.class), any(Pageable.class)))
                .willReturn(new PageImpl<>(List.of(response), PageRequest.of(0, 20), 1));

        // when & then
        mockMvc.perform(get("/admin/api/v1/inquiries/rooms"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1L))
                .andExpect(jsonPath("$.content[0].assigneeNickname").value("상추"))
                .andExpect(jsonPath("$.content[0].inquirerType").value("MEMBER"))
                .andExpect(jsonPath("$.content[0].inquirerLabel").value("메이"))
                .andExpect(jsonPath("$.content[0].inquirerEmail").value("may@example.com"))
                .andExpect(jsonPath("$.content[0].lastMessage.content").value("안녕하세요"))
                .andExpect(jsonPath("$.content[0].lastMessage.adminNickname").value("상추"));
    }

    @Test
    @DisplayName("담당자를 지정한다.")
    void 담당자_지정() throws Exception {
        // given
        AssignInquiryRoomRequest request = new AssignInquiryRoomRequest(100L);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/inquiries/rooms/1/assignee")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inquiryRoomService).assignRoom(any(Long.class), any(AssignInquiryRoomRequest.class));
    }

    @Test
    @DisplayName("채팅방 상태를 변경한다.")
    void 상태_변경() throws Exception {
        // given
        UpdateInquiryRoomStatusRequest request = new UpdateInquiryRoomStatusRequest(InquiryStatus.DONE);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/inquiries/rooms/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(inquiryRoomService).changeStatus(any(Long.class), any(UpdateInquiryRoomStatusRequest.class));
    }
}
