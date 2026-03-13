package me.bombom.api.v1.newsletter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.dto.CreateCategoryRequest;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import me.bombom.api.v1.newsletter.dto.UpdateCategoryRequest;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({ CategoryService.class, QuerydslConfig.class })
class CategoryServiceTest {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("카테고리를 생성한다.")
    void createCategory() {
        // given
        CreateCategoryRequest request = new CreateCategoryRequest("테크");

        // when
        categoryService.create(request);

        // then
        assertThat(categoryRepository.findAll()).hasSize(1);
        assertThat(categoryRepository.findByName("테크")).isPresent();
    }

    @Test
    @DisplayName("중복된 이름으로 생성 시 예외가 발생한다.")
    void createCategory_duplicateName() {
        // given
        categoryRepository.save(Category.builder().name("테크").build());
        CreateCategoryRequest request = new CreateCategoryRequest("테크");

        // when & then
        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("이미 존재하는 데이터입니다.");
    }

    @Test
    @DisplayName("카테고리 목록을 id 오름차순으로 조회한다.")
    void getCategories() {
        // given
        categoryRepository.save(Category.builder().name("경제").build());
        categoryRepository.save(Category.builder().name("테크").build());
        categoryRepository.save(Category.builder().name("라이프").build());

        // when
        List<GetCategoryResponse> result = categoryService.getCategories();

        // then
        assertSoftly(softly -> {
            softly.assertThat(result).hasSize(3);
            softly.assertThat(result.get(0).name()).isEqualTo("경제");
            softly.assertThat(result.get(1).name()).isEqualTo("테크");
            softly.assertThat(result.get(2).name()).isEqualTo("라이프");
        });
    }

    @Test
    @DisplayName("카테고리 이름을 수정한다.")
    void updateCategory() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        UpdateCategoryRequest request = new UpdateCategoryRequest("경제");

        // when
        categoryService.update(category.getId(), request);

        // then
        Category updated = categoryRepository.findById(category.getId()).orElseThrow();
        assertThat(updated.getName()).isEqualTo("경제");
    }

    @Test
    @DisplayName("다른 카테고리에 이미 존재하는 이름으로 수정 시 예외가 발생한다.")
    void updateCategory_duplicateName() {
        // given
        categoryRepository.save(Category.builder().name("경제").build());
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        UpdateCategoryRequest request = new UpdateCategoryRequest("경제");

        // when & then
        assertThatThrownBy(() -> categoryService.update(category.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("이미 존재하는 데이터입니다.");
    }

    @Test
    @DisplayName("같은 이름으로 수정해도 예외가 발생하지 않는다.")
    void updateCategory_sameName() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());
        UpdateCategoryRequest request = new UpdateCategoryRequest("테크");

        // when & then (no exception)
        categoryService.update(category.getId(), request);
        assertThat(categoryRepository.findById(category.getId()).orElseThrow().getName()).isEqualTo("테크");
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 수정 시 예외가 발생한다.")
    void updateCategory_notFound() {
        // given
        UpdateCategoryRequest request = new UpdateCategoryRequest("경제");

        // when & then
        assertThatThrownBy(() -> categoryService.update(999L, request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }

    @Test
    @DisplayName("카테고리를 삭제한다.")
    void deleteCategory() {
        // given
        Category category = categoryRepository.save(Category.builder().name("테크").build());

        // when
        categoryService.delete(category.getId());

        // then
        assertThat(categoryRepository.existsById(category.getId())).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 카테고리 삭제 시 예외가 발생한다.")
    void deleteCategory_notFound() {
        // when & then
        assertThatThrownBy(() -> categoryService.delete(999L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessage("존재하지 않는 데이터입니다.");
    }
}
