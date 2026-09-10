package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomInquiryRoomRepository {

    Page<InquiryRoom> findRoomsForAdmin(GetInquiryRoomsRequest request, Pageable pageable);
}
