package me.bombom.api.v1.notice.service;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.config.TimeConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.notice.domain.NoticeImageAsset;
import me.bombom.api.v1.notice.domain.NoticeImageAssetStatus;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.CreateNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.fixture.NoticeFixture;
import me.bombom.api.v1.notice.repository.NoticeImageAssetRepository;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.notice.repository.NoticeRepresentativeRepository;
import me.bombom.api.v1.notice.domain.NoticeRepresentative;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

@DataJpaTest
@Import({ NoticeService.class, QuerydslConfig.class, TimeConfig.class })
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@org.springframework.test.context.TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class NoticeServiceTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeRepresentativeRepository noticeRepresentativeRepository;

    @Autowired
    private NoticeImageAssetRepository noticeImageAssetRepository;

    @Test
    @DisplayName("공지사항 초안을 생성하면 비공개 상태의 빈 공지가 저장되고 id가 반환된다.")
    void createNotice() {
        // when
        CreateNoticeResponse response = noticeService.createNotice();

        // then
        List<Notice> notices = noticeRepository.findAll();
        assertSoftly(softly -> {
            assertThat(notices).hasSize(1);
            Notice draft = notices.getFirst();
            assertThat(response.noticeId()).isEqualTo(draft.getId());
            assertThat(draft.getVisibility()).isEqualTo(NoticeVisibility.PRIVATE);
            assertThat(draft.getTitle()).isNull();
            assertThat(draft.getContent()).isNull();
            assertThat(draft.getNoticeCategory()).isNull();
        });
    }

    @Test
    @DisplayName("공지사항을 수정한다.")
    void updateNotice() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", "수정 내용", NoticeCategory.UPDATE,
                NoticeVisibility.PUBLIC, null, null);

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        Notice updatedNotice = noticeRepository.findById(notice.getId()).get();

        assertSoftly(softly -> {
            assertThat(updatedNotice.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedNotice.getContent()).isEqualTo("수정 내용");
            assertThat(updatedNotice.getNoticeCategory()).isEqualTo(NoticeCategory.UPDATE);
            assertThat(updatedNotice.getVisibility()).isEqualTo(NoticeVisibility.PUBLIC);
        });
    }

    @Test
    @DisplayName("공지사항을 일부만 수정한다.")
    void updateNotice_partial() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", null, null, null, null, null);

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        Notice updatedNotice = noticeRepository.findById(notice.getId()).get();

        assertSoftly(softly -> {
            assertThat(updatedNotice.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedNotice.getContent()).isEqualTo("내용");
            assertThat(updatedNotice.getNoticeCategory()).isEqualTo(NoticeCategory.NOTICE);
        });
    }

    @Test
    @DisplayName("대표 공지로 지정하면 대표 공지는 지정한 1건으로 교체된다.")
    void updateNotice_representativeIsUnique() {
        // given
        Notice previous = noticeRepository.save(NoticeFixture.createNotice("기존 대표", "내용", NoticeCategory.NOTICE));
        Notice target = noticeRepository.save(NoticeFixture.createNotice("새 공지", "내용", NoticeCategory.NOTICE));
        noticeService.updateNotice(previous.getId(), representativeRequest(true));

        // when
        noticeService.updateNotice(target.getId(), representativeRequest(true));

        // then
        assertSoftly(softly -> {
            assertThat(noticeRepresentativeRepository.findAll()).hasSize(1);
            assertThat(representativeNoticeId()).isEqualTo(target.getId());
        });
    }

    @Test
    @DisplayName("대표 공지를 다시 지정해도 대표 지정이 유지된다.")
    void updateNotice_representativeStaysWhenReassigned() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("대표", "내용", NoticeCategory.NOTICE));
        noticeService.updateNotice(notice.getId(), representativeRequest(true));

        // when
        noticeService.updateNotice(notice.getId(), new UpdateNoticeRequest("수정 제목", null, null, null, true, null));

        // then
        assertThat(representativeNoticeId()).isEqualTo(notice.getId());
    }

    @Test
    @DisplayName("대표 공지를 해제하면 대표 공지가 없는 상태가 된다.")
    void updateNotice_clearRepresentative() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("대표", "내용", NoticeCategory.NOTICE));
        noticeService.updateNotice(notice.getId(), representativeRequest(true));

        // when
        noticeService.updateNotice(notice.getId(), representativeRequest(false));

        // then
        assertThat(noticeRepresentativeRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("대표가 아닌 공지를 해제해도 기존 대표 공지는 유지된다.")
    void updateNotice_clearRepresentative_otherNotice() {
        // given
        Notice representative = noticeRepository.save(NoticeFixture.createNotice("대표", "내용", NoticeCategory.NOTICE));
        Notice other = noticeRepository.save(NoticeFixture.createNotice("일반", "내용", NoticeCategory.NOTICE));
        noticeService.updateNotice(representative.getId(), representativeRequest(true));

        // when
        noticeService.updateNotice(other.getId(), representativeRequest(false));

        // then
        assertThat(representativeNoticeId()).isEqualTo(representative.getId());
    }

    private static UpdateNoticeRequest representativeRequest(boolean isRepresentative) {
        return new UpdateNoticeRequest(null, null, null, null, isRepresentative, null);
    }

    private Long representativeNoticeId() {
        return noticeRepresentativeRepository.findById(NoticeRepresentative.SINGLETON_ID)
                .orElseThrow()
                .getNoticeId();
    }

    @Test
    @DisplayName("공지사항을 삭제한다.")
    void deleteNotice() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        // when
        noticeService.deleteNotice(notice.getId());

        // then
        List<Notice> notices = noticeRepository.findAll();
        assertThat(notices).isEmpty();
    }

    @Test
    @DisplayName("목록 조회 시 대표로 지정된 공지만 isRepresentative가 true로 내려온다.")
    void getNotices_representativeFlag() {
        // given
        Notice representative = noticeRepository.save(
                NoticeFixture.createNotice("대표 공지", "내용", NoticeCategory.NOTICE));
        noticeRepository.save(NoticeFixture.createNotice("일반 공지", "내용", NoticeCategory.NOTICE));
        noticeService.updateNotice(representative.getId(), representativeRequest(true));

        // when
        Page<GetNoticeResponse> result = noticeService.getNotices(
                new GetNoticesRequest(null, null), PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent())
                    .filteredOn(GetNoticeResponse::isRepresentative)
                    .extracting(GetNoticeResponse::id)
                    .containsExactly(representative.getId());
        });
    }

    @Test
    @DisplayName("공지사항 목록을 조회한다.")
    void getNotices() {
        // given
        noticeRepository.save(NoticeFixture.createNotice("공지 제목", "공지 내용", NoticeCategory.NOTICE));
        noticeRepository.save(NoticeFixture.createNotice("이벤트 제목", "이벤트 내용", NoticeCategory.EVENT));

        GetNoticesRequest request = new GetNoticesRequest("공지", null);
        Pageable pageRequest = PageRequest.of(0, 10);

        // when
        Page<GetNoticeResponse> result = noticeService.getNotices(request, pageRequest);

        // then
        assertSoftly(softly -> {
            assertThat(result.getContent()).hasSize(1);
            GetNoticeResponse first = result.getContent().getFirst();
            assertThat(first.noticeCategory()).isEqualTo(NoticeCategory.NOTICE);
            assertThat(first.visibility()).isEqualTo(NoticeVisibility.PUBLIC);
            assertThat(first.isRepresentative()).isFalse();
            assertThat(first.createdAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 삭제 시 예외가 발생한다.")
    void deleteNotice_exception() {
        // when & then
        assertThatThrownBy(() -> noticeService.deleteNotice(999L))
                .isInstanceOf(me.bombom.api.v1.common.exception.CIllegalArgumentException.class)
                .hasMessage(me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("공지사항 상세 정보를 조회한다.")
    void getNotice() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        // when
        me.bombom.api.v1.notice.dto.GetNoticeDetailResponse response = noticeService.getNotice(notice.getId());

        // then
        assertSoftly(softly -> {
            assertThat(response.title()).isEqualTo("제목");
            assertThat(response.content()).isEqualTo("내용");
            assertThat(response.noticeCategory()).isEqualTo(NoticeCategory.NOTICE);
        });
    }

    @Test
    @DisplayName("존재하지 않는 공지사항 조회 시 예외가 발생한다.")
    void getNotice_exception() {
        // when & then
        assertThatThrownBy(() -> noticeService.getNotice(999L))
                .isInstanceOf(me.bombom.api.v1.common.exception.CIllegalArgumentException.class)
                .hasMessage(me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("referencedImageIds에 포함된 이미지는 UPLOADED에서 ATTACHED로 전환된다.")
    void updateNotice_referencedImageAttached() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        NoticeImageAsset image = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/a.png", NoticeImageAssetStatus.UPLOADED));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null, List.of(image.getId()));

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        NoticeImageAsset updated = noticeImageAssetRepository.findById(image.getId()).orElseThrow();
        assertSoftly(softly -> {
            assertThat(updated.getStatus()).isEqualTo(NoticeImageAssetStatus.ATTACHED);
            assertThat(updated.getDeleteRequestedAt()).isNull();
        });
    }

    @Test
    @DisplayName("참조가 끊긴 ATTACHED 이미지는 DELETE_PENDING으로 전환되고 삭제 요청 시각이 기록된다.")
    void updateNotice_unreferencedImageMarkedDeletePending() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        NoticeImageAsset image = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/a.png", NoticeImageAssetStatus.ATTACHED));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null, List.of());

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        NoticeImageAsset updated = noticeImageAssetRepository.findById(image.getId()).orElseThrow();
        assertSoftly(softly -> {
            assertThat(updated.getStatus()).isEqualTo(NoticeImageAssetStatus.DELETE_PENDING);
            assertThat(updated.getDeleteRequestedAt()).isNotNull();
        });
    }

    @Test
    @DisplayName("다른 공지에 속한 이미지 id를 전달하면 예외가 발생한다.")
    void updateNotice_foreignImageId() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        Notice otherNotice = noticeRepository.save(NoticeFixture.createNotice("다른 제목", "다른 내용", NoticeCategory.NOTICE));
        NoticeImageAsset foreignImage = noticeImageAssetRepository.save(
                createNoticeImageAsset(otherNotice.getId(), "notices/202609/other.png", NoticeImageAssetStatus.UPLOADED));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null,
                List.of(foreignImage.getId()));

        // when & then
        assertThatThrownBy(() -> noticeService.updateNotice(notice.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.NOTICE_IMAGE_NOT_REGISTERED);
    }

    @Test
    @DisplayName("존재하지 않는 이미지 id를 전달하면 예외가 발생한다.")
    void updateNotice_unknownImageId() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null, List.of(999L));

        // when & then
        assertThatThrownBy(() -> noticeService.updateNotice(notice.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    @DisplayName("referencedImageIds가 null이면 기존 이미지 상태를 변경하지 않는다.")
    void updateNotice_nullReferencedImageIdsKeepsStatus() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        NoticeImageAsset attachedImage = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/a.png", NoticeImageAssetStatus.ATTACHED));
        NoticeImageAsset uploadedImage = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/b.png", NoticeImageAssetStatus.UPLOADED));

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", null, null, null, null, null);

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        assertSoftly(softly -> {
            assertThat(noticeImageAssetRepository.findById(attachedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(NoticeImageAssetStatus.ATTACHED);
            assertThat(noticeImageAssetRepository.findById(uploadedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(NoticeImageAssetStatus.UPLOADED);
        });
    }

    @Test
    @DisplayName("referencedImageIds에 null 원소가 포함되면 예외가 발생한다.")
    void updateNotice_nullElementInReferencedImageIds() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null,
                Arrays.asList(1L, null));

        // when & then
        assertThatThrownBy(() -> noticeService.updateNotice(notice.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .extracting("errorDetail")
                .isEqualTo(ErrorDetail.INVALID_INPUT_VALUE);
    }

    @Test
    @DisplayName("referencedImageIds에 중복된 id가 있어도 정상적으로 참조 처리된다.")
    void updateNotice_duplicatedReferencedImageIds() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        NoticeImageAsset image = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/a.png", NoticeImageAssetStatus.UPLOADED));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, null,
                List.of(image.getId(), image.getId()));

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        assertThat(noticeImageAssetRepository.findById(image.getId()).orElseThrow().getStatus())
                .isEqualTo(NoticeImageAssetStatus.ATTACHED);
    }

    @Test
    @DisplayName("공지사항을 삭제하면 연결된 이미지 자산이 모두 DELETE_PENDING으로 전환된다.")
    void deleteNotice_marksImagesDeletePending() {
        // given
        Notice notice = noticeRepository.save(NoticeFixture.createNotice("제목", "내용", NoticeCategory.NOTICE));
        NoticeImageAsset attachedImage = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/a.png", NoticeImageAssetStatus.ATTACHED));
        NoticeImageAsset uploadedImage = noticeImageAssetRepository.save(
                createNoticeImageAsset(notice.getId(), "notices/202609/b.png", NoticeImageAssetStatus.UPLOADED));

        // when
        noticeService.deleteNotice(notice.getId());

        // then
        assertSoftly(softly -> {
            assertThat(noticeImageAssetRepository.findById(attachedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(NoticeImageAssetStatus.DELETE_PENDING);
            assertThat(noticeImageAssetRepository.findById(uploadedImage.getId()).orElseThrow().getStatus())
                    .isEqualTo(NoticeImageAssetStatus.DELETE_PENDING);
            assertThat(noticeImageAssetRepository.findById(attachedImage.getId()).orElseThrow()
                    .getDeleteRequestedAt()).isNotNull();
        });
    }

    private NoticeImageAsset createNoticeImageAsset(
            Long noticeId,
            String objectKey,
            NoticeImageAssetStatus status
    ) {
        return NoticeImageAsset.builder()
                .noticeId(noticeId)
                .objectKey(objectKey)
                .imageUrl("https://cdn.bombom.me/" + objectKey)
                .status(status)
                .build();
    }
}
