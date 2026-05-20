package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;

public record CreateMaeilMailContentAnswerRequest(

        @NotNull
        MaeilMailTrack track,

        @NotBlank
        @Size(max = 100)
        String title,

        @NotBlank
        String content,

        @NotBlank
        String contentsText,

        @NotBlank
        @Size(max = 50)
        String contentsSummary,

        @Positive
        int expectedReadTime,

        @NotBlank
        String answer
) {
}
