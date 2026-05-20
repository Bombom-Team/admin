package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record GetMaeilMailContentAnswerDetailResponse(

        Long id,
        String contentTitle,
        MaeilMailTrack track,
        String answer
) {

    @QueryProjection
    public GetMaeilMailContentAnswerDetailResponse {
    }
}
