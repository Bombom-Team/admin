package me.bombom.api.v1.notice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.file.dto.StoredFile;
import me.bombom.api.v1.file.service.S3FileService;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeImageAsset;
import me.bombom.api.v1.notice.domain.NoticeImageAssetStatus;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.UploadNoticeImageResponse;
import me.bombom.api.v1.notice.repository.NoticeImageAssetRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@Import({NoticeImageService.class, QuerydslConfig.class})
class NoticeImageServiceTest {

    private static final String OBJECT_KEY = "notices/202609/test.png";
    private static final String IMAGE_URL = "https://cdn.bombom.me/notices/202609/test.png";

    @Autowired
    private NoticeImageService noticeImageService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeImageAssetRepository noticeImageAssetRepository;

    @MockitoBean
    private S3FileService s3FileService;

    @Test
    @DisplayName("초안 공지에 이미지를 업로드하면 UPLOADED 상태의 이미지 자산이 생성된다.")
    void 초안_공지_이미지_업로드_성공() {
        // given
        Notice notice = noticeRepository.save(createNotice(NoticeVisibility.PRIVATE));
        MockMultipartFile imageFile = createImageFile();
        given(s3FileService.uploadToBucketWithS3Url(eq(imageFile), anyString(), eq("notices")))
                .willReturn(new StoredFile(OBJECT_KEY, IMAGE_URL));

        // when
        UploadNoticeImageResponse response = noticeImageService.uploadNoticeImage(notice.getId(), imageFile);

        // then
        NoticeImageAsset noticeImageAsset = noticeImageAssetRepository.findById(response.imageId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(response.imageUrl()).isEqualTo(IMAGE_URL);
            softly.assertThat(noticeImageAsset.getNoticeId()).isEqualTo(notice.getId());
            softly.assertThat(noticeImageAsset.getObjectKey()).isEqualTo(OBJECT_KEY);
            softly.assertThat(noticeImageAsset.getImageUrl()).isEqualTo(IMAGE_URL);
            softly.assertThat(noticeImageAsset.getStatus()).isEqualTo(NoticeImageAssetStatus.UPLOADED);
            softly.assertThat(noticeImageAsset.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    @DisplayName("이미 공개된 공지에 이미지를 업로드하면 ATTACHED 상태의 이미지 자산이 생성된다.")
    void 공개_공지_이미지_업로드_성공() {
        // given
        Notice notice = noticeRepository.save(createNotice(NoticeVisibility.PUBLIC));
        MockMultipartFile imageFile = createImageFile();
        given(s3FileService.uploadToBucketWithS3Url(eq(imageFile), anyString(), eq("notices")))
                .willReturn(new StoredFile(OBJECT_KEY, IMAGE_URL));

        // when
        UploadNoticeImageResponse response = noticeImageService.uploadNoticeImage(notice.getId(), imageFile);

        // then
        NoticeImageAsset noticeImageAsset = noticeImageAssetRepository.findById(response.imageId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(noticeImageAsset.getStatus()).isEqualTo(NoticeImageAssetStatus.ATTACHED);
            softly.assertThat(noticeImageAsset.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    @DisplayName("존재하지 않는 공지에 이미지를 업로드하면 예외가 발생한다.")
    void 존재하지_않는_공지_이미지_업로드_시_예외() {
        // given
        MockMultipartFile imageFile = createImageFile();

        // when & then
        assertThatThrownBy(() -> noticeImageService.uploadNoticeImage(999L, imageFile))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("빈 이미지 파일을 업로드하면 예외가 발생한다.")
    void 빈_이미지_파일_업로드_시_예외() {
        // given
        Notice notice = noticeRepository.save(createNotice(NoticeVisibility.PRIVATE));
        MockMultipartFile emptyImageFile = new MockMultipartFile("imageFile", "notice.png", "image/png", new byte[0]);

        // when & then
        assertThatThrownBy(() -> noticeImageService.uploadNoticeImage(notice.getId(), emptyImageFile))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    private Notice createNotice(NoticeVisibility visibility) {
        return Notice.builder()
                .title("제목")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .visibility(visibility)
                .build();
    }

    private MockMultipartFile createImageFile() {
        return new MockMultipartFile("imageFile", "notice.png", "image/png", "content".getBytes());
    }
}
