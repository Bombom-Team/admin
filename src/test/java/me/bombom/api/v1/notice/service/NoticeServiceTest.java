package me.bombom.api.v1.notice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@DataJpaTest
@Import({NoticeService.class, QuerydslConfig.class})
class NoticeServiceTest {

    @Autowired
    private NoticeService noticeService;

    @Autowired
    private NoticeRepository noticeRepository;

    @Test
    @DisplayName("공지사항을 등록한다.")
    void createNotice() {
        // given
        CreateNoticeRequest request = new CreateNoticeRequest("제목", "내용", NoticeCategory.NOTICE);

        // when
        noticeService.createNotice(request);

        // then
        List<Notice> notices = noticeRepository.findAll();
        assertThat(notices).hasSize(1);
    }

    @Test
    @DisplayName("공지사항을 수정한다.")
    void updateNotice() {
        // given
        Notice notice = noticeRepository.save(Notice.builder()
                .title("제목")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .build());

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", "수정 내용", NoticeCategory.UPDATE);

        // when
        noticeService.updateNotice(notice.getId(), request);

        // then
        Notice updatedNotice = noticeRepository.findById(notice.getId()).get();

        assertSoftly(softly -> {
            assertThat(updatedNotice.getTitle()).isEqualTo("수정 제목");
            assertThat(updatedNotice.getContent()).isEqualTo("수정 내용");
            assertThat(updatedNotice.getNoticeCategory()).isEqualTo(NoticeCategory.UPDATE);
        });
    }

    @Test
    @DisplayName("공지사항을 일부만 수정한다.")
    void updateNotice_partial() {
        // given
        Notice notice = noticeRepository.save(Notice.builder()
                .title("제목")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .build());

        UpdateNoticeRequest request = new UpdateNoticeRequest("수정 제목", null, null);

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
    @DisplayName("공지사항을 삭제한다.")
    void deleteNotice() {
        // given
        Notice notice = noticeRepository.save(Notice.builder()
                .title("제목")
                .content("내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .build());

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
        noticeRepository.save(Notice.builder()
                .title("공지 제목")
                .content("공지 내용")
                .noticeCategory(NoticeCategory.NOTICE)
                .build());

        noticeRepository.save(Notice.builder()
                .title("이벤트 제목")
                .content("이벤트 내용")
                .noticeCategory(NoticeCategory.EVENT)
                .build());

        GetNoticesRequest request = new GetNoticesRequest("공지", null);
        Pageable pageRequest = PageRequest.of(0, 10);

        // when
        Page<GetNoticeResponse> result = noticeService.getNotices(request, pageRequest);

        // then
        assertSoftly(softly -> {
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().noticeCategory()).isEqualTo(NoticeCategory.NOTICE.getValue());
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
}
