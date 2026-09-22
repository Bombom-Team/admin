package me.bombom.api.v1.notice.repository;

import me.bombom.api.v1.notice.domain.NoticeRepresentative;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepresentativeRepository extends JpaRepository<NoticeRepresentative, Byte> {

    void deleteByNoticeId(Long noticeId);
}
