package me.bombom.api.v1.article.fixture;

import static org.instancio.Select.field;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.PreviousArticle;
import org.instancio.Instancio;

public class PreviousArticleFixture {

    public static PreviousArticle create(Long newsletterId) {
        return Instancio.of(PreviousArticle.class)
                .set(field(PreviousArticle::getId), null)
                .set(field(PreviousArticle::getNewsletterId), newsletterId)
                .set(field(PreviousArticle::getTitle), "테스트 아티클")
                .set(field(PreviousArticle::getContents), "<p>테스트 내용입니다.</p>")
                .set(field(PreviousArticle::getContentsSummary), "테스트 내용입니다.")
                .set(field(PreviousArticle::getExpectedReadTime), 1)
                .set(field(PreviousArticle::getArrivedDateTime), LocalDateTime.of(2025, 1, 1, 9, 0))
                .set(field(PreviousArticle::isFixed), false)
                .create();
    }
}
