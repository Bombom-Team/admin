package me.bombom.api.v1.newsletterrequest.collector;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ExtractedNewsletterInfoTest {

    private static final List<String> CATEGORY_NAMES = List.of("IT/테크", "경제");

    @Test
    void 목록에_있는_카테고리만_돌려준다() {
        // given
        ExtractedNewsletterInfo info = infoWithCategory(" IT/테크 ");

        // when
        String categoryName = info.categoryNameIn(CATEGORY_NAMES);

        // then
        assertThat(categoryName).isEqualTo("IT/테크");
    }

    @Test
    void 목록에_없는_카테고리는_버린다() {
        // given
        ExtractedNewsletterInfo info = infoWithCategory("개발");

        // when
        String categoryName = info.categoryNameIn(CATEGORY_NAMES);

        // then
        assertThat(categoryName).isNull();
    }

    private static ExtractedNewsletterInfo infoWithCategory(String categoryName) {
        return new ExtractedNewsletterInfo(
                "주간 개발 노트",
                "설명",
                categoryName,
                null,
                null,
                null,
                null,
                null,
                null,
                null
        );
    }
}
