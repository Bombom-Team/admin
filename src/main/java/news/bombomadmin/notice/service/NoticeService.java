package news.bombomadmin.notice.service;

import lombok.RequiredArgsConstructor;
import news.bombomadmin.notice.domain.Notice;
import news.bombomadmin.notice.dto.CreateNoticeRequest;
import news.bombomadmin.notice.repository.NoticeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private final NoticeRepository noticeRepository;

    @Transactional
    public void createNotice(CreateNoticeRequest request) {
        Notice notice = Notice.builder()
                .title(request.title())
                .content(request.content())
                .noticeCategory(request.noticeCategory())
                .build();
        noticeRepository.save(notice);
    }
}
