package me.bombom.api.v1.article.controller;

import me.bombom.api.v1.article.dto.CreatePreviousArticleRequest;
import me.bombom.api.v1.article.dto.GetPreviousArticleResponse;
import me.bombom.api.v1.article.dto.UpdatePreviousArticleRequest;
import me.bombom.api.v1.article.service.PreviousArticleService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/newsletters/{newsletterId}/articles/previous")
public class PreviousArticleController implements PreviousArticleControllerApi {

    private final PreviousArticleService previousArticleService;

    @Override
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void createPreviousArticle(
            @PathVariable Long newsletterId,
            @Valid @RequestBody CreatePreviousArticleRequest request
    ) {
        previousArticleService.create(newsletterId, request);
    }

    @Override
    @GetMapping
    public List<GetPreviousArticleResponse> getPreviousArticles(@PathVariable Long newsletterId) {
        return previousArticleService.getPreviousArticles(newsletterId);
    }

    @Override
    @GetMapping("/{id}")
    public GetPreviousArticleResponse getPreviousArticle(
            @PathVariable Long newsletterId,
            @PathVariable Long id
    ) {
        return previousArticleService.getPreviousArticle(newsletterId, id);
    }

    @Override
    @PatchMapping("/{id}")
    public void updatePreviousArticle(
            @PathVariable Long newsletterId,
            @PathVariable Long id,
            @RequestBody UpdatePreviousArticleRequest request
    ) {
        previousArticleService.update(newsletterId, id, request);
    }

    @Override
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePreviousArticle(
            @PathVariable Long newsletterId,
            @PathVariable Long id
    ) {
        previousArticleService.delete(newsletterId, id);
    }
}
