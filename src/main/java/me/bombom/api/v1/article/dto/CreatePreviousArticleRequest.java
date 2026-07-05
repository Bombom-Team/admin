package me.bombom.api.v1.article.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CreatePreviousArticleRequest(

        @NotBlank
        String title,

        @NotBlank
        String contents,

        @NotNull
        LocalDateTime arrivedDateTime,

        @NotNull
        boolean isFixed
) {
}
