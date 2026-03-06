package me.bombom.api.v1.article.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.PreviousArticle;

public record GetPreviousArticleResponse(

        Long id,
        String title,
        String contents,
        String contentsSummary,
        int expectedReadTime,
        LocalDateTime arrivedDateTime,
        boolean isFixed,
        Long newsletterId
) {

    public static GetPreviousArticleResponse from(PreviousArticle article) {
        return new GetPreviousArticleResponse(
                article.getId(),
                article.getTitle(),
                article.getContents(),
                article.getContentsSummary(),
                article.getExpectedReadTime(),
                article.getArrivedDateTime(),
                article.isFixed(),
                article.getNewsletterId()
        );
    }
}
