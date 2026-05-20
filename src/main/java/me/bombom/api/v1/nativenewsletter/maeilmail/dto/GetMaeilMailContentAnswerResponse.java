package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record GetMaeilMailContentAnswerResponse(

        Long id,
        Long contentId,
        String contentTitle,
        MaeilMailTrack track,
        String topicName,
        String answer,
        LocalDateTime createdAt
) {

    @QueryProjection
    public GetMaeilMailContentAnswerResponse {
    }
}
