package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record GetMaeilMailContentAnswerResponse(

        Long id,
        String contentTitle,
        MaeilMailTrack track
) {

    @QueryProjection
    public GetMaeilMailContentAnswerResponse {
    }
}
