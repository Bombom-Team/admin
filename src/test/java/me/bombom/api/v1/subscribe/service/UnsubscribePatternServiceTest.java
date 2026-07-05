package me.bombom.api.v1.subscribe.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.subscribe.domain.UnsubscribePattern;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternUpdateRequest;
import me.bombom.api.v1.subscribe.dto.request.UnsubscribePatternType;
import me.bombom.api.v1.subscribe.dto.response.UnsubscribePatternResponse;
import me.bombom.api.v1.subscribe.fixture.UnsubscribePatternFixture;
import me.bombom.api.v1.subscribe.repository.UnsubscribePatternRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.context.TestPropertySource;

@DataJpaTest
@EnableJpaAuditing
@RecordApplicationEvents
@Import({ UnsubscribePatternService.class, QuerydslConfig.class })
@TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class UnsubscribePatternServiceTest {

    @Autowired
    private UnsubscribePatternService unsubscribePatternService;

    @Autowired
    private UnsubscribePatternRepository unsubscribePatternRepository;

    @Autowired
    private ApplicationEvents applicationEvents;

    @Test
    @DisplayName("구독_해지_패턴을_생성한다")
    void 구독_해지_패턴을_생성한다() {
        // given
        UnsubscribePatternRequest request = new UnsubscribePatternRequest(
                "pattern-key",
                "pattern-value"
        );

        // when
        unsubscribePatternService.createUnsubscribePattern(request);

        // then
        List<UnsubscribePattern> patterns = unsubscribePatternRepository.findAll();
        assertSoftly(softly -> {
            softly.assertThat(patterns).hasSize(1);
            softly.assertThat(patterns.getFirst().getPatternKey()).isEqualTo("pattern-key");
            softly.assertThat(patterns.getFirst().getPatternValue()).isEqualTo("pattern-value");
        });
    }

    @Test
    @DisplayName("일반_구독_해지_패턴_목록을_조회한다")
    void 일반_구독_해지_패턴_목록을_조회한다() {
        // given
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("pattern-key-1", "pattern-value-1"));
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("pattern-key-2", "pattern-value-2"));
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("parse.pattern-key", "pattern-value"));

        // when
        List<UnsubscribePatternResponse> responses =
                unsubscribePatternService.getUnsubscribePatterns(UnsubscribePatternType.AUTO_UNSUBSCRIBE);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.getFirst().patternKey()).isEqualTo("pattern-key-1");
            softly.assertThat(responses.getFirst().patternValue()).isEqualTo("pattern-value-1");
        });
    }

    @Test
    @DisplayName("파싱_구독_해지_패턴_목록을_조회한다")
    void 파싱_구독_해지_패턴_목록을_조회한다() {
        // given
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("pattern-key", "pattern-value"));
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("parse.pattern-key-1", "pattern-value-1"));
        unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("parse.pattern-key-2", "pattern-value-2"));

        // when
        List<UnsubscribePatternResponse> responses =
                unsubscribePatternService.getUnsubscribePatterns(UnsubscribePatternType.PARSE);

        // then
        assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);
            softly.assertThat(responses.getFirst().patternKey()).isEqualTo("parse.pattern-key-1");
            softly.assertThat(responses.getFirst().patternValue()).isEqualTo("pattern-value-1");
        });
    }

    @Test
    @DisplayName("구독_해지_패턴을_단건_조회한다")
    void 구독_해지_패턴을_단건_조회한다() {
        // given
        UnsubscribePattern saved = unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("pattern-key", "pattern-value"));

        // when
        UnsubscribePatternResponse response = unsubscribePatternService.getUnsubscribePattern(saved.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.id()).isEqualTo(saved.getId());
            softly.assertThat(response.patternKey()).isEqualTo("pattern-key");
            softly.assertThat(response.patternValue()).isEqualTo("pattern-value");
        });
    }

    @Test
    @DisplayName("존재하지_않는_구독_해지_패턴_단건_조회시_예외가_발생한다")
    void 존재하지_않는_구독_해지_패턴_단건_조회시_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> unsubscribePatternService.getUnsubscribePattern(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("구독_해지_패턴을_수정한다")
    void 구독_해지_패턴을_수정한다() {
        // given
        UnsubscribePattern saved = unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("pattern-key", "pattern-value"));
        UnsubscribePatternUpdateRequest request = new UnsubscribePatternUpdateRequest("updated-pattern-value");

        // when
        unsubscribePatternService.updateUnsubscribePattern(saved.getId(), request);

        // then
        UnsubscribePattern updated = unsubscribePatternRepository.findById(saved.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getPatternValue()).isEqualTo("updated-pattern-value");
            softly.assertThat(parseUnsubscribePatternUpdatedEventCount()).isZero();
        });
    }

    @Test
    @DisplayName("파싱_구독_해지_패턴_수정시_리로드를_요청한다")
    void 파싱_구독_해지_패턴_수정시_리로드를_요청한다() {
        // given
        UnsubscribePattern saved = unsubscribePatternRepository
                .save(UnsubscribePatternFixture.createUnsubscribePattern("parse.pattern-key", "pattern-value"));
        UnsubscribePatternUpdateRequest request = new UnsubscribePatternUpdateRequest("updated-pattern-value");

        // when
        unsubscribePatternService.updateUnsubscribePattern(saved.getId(), request);

        // then
        UnsubscribePattern updated = unsubscribePatternRepository.findById(saved.getId()).orElseThrow();
        assertSoftly(softly -> {
            softly.assertThat(updated.getPatternValue()).isEqualTo("updated-pattern-value");
            softly.assertThat(parseUnsubscribePatternUpdatedEventCount()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("존재하지_않는_구독_해지_패턴_수정시_예외가_발생한다")
    void 존재하지_않는_구독_해지_패턴_수정시_예외가_발생한다() {
        // given
        UnsubscribePatternUpdateRequest request = new UnsubscribePatternUpdateRequest("updated-pattern-value");

        // when & then
        assertThatThrownBy(() -> unsubscribePatternService.updateUnsubscribePattern(0L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    private long parseUnsubscribePatternUpdatedEventCount() {
        return applicationEvents.stream(ParseUnsubscribePatternUpdatedEvent.class)
                .count();
    }
}
