package me.bombom.api.v1.notice.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.repository.NoticeRepository;
import me.bombom.api.v1.notice.service.NoticeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

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
}
