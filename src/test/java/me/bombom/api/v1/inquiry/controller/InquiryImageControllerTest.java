package me.bombom.api.v1.inquiry.controller;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import me.bombom.api.v1.common.support.ControllerTestSupport;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import me.bombom.api.v1.inquiry.service.InquiryImageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@WebMvcTest(controllers = InquiryImageController.class)
class InquiryImageControllerTest extends ControllerTestSupport {

    @MockitoBean
    private InquiryImageService inquiryImageService;

    @Test
    @DisplayName("이미지를 업로드한다.")
    void 이미지_업로드() throws Exception {
        // given
        MockMultipartFile image = new MockMultipartFile("images", "test.png", "image/png", new byte[] {1, 2, 3});
        given(inquiryImageService.uploadImages(anyList()))
                .willReturn(new InquiryImageUploadResponse(List.of("https://bombom-inquiry.s3.ap-northeast-2.amazonaws.com/inquiry/test.png")));

        // when & then
        mockMvc.perform(multipart("/admin/api/v1/inquiries/images")
                        .file(image)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imageUrls[0]").value("https://bombom-inquiry.s3.ap-northeast-2.amazonaws.com/inquiry/test.png"));
    }
}
