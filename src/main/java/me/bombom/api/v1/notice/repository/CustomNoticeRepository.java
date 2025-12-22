package me.bombom.api.v1.notice.repository;

import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomNoticeRepository {

    Page<GetNoticeResponse> findNotices(GetNoticesRequest request, Pageable pageable);
}
