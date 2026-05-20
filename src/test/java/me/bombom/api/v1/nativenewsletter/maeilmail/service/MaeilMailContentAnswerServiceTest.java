package me.bombom.api.v1.nativenewsletter.maeilmail.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContent;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailContentAnswer;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTopic;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.CreateMaeilMailContentAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.UpdateMaeilMailContentAnswerRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.fixture.MaeilMailFixture;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentAnswerRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailContentRepository;
import me.bombom.api.v1.nativenewsletter.maeilmail.repository.MaeilMailTopicRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect"
})
@Import({ MaeilMailContentAnswerService.class, QuerydslConfig.class })
class MaeilMailContentAnswerServiceTest {

    @Autowired
    private MaeilMailContentAnswerService contentAnswerService;

    @Autowired
    private MaeilMailContentAnswerRepository contentAnswerRepository;

    @Autowired
    private MaeilMailContentRepository contentRepository;

    @Autowired
    private MaeilMailTopicRepository topicRepository;

    @Test
    void 필터_없이_전체_답변_목록을_조회한다() {
        // given
        MaeilMailTopic beTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailTopic feTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.FE));

        MaeilMailContent beContent = contentRepository.save(MaeilMailFixture.createContent(beTopic.getId(), "자바 기초"));
        MaeilMailContent feContent = contentRepository.save(MaeilMailFixture.createContent(feTopic.getId(), "리액트 기초"));

        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(beContent.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(feContent.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(null, null);

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isEqualTo(2);
    }

    @Test
    void track_필터로_BE_답변만_조회한다() {
        // given
        MaeilMailTopic beTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailTopic feTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.FE));

        MaeilMailContent beContent = contentRepository.save(MaeilMailFixture.createContent(beTopic.getId(), "자바 기초"));
        MaeilMailContent feContent = contentRepository.save(MaeilMailFixture.createContent(feTopic.getId(), "리액트 기초"));

        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(beContent.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(feContent.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(MaeilMailTrack.BE, null);

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent().get(0).track()).isEqualTo(MaeilMailTrack.BE);
            softly.assertThat(result.getContent().get(0).contentTitle()).isEqualTo("자바 기초");
        });
    }

    @Test
    void title_검색으로_부분_일치하는_답변만_조회한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));

        MaeilMailContent content1 = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        MaeilMailContent content2 = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 OOP"));
        MaeilMailContent content3 = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "스프링 입문"));

        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content1.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content2.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content3.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(null, "자바");

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(2);
            softly.assertThat(result.getContent())
                    .extracting(GetMaeilMailContentAnswerResponse::contentTitle)
                    .containsExactlyInAnyOrder("자바 기초", "자바 OOP");
        });
    }

    @Test
    void track과_title_복합_필터로_조회한다() {
        // given
        MaeilMailTopic beTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailTopic feTopic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.FE));

        MaeilMailContent beJavaContent = contentRepository.save(MaeilMailFixture.createContent(beTopic.getId(), "자바 기초"));
        MaeilMailContent feJavaContent = contentRepository.save(MaeilMailFixture.createContent(feTopic.getId(), "자바스크립트 기초"));
        MaeilMailContent beSpringContent = contentRepository.save(MaeilMailFixture.createContent(beTopic.getId(), "스프링 입문"));

        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(beJavaContent.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(feJavaContent.getId()));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(beSpringContent.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(MaeilMailTrack.BE, "자바");

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getTotalElements()).isEqualTo(1);
            softly.assertThat(result.getContent().get(0).contentTitle()).isEqualTo("자바 기초");
            softly.assertThat(result.getContent().get(0).track()).isEqualTo(MaeilMailTrack.BE);
        });
    }

    @Test
    void 응답에_track과_contentTitle이_포함된다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(null, null);

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        GetMaeilMailContentAnswerResponse response = result.getContent().get(0);
        assertSoftly(softly -> {
            softly.assertThat(response.track()).isEqualTo(MaeilMailTrack.BE);
            softly.assertThat(response.contentTitle()).isEqualTo("자바 기초");
        });
    }

    @Test
    void 페이지네이션이_동작한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));

        for (int i = 1; i <= 5; i++) {
            MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "콘텐츠 " + i));
            contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));
        }

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(null, null);

        // when
        Page<GetMaeilMailContentAnswerResponse> firstPage = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 3));
        Page<GetMaeilMailContentAnswerResponse> secondPage = contentAnswerService.getContentAnswers(
                request, PageRequest.of(1, 3));

        // then
        assertSoftly(softly -> {
            softly.assertThat(firstPage.getTotalElements()).isEqualTo(5);
            softly.assertThat(firstPage.getContent()).hasSize(3);
            softly.assertThat(secondPage.getContent()).hasSize(2);
        });
    }

    @Test
    void 조건에_맞는_답변이_없으면_빈_목록을_반환한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));

        GetMaeilMailContentAnswersRequest request = new GetMaeilMailContentAnswersRequest(MaeilMailTrack.FE, null);

        // when
        Page<GetMaeilMailContentAnswerResponse> result = contentAnswerService.getContentAnswers(
                request, PageRequest.of(0, 10));

        // then
        assertThat(result.getTotalElements()).isZero();
    }

    @Test
    void 답변_단건을_조회한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        MaeilMailContentAnswer answer = contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));

        // when
        GetMaeilMailContentAnswerDetailResponse result = contentAnswerService.getContentAnswer(answer.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.id()).isEqualTo(answer.getId());
            softly.assertThat(result.contentTitle()).isEqualTo("자바 기초");
            softly.assertThat(result.track()).isEqualTo(MaeilMailTrack.BE);
            softly.assertThat(result.answer()).isEqualTo("테스트 답변입니다.");
        });
    }

    @Test
    void 존재하지_않는_답변을_조회하면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> contentAnswerService.getContentAnswer(999L))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @Test
    void 답변을_생성하면_콘텐츠와_답변이_함께_저장된다() {
        // given
        topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        CreateMaeilMailContentAnswerRequest request = new CreateMaeilMailContentAnswerRequest(
                MaeilMailTrack.BE, "자바 기초", "콘텐츠 내용", "콘텐츠 텍스트", "요약", 5, "정답입니다.");

        // when
        contentAnswerService.createContentAnswer(request);

        // then
        assertSoftly(softly -> {
            softly.assertThat(contentRepository.findAll()).hasSize(1);
            softly.assertThat(contentAnswerRepository.findAll()).hasSize(1);
            softly.assertThat(contentRepository.findAll().get(0).getTitle()).isEqualTo("자바 기초");
        });
    }

    @Test
    void topic이_없는_track으로_생성_시_예외가_발생한다() {
        // given: topic 미등록 상태
        CreateMaeilMailContentAnswerRequest request = new CreateMaeilMailContentAnswerRequest(
                MaeilMailTrack.BE, "자바 기초", "콘텐츠 내용", "콘텐츠 텍스트", "요약", 5, "정답입니다.");

        // when & then
        assertThatThrownBy(() -> contentAnswerService.createContentAnswer(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 답변을_수정한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        MaeilMailContentAnswer answer = contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));

        // when
        contentAnswerService.updateContentAnswer(answer.getId(), new UpdateMaeilMailContentAnswerRequest("수정된 답변입니다."));

        // then
        MaeilMailContentAnswer updated = contentAnswerRepository.findById(answer.getId()).get();
        assertThat(updated.getAnswer()).isEqualTo("수정된 답변입니다.");
    }

    @Test
    void 존재하지_않는_답변_수정_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> contentAnswerService.updateContentAnswer(
                999L, new UpdateMaeilMailContentAnswerRequest("수정된 답변")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 답변을_삭제한다() {
        // given
        MaeilMailTopic topic = topicRepository.save(MaeilMailFixture.createTopic(MaeilMailTrack.BE));
        MaeilMailContent content = contentRepository.save(MaeilMailFixture.createContent(topic.getId(), "자바 기초"));
        MaeilMailContentAnswer answer = contentAnswerRepository.save(MaeilMailFixture.createContentAnswer(content.getId()));

        // when
        contentAnswerService.deleteContentAnswer(answer.getId());

        // then
        assertThat(contentAnswerRepository.existsById(answer.getId())).isFalse();
    }

    @Test
    void 존재하지_않는_답변_삭제_시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> contentAnswerService.deleteContentAnswer(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
