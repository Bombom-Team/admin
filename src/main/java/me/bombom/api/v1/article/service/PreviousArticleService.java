package me.bombom.api.v1.article.service;

import me.bombom.api.v1.article.domain.PreviousArticle;
import me.bombom.api.v1.article.dto.CreatePreviousArticleRequest;
import me.bombom.api.v1.article.dto.GetPreviousArticleResponse;
import me.bombom.api.v1.article.dto.UpdatePreviousArticleRequest;
import me.bombom.api.v1.article.repository.PreviousArticleRepository;
import me.bombom.api.v1.article.util.ReadingTimeCalculator;
import me.bombom.api.v1.article.util.SummaryGenerator;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.newsletter.repository.NewsletterRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PreviousArticleService {

    private final PreviousArticleRepository previousArticleRepository;
    private final NewsletterRepository newsletterRepository;

    @Transactional
    public void create(Long newsletterId, CreatePreviousArticleRequest request) {
        validateNewsletterExists(newsletterId);

        String contentsSummary = SummaryGenerator.summarize(request.contents());
        int expectedReadTime = ReadingTimeCalculator.calculate(request.contents());

        PreviousArticle article = PreviousArticle.builder()
                .newsletterId(newsletterId)
                .title(request.title())
                .contents(request.contents())
                .contentsSummary(contentsSummary)
                .expectedReadTime(expectedReadTime)
                .arrivedDateTime(request.arrivedDateTime())
                .isFixed(request.isFixed())
                .build();

        previousArticleRepository.save(article);
    }

    public List<GetPreviousArticleResponse> getPreviousArticles(Long newsletterId) {
        validateNewsletterExists(newsletterId);
        return previousArticleRepository.findByNewsletterIdOrderByArrivedDateTimeDesc(newsletterId)
                .stream()
                .map(GetPreviousArticleResponse::from)
                .toList();
    }

    public GetPreviousArticleResponse getPreviousArticle(Long newsletterId, Long id) {
        validateNewsletterExists(newsletterId);
        PreviousArticle article = findArticle(id);
        validateArticleBelongsToNewsletter(article, newsletterId);
        return GetPreviousArticleResponse.from(article);
    }

    @Transactional
    public void update(Long newsletterId, Long id, UpdatePreviousArticleRequest request) {
        validateNewsletterExists(newsletterId);
        PreviousArticle article = findArticle(id);
        validateArticleBelongsToNewsletter(article, newsletterId);

        String newContentsSummary = null;
        int newExpectedReadTime = 0;
        if (request.contents() != null) {
            newContentsSummary = SummaryGenerator.summarize(request.contents());
            newExpectedReadTime = ReadingTimeCalculator.calculate(request.contents());
        }

        article.update(
                request.title(),
                request.contents(),
                newContentsSummary,
                newExpectedReadTime,
                request.arrivedDateTime(),
                request.isFixed()
        );
    }

    @Transactional
    public void delete(Long newsletterId, Long id) {
        validateNewsletterExists(newsletterId);
        PreviousArticle article = findArticle(id);
        validateArticleBelongsToNewsletter(article, newsletterId);
        previousArticleRepository.delete(article);
    }

    private void validateNewsletterExists(Long newsletterId) {
        if (!newsletterRepository.existsById(newsletterId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "newsletter")
                    .addContext("newsletterId", newsletterId);
        }
    }

    private PreviousArticle findArticle(Long id) {
        return previousArticleRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "previousArticle")
                        .addContext("id", id));
    }

    private void validateArticleBelongsToNewsletter(PreviousArticle article, Long newsletterId) {
        if (!article.getNewsletterId().equals(newsletterId)) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "previousArticle")
                    .addContext("newsletterId", newsletterId);
        }
    }
}
