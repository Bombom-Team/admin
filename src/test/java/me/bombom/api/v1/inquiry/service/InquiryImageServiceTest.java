package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.List;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
class InquiryImageServiceTest {

    @Mock
    private S3FileService s3FileService;

    @InjectMocks
    private InquiryImageService inquiryImageService;

    @Test
    @DisplayName("이미지를 업로드하면 URL 목록을 반환한다.")
    void 이미지_업로드_성공() {
        // given
        ReflectionTestUtils.setField(inquiryImageService, "inquiryBucketName", "bombom-inquiry");
        MultipartFile image = new MockMultipartFile("images", "test.png", "image/png", new byte[] {1, 2, 3});
        given(s3FileService.uploadToBucketWithMetadata(any(MultipartFile.class), anyString(), anyString()))
                .willReturn(new StoredFile("inquiry/202609/test.png", "https://bombom-inquiry.s3.ap-northeast-2.amazonaws.com/inquiry/202609/test.png"));

        // when
        InquiryImageUploadResponse response = inquiryImageService.uploadImages(List.of(image));

        // then
        assertThat(response.imageUrls()).hasSize(1);
    }

    @Test
    @DisplayName("이미지가 4장을 초과하면 예외가 발생한다.")
    void 이미지_4장_초과_업로드_실패() {
        // given
        ReflectionTestUtils.setField(inquiryImageService, "inquiryBucketName", "bombom-inquiry");
        List<MultipartFile> images = List.of(
                new MockMultipartFile("images", "1.png", "image/png", new byte[] {1}),
                new MockMultipartFile("images", "2.png", "image/png", new byte[] {1}),
                new MockMultipartFile("images", "3.png", "image/png", new byte[] {1}),
                new MockMultipartFile("images", "4.png", "image/png", new byte[] {1}),
                new MockMultipartFile("images", "5.png", "image/png", new byte[] {1}));

        // when & then
        assertThatThrownBy(() -> inquiryImageService.uploadImages(images))
                .isInstanceOf(CIllegalArgumentException.class);
    }
}
