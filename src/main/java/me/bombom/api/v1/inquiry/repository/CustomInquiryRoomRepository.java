package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomInquiryRoomRepository {

    Page<InquiryRoom> findRoomsForAdmin(InquiryStatus status, Long assigneeId, Long categoryId, Pageable pageable);
}
