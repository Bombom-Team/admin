package me.bombom.api.v1.newsletter.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.dto.CreateCategoryRequest;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import me.bombom.api.v1.newsletter.dto.UpdateCategoryRequest;
import me.bombom.api.v1.newsletter.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional
    public void create(CreateCategoryRequest request) {
        categoryRepository.findByName(request.name()).ifPresent(c -> {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext("name", request.name());
        });
        categoryRepository.save(
                Category.builder()
                        .name(request.name())
                        .build()
        );
    }

    public List<GetCategoryResponse> getCategories() {
        return categoryRepository.findAllAsResponse();
    }

    @Transactional
    public void update(Long id, UpdateCategoryRequest request) {
        Category category = getCategoryById(id);
        categoryRepository.findByName(request.name()).ifPresent(c -> {
            if (!c.getId().equals(id)) {
                throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                        .addContext("name", request.name());
            }
        });
        category.update(request.name());
    }

    @Transactional
    public void delete(Long id) {
        Category category = getCategoryById(id);
        categoryRepository.delete(category);
    }

    private Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("categoryId", id));
    }
}
