package me.bombom.api.v1.notice.service;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.domain.NoticeVisibility;
import me.bombom.api.v1.notice.dto.CreateNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.fixture.NoticeFixture;
import me.bombom.api.v1.notice.repository.NoticeRepository;

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

import java.util.List;

@DataJpaTest
@Import({ NoticeService.class, QuerydslConfig.class })
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@org.springframework.test.context.TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class NoticeServiceTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

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
            assertThat(draft.isRepresentative()).isFalse();
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
                NoticeVisibility.PUBLIC, null);

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

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", null, null, null, null);

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
    @DisplayName("대표 공지로 지정하면 기존 대표 공지는 해제된다.")
    void updateNotice_representativeIsUnique() {
        // given
        Notice previous = noticeRepository.save(Notice.builder()
                .title("기존 대표")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .visibility(NoticeVisibility.PUBLIC)
                .isRepresentative(true)
                .build());
        Notice target = noticeRepository.save(NoticeFixture.createNotice("새 공지", "내용", NoticeCategory.NOTICE));

        UpdateNoticeRequest request = new UpdateNoticeRequest(null, null, null, null, true);

        // when
        noticeService.updateNotice(target.getId(), request);

        // then
        assertSoftly(softly -> {
            assertThat(noticeRepository.findById(target.getId()).get().isRepresentative()).isTrue();
            assertThat(noticeRepository.findById(previous.getId()).get().isRepresentative()).isFalse();
        });
    }

    @Test
    @DisplayName("대표 공지를 다시 지정해도 자기 자신은 해제되지 않는다.")
    void updateNotice_representativeStaysWhenReassigned() {
        // given
        Notice notice = noticeRepository.save(Notice.builder()
                .title("대표")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .visibility(NoticeVisibility.PUBLIC)
                .isRepresentative(true)
                .build());

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", null, null, null, true);

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        assertThat(noticeRepository.findById(notice.getId()).get().isRepresentative()).isTrue();
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
}
