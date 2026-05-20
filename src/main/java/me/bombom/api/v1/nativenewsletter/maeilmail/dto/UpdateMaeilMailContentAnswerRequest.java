package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.Size;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record UpdateMaeilMailContentAnswerRequest(

        MaeilMailTrack track,

        @Size(max = 100) String title,

        String content,

        String contentsText,

        @Size(max = 50) String contentsSummary,

        Integer expectedReadTime,

        String answer
) {
}
