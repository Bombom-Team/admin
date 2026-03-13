package me.bombom.api.v1.newsletter.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.dto.CreateCategoryRequest;
import me.bombom.api.v1.newsletter.dto.GetCategoryResponse;
import me.bombom.api.v1.newsletter.dto.UpdateCategoryRequest;
import me.bombom.api.v1.newsletter.service.CategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/categories")
public class CategoryController implements CategoryControllerApi {

    private final CategoryService categoryService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        categoryService.create(request);
    }

    @Override
    @GetMapping
    public List<GetCategoryResponse> getCategories() {
        return categoryService.getCategories();
    }

    @Override
    @PatchMapping("/{id}")
    public void updateCategory(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        categoryService.update(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id) {
        categoryService.delete(id);
    }
}
