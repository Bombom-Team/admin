package me.bombom.api.v1.notice.controller;

import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.CreateNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticeDetailResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.dto.UploadNoticeImageResponse;
import me.bombom.api.v1.notice.service.NoticeImageService;
import me.bombom.api.v1.notice.service.NoticeService;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

@WebMvcTest(controllers = NoticeController.class)
class NoticeControllerTest extends ControllerTestSupport {

        @MockitoBean
        private NoticeService noticeService;

        @MockitoBean
        private NoticeImageService noticeImageService;

        @Test
        @DisplayName("공지사항 이미지를 업로드하면 201과 함께 이미지 id와 URL을 반환한다.")
        void uploadNoticeImage() throws Exception {
                // given
                MockMultipartFile imageFile = new MockMultipartFile("imageFile", "notice.png", "image/png",
                                "content".getBytes());
                given(noticeImageService.uploadNoticeImage(1L, imageFile))
                                .willReturn(new UploadNoticeImageResponse(10L, "https://cdn.bombom.me/notices/202609/notice.png"));

                // when & then
                mockMvc.perform(multipart("/admin/api/v1/notices/{noticeId}/images", 1L)
                                .file(imageFile)
                                .with(csrf()))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.imageId").value(10L))
                                .andExpect(jsonPath("$.imageUrl")
                                                .value("https://cdn.bombom.me/notices/202609/notice.png"));
        }

        @Test
        @DisplayName("공지사항 초안을 생성하고 공지 id를 반환한다.")
        void createNotice() throws Exception {
                // given
                given(noticeService.createNotice()).willReturn(new CreateNoticeResponse(1L));

                // when & then
                mockMvc.perform(post("/admin/api/v1/notices"))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.noticeId").value(1L));

                verify(noticeService).createNotice();
        }

        @Test
        @DisplayName("공지사항을 수정한다.")
        void updateNotice() throws Exception {
                // given
                UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("수정 제목", "수정 내용",
                                NoticeCategory.UPDATE, NoticeVisibility.PUBLIC, true, null);

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
                UpdateNoticeRequest updateNoticeRequest = new UpdateNoticeRequest("수정 제목", null, null, null, null, null);

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
                GetNoticeResponse response = new GetNoticeResponse(
                                1L,
                                "제목",
                                NoticeCategory.NOTICE,
                                NoticeVisibility.PUBLIC,
                                true,
                                java.time.LocalDateTime.of(2026, 8, 30, 1, 23, 45));
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
                                .andExpect(jsonPath("$.content[0].id").value(1L))
                                .andExpect(jsonPath("$.content[0].title").value("제목"))
                                .andExpect(jsonPath("$.content[0].noticeCategory").value("NOTICE"))
                                .andExpect(jsonPath("$.content[0].visibility").value("PUBLIC"))
                                .andExpect(jsonPath("$.content[0].isRepresentative").value(true))
                                .andExpect(jsonPath("$.content[0].createdAt").value("2026-08-30T01:23:45"));
        }

        @Test
        @DisplayName("공지사항 상세 정보를 조회한다.")
        void getNotice() throws Exception {
                // given
                GetNoticeDetailResponse response = new me.bombom.api.v1.notice.dto.GetNoticeDetailResponse(
                                "제목",
                                NoticeCategory.NOTICE,
                                "내용",
                                java.time.LocalDateTime.of(2026, 8, 30, 1, 23, 45));

                given(noticeService.getNotice(1L)).willReturn(response);

                // when & then
                mockMvc.perform(get("/admin/api/v1/notices/1"))
                                .andDo(print())
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.title").value("제목"))
                                .andExpect(jsonPath("$.noticeCategory").value("NOTICE"))
                                .andExpect(jsonPath("$.content").value("내용"))
                                .andExpect(jsonPath("$.createdAt").value("2026-08-30T01:23:45"));
        }

        @Test
        @DisplayName("존재하지 않는 공지사항 상세 조회 시 404를 반환한다.")
        void getNotice_notFound() throws Exception {
                // given
                given(noticeService.getNotice(999L))
                                .willThrow(new me.bombom.api.v1.common.exception.CIllegalArgumentException(
                                                me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND));

                // when & then
                mockMvc.perform(get("/admin/api/v1/notices/999"))
                                .andDo(print())
                                .andExpect(status().isNotFound());
        }
}
