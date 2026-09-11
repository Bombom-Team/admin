package me.bombom.api.v1.inquiry.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.inquiry.domain.InquiryCategory;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.fixture.InquiryCategoryFixture;
import me.bombom.api.v1.inquiry.repository.InquiryCategoryRepository;
import me.bombom.api.v1.inquiry.repository.InquiryRoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ InquiryCategoryService.class, QuerydslConfig.class })
class InquiryCategoryServiceTest {

    @Autowired
    private InquiryCategoryService inquiryCategoryService;

    @Autowired
    private InquiryCategoryRepository inquiryCategoryRepository;

    @Autowired
    private InquiryRoomRepository inquiryRoomRepository;

    @Test
    @DisplayName("카테고리를 생성한다.")
    void 카테고리_생성_성공() {
        // given
        CreateInquiryCategoryRequest request = new CreateInquiryCategoryRequest("뉴스레터");

        // when
        inquiryCategoryService.createCategory(request);

        // then
        List<InquiryCategory> categories = inquiryCategoryRepository.findAll();
        assertThat(categories).hasSize(1);
    }

    @Test
    @DisplayName("이름이 중복되면 카테고리 생성 시 예외가 발생한다.")
    void 카테고리_이름_중복_생성_실패() {
        // given
        inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("뉴스레터"));
        CreateInquiryCategoryRequest request = new CreateInquiryCategoryRequest("뉴스레터");

        // when & then
        assertThatThrownBy(() -> inquiryCategoryService.createCategory(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.DUPLICATED_DATA.getMessage());
    }

    @Test
    @DisplayName("카테고리를 수정한다.")
    void 카테고리_수정_성공() {
        // given
        InquiryCategory category = inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("뉴스레터"));
        UpdateInquiryCategoryRequest request = new UpdateInquiryCategoryRequest("챌린지");

        // when
        inquiryCategoryService.updateCategory(category.getId(), request);

        // then
        InquiryCategory updated = inquiryCategoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("챌린지");
    }

    @Test
    @DisplayName("사용 중이 아닌 카테고리를 삭제한다.")
    void 카테고리_삭제_성공() {
        // given
        InquiryCategory category = inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("뉴스레터"));

        // when
        inquiryCategoryService.deleteCategory(category.getId());

        // then
        assertThat(inquiryCategoryRepository.findById(category.getId())).isEmpty();
    }

    @Test
    @DisplayName("사용 중인 카테고리를 삭제하면 예외가 발생한다.")
    void 사용중인_카테고리_삭제_실패() {
        // given
        InquiryCategory category = inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("뉴스레터"));
        inquiryRoomRepository.save(InquiryRoom.createMemberInquiryRoom(1L, category.getId()));

        // when & then
        assertThatThrownBy(() -> inquiryCategoryService.deleteCategory(category.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage(ErrorDetail.INQUIRY_CATEGORY_IN_USE.getMessage());
    }

    @Test
    @DisplayName("카테고리 목록을 id 오름차순으로 조회한다.")
    void 카테고리_목록_조회_성공() {
        // given
        inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("뉴스레터"));
        inquiryCategoryRepository.save(InquiryCategoryFixture.createCategory("챌린지"));

        // when
        List<InquiryCategoryResponse> result = inquiryCategoryService.getCategories();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(2);
            softly.assertThat(result.get(0).name()).isEqualTo("뉴스레터");
        });
    }
}
