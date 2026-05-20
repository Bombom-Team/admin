package me.bombom.api.v1.nativenewsletter.maeilmail.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerDetailResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.service.MaeilMailContentAnswerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/maeil-mail/content-answers")
public class MaeilMailContentAnswerController implements MaeilMailContentAnswerControllerApi {

    private final MaeilMailContentAnswerService contentAnswerService;

    @Override
    @GetMapping
    public Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            @ModelAttribute GetMaeilMailContentAnswersRequest request,
            @PageableDefault(sort = "id", direction = Direction.ASC) Pageable pageable
    ) {
        return contentAnswerService.getContentAnswers(request, pageable);
    }

    @Override
    @GetMapping("/{id}")
    public GetMaeilMailContentAnswerDetailResponse getContentAnswer(@PathVariable Long id) {
        return contentAnswerService.getContentAnswer(id);
    }

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createContentAnswer(@Valid @RequestBody CreateMaeilMailContentAnswerRequest request) {
        contentAnswerService.createContentAnswer(request);
    }

    }
}
