package me.bombom.api.v1.faq.service;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.domain.FaqCategory;
import me.bombom.api.v1.faq.dto.CreateFaqRequest;
import me.bombom.api.v1.faq.dto.GetFaqResponse;
import me.bombom.api.v1.faq.dto.GetFaqsRequest;
import me.bombom.api.v1.faq.dto.UpdateFaqRequest;
import me.bombom.api.v1.faq.fixture.FaqFixture;
import me.bombom.api.v1.faq.repository.FaqRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

@DataJpaTest
@Import({ FaqService.class, QuerydslConfig.class })
@org.springframework.data.jpa.repository.config.EnableJpaAuditing
@org.springframework.test.context.TestPropertySource(properties = "spring.main.allow-bean-definition-overriding=true")
class FaqServiceTest {

    @Autowired
    private FaqService faqService;

    @Autowired
    private FaqRepository faqRepository;

    @Test
    @DisplayName("FAQ를 등록한다.")
    void createFaq() {
        // given
        CreateFaqRequest request = new CreateFaqRequest("질문", "답변", FaqCategory.FEATURE);

        // when
        faqService.createFaq(request);

        // then
        List<Faq> faqs = faqRepository.findAll();
        assertThat(faqs).hasSize(1);
    }

    @Test
    @DisplayName("FAQ를 수정한다.")
    void updateFaq() {
        // given
        Faq faq = faqRepository.save(FaqFixture.createFaq("질문", "답변", FaqCategory.FEATURE));

        UpdateFaqRequest request = new UpdateFaqRequest("수정 질문", "수정 답변", FaqCategory.ACCOUNT);

        // when
        faqService.updateFaq(faq.getId(), request);

        // then
        Faq updatedFaq = faqRepository.findById(faq.getId()).get();

        assertSoftly(softly -> {
            assertThat(updatedFaq.getQuestion()).isEqualTo("수정 질문");
            assertThat(updatedFaq.getAnswer()).isEqualTo("수정 답변");
            assertThat(updatedFaq.getFaqCategory()).isEqualTo(FaqCategory.ACCOUNT);
        });
    }

    @Test
    @DisplayName("FAQ를 일부만 수정한다.")
    void updateFaq_partial() {
        // given
        Faq faq = faqRepository.save(FaqFixture.createFaq("질문", "답변", FaqCategory.FEATURE));

        UpdateFaqRequest request = new UpdateFaqRequest("수정 질문", null, null);

        // when
        faqService.updateFaq(faq.getId(), request);

        // then
        Faq updatedFaq = faqRepository.findById(faq.getId()).get();

        assertSoftly(softly -> {
            assertThat(updatedFaq.getQuestion()).isEqualTo("수정 질문");
            assertThat(updatedFaq.getAnswer()).isEqualTo("답변");
            assertThat(updatedFaq.getFaqCategory()).isEqualTo(FaqCategory.FEATURE);
        });
    }

    @Test
    @DisplayName("FAQ를 삭제한다.")
    void deleteFaq() {
        // given
        Faq faq = faqRepository.save(FaqFixture.createFaq("질문", "답변", FaqCategory.FEATURE));

        // when
        faqService.deleteFaq(faq.getId());

        // then
        List<Faq> faqs = faqRepository.findAll();
        assertThat(faqs).isEmpty();
    }

    @Test
    @DisplayName("FAQ 목록을 조회한다.")
    void getFaqs() {
        // given
        faqRepository.save(FaqFixture.createFaq("질문1", "답변1", FaqCategory.FEATURE));
        faqRepository.save(FaqFixture.createFaq("질문2", "답변2", FaqCategory.ACCOUNT));

        Pageable pageRequest = PageRequest.of(0, 10);

        // when
        Page<GetFaqResponse> result = faqService.getFaqs(new GetFaqsRequest(null), pageRequest);

        // then
        assertSoftly(softly -> {
            assertThat(result.getContent()).hasSize(2);
        });
    }

    @Test
    @DisplayName("존재하지 않는 FAQ 삭제 시 예외가 발생한다.")
    void deleteFaq_exception() {
        // when & then
        assertThatThrownBy(() -> faqService.deleteFaq(999L))
                .isInstanceOf(me.bombom.api.v1.common.exception.CIllegalArgumentException.class)
                .hasMessage(me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    @DisplayName("FAQ 상세 정보를 조회한다.")
    void getFaq() {
        // given
        Faq faq = faqRepository.save(FaqFixture.createFaq("질문", "답변", FaqCategory.FEATURE));

        // when
        me.bombom.api.v1.faq.dto.GetFaqDetailResponse response = faqService.getFaq(faq.getId());

        // then
        assertSoftly(softly -> {
            assertThat(response.question()).isEqualTo("질문");
            assertThat(response.answer()).isEqualTo("답변");
            assertThat(response.faqCategory()).isEqualTo(FaqCategory.FEATURE);
        });
    }

    @Test
    @DisplayName("존재하지 않는 FAQ 조회 시 예외가 발생한다.")
    void getFaq_exception() {
        // when & then
        assertThatThrownBy(() -> faqService.getFaq(999L))
                .isInstanceOf(me.bombom.api.v1.common.exception.CIllegalArgumentException.class)
                .hasMessage(me.bombom.api.v1.common.exception.ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }
}
