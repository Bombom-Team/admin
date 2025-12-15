package news.bombomadmin.notice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import news.bombomadmin.common.support.ControllerTestSupport;
import news.bombomadmin.notice.dto.CreateNoticeRequest;
import news.bombomadmin.notice.service.NoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
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
        String requestBody = """
                {
                    "title": "제목",
                    "content": "내용",
                    "noticeCategory": "NOTICE"
                }
                """;

        // when & then
        mockMvc.perform(post("/api/v1/notices")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk());

        verify(noticeService).createNotice(any(CreateNoticeRequest.class));
    }
}
