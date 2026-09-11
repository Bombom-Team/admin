package me.bombom.api.v1.inquiry.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.request.CreateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.request.UpdateInquiryCategoryRequest;
import me.bombom.api.v1.inquiry.dto.response.InquiryCategoryResponse;
import me.bombom.api.v1.inquiry.service.InquiryCategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/inquiries/categories")
public class InquiryCategoryController implements InquiryCategoryControllerApi {

    private final InquiryCategoryService inquiryCategoryService;

    @Override
    @GetMapping
    public List<InquiryCategoryResponse> getCategories() {
        return inquiryCategoryService.getCategories();
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createCategory(@Valid @RequestBody CreateInquiryCategoryRequest request) {
        inquiryCategoryService.createCategory(request);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateCategory(
            @PathVariable @Positive Long id,
            @Valid @RequestBody UpdateInquiryCategoryRequest request) {
        inquiryCategoryService.updateCategory(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable @Positive Long id) {
        inquiryCategoryService.deleteCategory(id);
    }
}
