package me.bombom.api.v1.faq.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.faq.dto.CreateFaqRequest;
import me.bombom.api.v1.faq.dto.GetFaqDetailResponse;
import me.bombom.api.v1.faq.dto.GetFaqResponse;
import me.bombom.api.v1.faq.dto.UpdateFaqRequest;
import me.bombom.api.v1.faq.service.FaqService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.SortDefault;
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
@RequestMapping("/admin/api/v1/faqs")
public class FaqController implements FaqControllerApi {

    private final FaqService faqService;

    @Override
    @GetMapping
    public Page<GetFaqResponse> getFaqs(
            @PageableDefault(size = 20)
            @SortDefault.SortDefaults({
                    @SortDefault(sort = "createdAt", direction = Sort.Direction.DESC),
                    @SortDefault(sort = "id", direction = Sort.Direction.ASC)
            }) Pageable pageable
    ) {
        return faqService.getFaqs(pageable);
    }

    @Override
    @GetMapping("/{id}")
    public GetFaqDetailResponse getFaq(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        return faqService.getFaq(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createFaq(@Valid @RequestBody CreateFaqRequest request) {
        faqService.createFaq(request);
    }

    @Override
    @PatchMapping("/{id}")
    public void updateFaq(
            @PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id,
            @RequestBody UpdateFaqRequest request) {
        faqService.updateFaq(id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteFaq(@PathVariable @Positive(message = "id는 1 이상의 값이어야 합니다.") Long id) {
        faqService.deleteFaq(id);
    }
}
