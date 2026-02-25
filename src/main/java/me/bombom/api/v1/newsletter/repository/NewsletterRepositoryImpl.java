package me.bombom.api.v1.newsletter.repository;

import static me.bombom.api.v1.newsletter.domain.QCategory.category;
import static me.bombom.api.v1.newsletter.domain.QNewsletter.newsletter;
import static me.bombom.api.v1.newsletter.domain.QNewsletterDetail.newsletterDetail;
import static me.bombom.api.v1.newsletter.domain.QNewsletterPreviousPolicy.newsletterPreviousPolicy;
import static me.bombom.api.v1.subscribe.domain.QNewsletterSubscriptionCount.newsletterSubscriptionCount;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterPreviousStrategy;
import me.bombom.api.v1.newsletter.dto.GetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.GetNewsletterSummaryResponse;
import me.bombom.api.v1.newsletter.dto.GetNewslettersRequest;
import me.bombom.api.v1.newsletter.dto.NewsletterSortType;
import me.bombom.api.v1.newsletter.dto.QGetNewsletterResponse;
import me.bombom.api.v1.newsletter.dto.QGetNewsletterSummaryResponse;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class NewsletterRepositoryImpl implements CustomNewsletterRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<GetNewsletterSummaryResponse> findNewsletters(GetNewslettersRequest request, LocalDate suspensionThreshold) {
        return queryFactory
                .select(new QGetNewsletterSummaryResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.imageUrl,
                        category.name,
                        newsletterDetail.issueCycle,
                        newsletterSubscriptionCount.total,
                        newsletterPreviousPolicy.strategy.stringValue(),
                        publicationStatusExpression(suspensionThreshold),
                        newsletter.suspendedAt))
                .from(newsletter)
                .join(newsletterDetail).on(newsletter.detailId.eq(newsletterDetail.id))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .leftJoin(newsletterPreviousPolicy).on(newsletter.id.eq(newsletterPreviousPolicy.newsletterId))
                .leftJoin(newsletterSubscriptionCount).on(newsletter.id.eq(newsletterSubscriptionCount.newsletterId))
                .where(
                        keywordContains(request.keyword()),
                        categoryNameEq(request.category()),
                        previousStrategyEq(request.previousStrategy()))
                .orderBy(getOrderSpecifier(request.sort()))
                .fetch();
    }

    @Override
    public Optional<GetNewsletterResponse> findNewsletter(Long id, LocalDate suspensionThreshold) {
        return Optional.ofNullable(queryFactory
                .select(new QGetNewsletterResponse(
                        newsletter.id,
                        newsletter.name,
                        newsletter.description,
                        newsletter.imageUrl,
                        newsletter.email,
                        category.name,

                        newsletterDetail.mainPageUrl,
                        newsletterDetail.subscribeUrl,
                        newsletterDetail.issueCycle,
                        newsletterSubscriptionCount.total,
                        newsletterDetail.sender,
                        newsletterDetail.previousNewsletterUrl,
                        newsletterDetail.previousAllowed,
                        newsletterDetail.subscribeMethod,

                        newsletterPreviousPolicy.strategy.stringValue(),
                        newsletterPreviousPolicy.fixedCount,
                        newsletterPreviousPolicy.recentCount,
                        newsletterPreviousPolicy.exposureRatio,
                        publicationStatusExpression(suspensionThreshold),
                        newsletter.suspendedAt))
                .from(newsletter)
                .join(newsletterDetail).on(newsletter.detailId.eq(newsletterDetail.id))
                .leftJoin(newsletterPreviousPolicy).on(newsletter.id.eq(newsletterPreviousPolicy.newsletterId))
                .leftJoin(newsletterSubscriptionCount).on(newsletter.id.eq(newsletterSubscriptionCount.newsletterId))
                .join(category).on(newsletter.categoryId.eq(category.id))
                .where(newsletter.id.eq(id))
                .fetchOne());
    }

    private OrderSpecifier<?>[] getOrderSpecifier(NewsletterSortType sort) {
        if (sort == NewsletterSortType.POPULAR) {
            return new OrderSpecifier[] {
                    newsletterSubscriptionCount.total.desc().nullsLast(),
                    newsletter.id.desc()
            };
        }
        return new OrderSpecifier[] { newsletter.id.desc() };
    }

    private BooleanExpression keywordContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return newsletter.name.contains(keyword)
                .or(newsletter.description.contains(keyword))
                .or(newsletterDetail.issueCycle.contains(keyword));
    }

    private BooleanExpression categoryNameEq(String categoryName) {
        if (!StringUtils.hasText(categoryName)) {
            return null;
        }
        return category.name.eq(categoryName);
    }

    private StringExpression publicationStatusExpression(LocalDate threshold) {
        return new CaseBuilder()
                .when(newsletter.status.eq(NewsletterPublicationStatus.SUSPENDED)
                        .and(newsletter.suspendedAt.goe(threshold)))
                .then("SUSPENDED_VISIBLE")
                .when(newsletter.status.eq(NewsletterPublicationStatus.SUSPENDED)
                        .and(newsletter.suspendedAt.lt(threshold)))
                .then("SUSPENDED_HIDDEN")
                .otherwise(newsletter.status.stringValue());
    }

    private BooleanExpression previousStrategyEq(NewsletterPreviousStrategy strategy) {
        if (strategy == null) {
            return null;
        }
        return newsletterPreviousPolicy.strategy.eq(strategy);
    }
}
