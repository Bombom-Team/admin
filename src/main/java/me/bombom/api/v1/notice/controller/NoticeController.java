package me.bombom.api.v1.notice.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.notice.dto.CreateNoticeRequest;
import me.bombom.api.v1.notice.dto.GetNoticeDetailResponse;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.UpdateNoticeRequest;
import me.bombom.api.v1.notice.service.NoticeService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/notices")
public class NoticeController implements NoticeControllerApi {

    private final NoticeService noticeService;

    @Override
    @GetMapping
    public Page<GetNoticeResponse> getNotices(
            @ModelAttribute GetNoticesRequest request,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return noticeService.getNotices(request, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public GetNoticeDetailResponse getNotice(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        return noticeService.getNotice(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createNotice(@Valid @RequestBody CreateNoticeRequest request) {
        noticeService.createNotice(request);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateNotice(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @RequestBody UpdateNoticeRequest request) {
        noticeService.updateNotice(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteNotice(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        noticeService.deleteNotice(id);
    }
}
