package me.bombom.api.v1.notice.repository;

import java.util.Collection;
import java.util.List;
import me.bombom.api.v1.notice.domain.NoticeImageAsset;
import me.bombom.api.v1.notice.domain.NoticeImageAssetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NoticeImageAssetRepository extends JpaRepository<NoticeImageAsset, Long> {

    List<NoticeImageAsset> findAllByNoticeId(Long noticeId);

    List<NoticeImageAsset> findAllByNoticeIdAndStatus(Long noticeId, NoticeImageAssetStatus status);

    List<NoticeImageAsset> findAllByIdIn(Collection<Long> ids);
}
