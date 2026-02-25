package me.bombom.api.v1.notice.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.notice.domain.Notice;
import me.bombom.api.v1.notice.domain.NoticeCategory;
import org.instancio.Instancio;

public class NoticeFixture {

    public static Notice createNotice() {
        return createNotice("제목", "내용", NoticeCategory.NOTICE);
    }

    public static Notice createNotice(String title, String content, NoticeCategory category) {
        return Instancio.of(Notice.class)
                .set(field(Notice::getId), null)
                .set(field(Notice::getTitle), title)
                .set(field(Notice::getContent), content)
                .set(field(Notice::getNoticeCategory), category)
                .create();
    }
}
