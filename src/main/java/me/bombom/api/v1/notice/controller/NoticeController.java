package me.bombom.api.v1.notice.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.service.NoticeService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/notices")
public class NoticeController implements NoticeControllerApi {

    private final NoticeService noticeService;

    @Override
    @PostMapping
    public void createNotice(@RequestBody CreateNoticeRequest request) {
        noticeService.createNotice(request);
    }
}
