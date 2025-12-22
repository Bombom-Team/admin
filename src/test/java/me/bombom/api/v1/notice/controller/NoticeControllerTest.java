package me.bombom.api.v1.notice.controller;

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

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.service.NoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = NoticeController.class)
class NoticeControllerTest extends ControllerTestSupport {

    @MockitoBean
    private NoticeService noticeService;

    @Test
    @DisplayName("공지사항을 등록한다.")
    void createNotice() throws Exception {
        // given
        // noticeCategory Enum 바인딩 확인을 위해 String으로 요청 바디를 작성
        String requestBody = """
                {
                    "title": "제목",
                    "content": "내용",
                    "noticeCategory": "NOTICE"
                }
                """;

        // when & then
        mockMvc.perform(post("/admin/api/v1/notices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());

        verify(noticeService).createNotice(any(CreateNoticeRequest.class));
    }

    @Test
    @DisplayName("공지사항을 수정한다.")
    void updateNotice() throws Exception {
        // given
        UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("수정 제목", "수정 내용", NoticeCategory.UPDATE);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/notices/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoticeRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(noticeService).updateNotice(any(Long.class), any(UpdateNoticeRequest.class));
    }

    @Test
    @DisplayName("공지사항을 일부만 수정한다.")
    void updateNotice_partial() throws Exception {
        // given
        UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("수정 제목", null, null);

        // when & then
        mockMvc.perform(patch("/admin/api/v1/notices/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateNoticeRequest)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(noticeService).updateNotice(any(Long.class), any(UpdateNoticeRequest.class));
    }

    @Test
    @DisplayName("공지사항을 삭제한다.")
    void deleteNotice() throws Exception {
        // when & then
        mockMvc.perform(delete("/admin/api/v1/notices/1")
                .with(csrf()))
                .andDo(print())
                .andExpect(status().isNoContent());

        verify(noticeService).deleteNotice(any(Long.class));
    }

    @Test
    @DisplayName("공지사항 목록을 조회한다.")
    void getNotices() throws Exception {
        // given
        GetNoticeResponse response = new GetNoticeResponse(1L, "제목", "내용", "NOTICE");
        PageImpl<GetNoticeResponse> result = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        given(noticeService.getNotices(any(GetNoticesRequest.class), any(Pageable.class)))
                .willReturn(result);

        // when & then
        mockMvc.perform(get("/admin/api/v1/notices")
                .param("keyword", "제목")
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("제목"));
    }
}
