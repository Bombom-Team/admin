package news.bombomadmin.notice.controller;

import lombok.RequiredArgsConstructor;
import news.bombomadmin.notice.dto.CreateNoticeRequest;
import news.bombomadmin.notice.service.NoticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/notices")
public class NoticeController {

    private final NoticeService noticeService;

    @PostMapping
    public void createNotice(@RequestBody CreateNoticeRequest request) {
        noticeService.createNotice(request);
    }
}
