package me.bombom.api.v1.inquiry.repository;

import me.bombom.api.v1.inquiry.domain.InquiryMessageArrivalNotification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryMessageArrivalNotificationRepository extends
        JpaRepository<InquiryMessageArrivalNotification, Long> {
}
