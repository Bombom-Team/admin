package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record GetMaeilMailContentAnswersRequest(

        MaeilMailTrack track,
        String title
) {
}
