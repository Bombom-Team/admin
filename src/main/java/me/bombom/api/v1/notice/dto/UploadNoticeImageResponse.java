package me.bombom.api.v1.notice.dto;

import me.bombom.api.v1.notice.domain.NoticeImageAsset;

public record UploadNoticeImageResponse(

        Long imageId,
        String imageUrl
) {

    public static UploadNoticeImageResponse from(NoticeImageAsset noticeImageAsset) {
        return new UploadNoticeImageResponse(noticeImageAsset.getId(), noticeImageAsset.getImageUrl());
    }
}
