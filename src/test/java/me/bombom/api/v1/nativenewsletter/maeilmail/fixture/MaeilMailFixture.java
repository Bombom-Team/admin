package me.bombom.api.v1.nativenewsletter.maeilmail.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import org.instancio.Instancio;

public class MaeilMailFixture {

    public static MaeilMailTopic createTopic(MaeilMailTrack track) {
        return Instancio.of(MaeilMailTopic.class)
                .set(field(MaeilMailTopic::getId), null)
                .set(field(MaeilMailTopic::getTrack), track)
                .set(field(MaeilMailTopic::getName), track.name() + " 기초")
                .set(field(MaeilMailTopic::getDisplayOrder), 1)
                .create();
    }

    public static MaeilMailTopic createTopic(MaeilMailTrack track, String name, int displayOrder) {
        return Instancio.of(MaeilMailTopic.class)
                .set(field(MaeilMailTopic::getId), null)
                .set(field(MaeilMailTopic::getTrack), track)
                .set(field(MaeilMailTopic::getName), name)
                .set(field(MaeilMailTopic::getDisplayOrder), displayOrder)
                .create();
    }

    public static MaeilMailContent createContent(Long topicId, String title) {
        return Instancio.of(MaeilMailContent.class)
                .set(field(MaeilMailContent::getId), null)
                .set(field(MaeilMailContent::getTopicId), topicId)
                .set(field(MaeilMailContent::getTitle), title)
                .set(field(MaeilMailContent::getContent), "콘텐츠 내용")
                .set(field(MaeilMailContent::getContentsText), "콘텐츠 텍스트")
                .set(field(MaeilMailContent::getContentsSummary), "요약")
                .set(field(MaeilMailContent::getExpectedReadTime), 5)
                .create();
    }

    public static MaeilMailContentAnswer createContentAnswer(Long contentId) {
        return Instancio.of(MaeilMailContentAnswer.class)
                .set(field(MaeilMailContentAnswer::getId), null)
                .set(field(MaeilMailContentAnswer::getContentId), contentId)
                .set(field(MaeilMailContentAnswer::getAnswer), "테스트 답변입니다.")
                .create();
    }
}
