package me.bombom.api.v1.article.dto;

import java.time.LocalDateTime;

public record UpdatePreviousArticleRequest(

        String title,
        String contents,
        LocalDateTime arrivedDateTime,
        Boolean isFixed
) {
}
