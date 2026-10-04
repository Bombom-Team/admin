package me.bombom.api.v1.newsletterrequest.collector;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.newsletterrequest.domain.DraftContent;
import me.bombom.api.v1.newsletterrequest.dto.response.CollectTarget;
import me.bombom.api.v1.newsletterrequest.service.NewsletterRequestCollectService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 선점한 초안마다 페이지 수집 → LLM 추출 → 썸네일 업로드를 거쳐 초안을 채운다.
 * 한 건이 실패해도 나머지 건은 계속 수집한다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "newsletter-request.collector", name = "enabled", havingValue = "true")
public class NewsletterRequestCollector {

    private final NewsletterRequestCollectService newsletterRequestCollectService;
    private final NewsletterPageFetcher newsletterPageFetcher;
    private final NewsletterInfoExtractor newsletterInfoExtractor;
    private final NewsletterThumbnailUploader newsletterThumbnailUploader;

    public void collectPending() {
        List<CollectTarget> targets = newsletterRequestCollectService.claimTargets();
        if (targets.isEmpty()) {
            return;
        }
        List<String> categoryNames = newsletterRequestCollectService.getCategoryNames();
        targets.forEach(target -> collect(target, categoryNames));
    }

    private void collect(CollectTarget target, List<String> categoryNames) {
        try {
            DraftContent content = collectContent(target, categoryNames);
            newsletterRequestCollectService.complete(target.draftId(), content);
            log.info("뉴스레터 신청 수집 완료: newsletterRequestId={}", target.newsletterRequestId());
        } catch (RuntimeException exception) {
            log.warn("뉴스레터 신청 수집 실패: newsletterRequestId={}", target.newsletterRequestId(), exception);
            newsletterRequestCollectService.fail(target.draftId(), failureReason(exception));
        }
    }

    private DraftContent collectContent(CollectTarget target, List<String> categoryNames) {
        FetchedNewsletterPage page = newsletterPageFetcher.fetchPage(target.requestedUrl());
        ExtractedNewsletterInfo info = newsletterInfoExtractor.extract(target.requestedName(), page, categoryNames);
        String imageUrl = newsletterThumbnailUploader.upload(thumbnailCandidates(info, page));
        return DraftContent.builder()
                .name(info.name())
                .description(info.description())
                .imageUrl(imageUrl)
                .categoryId(newsletterRequestCollectService.findCategoryId(info.categoryNameIn(categoryNames)))
                .mainPageUrl(orDefault(info.mainPageUrl(), target.requestedUrl()))
                .subscribeUrl(orDefault(info.subscribeUrl(), target.requestedUrl()))
                .issueCycle(info.issueCycle())
                .sender(info.sender())
                .subscribeMethod(info.subscribeMethod())
                .previousNewsletterUrl(info.previousNewsletterUrl())
                .build();
    }

    private static List<String> thumbnailCandidates(ExtractedNewsletterInfo info, FetchedNewsletterPage page) {
        List<String> candidates = new ArrayList<>();
        candidates.add(info.imageUrl());
        candidates.add(page.ogImage());
        candidates.add(page.faviconUrl());
        return candidates;
    }

    private static String orDefault(String value, String defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return value;
    }

    private static String failureReason(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null) {
            return exception.getClass().getSimpleName();
        }
        return message;
    }
}
