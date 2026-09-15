package me.bombom.api.v1.notice.repository;

import me.bombom.api.v1.notice.domain.Notice;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeRepository extends JpaRepository<Notice, Long>, CustomNoticeRepository {
}
