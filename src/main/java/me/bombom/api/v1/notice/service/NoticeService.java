package me.bombom.api.v1.notice.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.repository.NoticeRepository;
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
