package me.bombom.api.v1.faq.repository;

import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.domain.FaqCategory;
import me.bombom.api.v1.faq.dto.GetFaqsRequest;

import static me.bombom.api.v1.faq.domain.QFaq.faq;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

@RequiredArgsConstructor
public class FaqRepositoryImpl implements CustomFaqRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Faq> findFaqs(GetFaqsRequest request, Pageable pageable) {
        List<Faq> content = queryFactory
                .selectFrom(faq)
                .where(categoryEq(request.faqCategory()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(faq.createdAt.desc(), faq.id.asc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(faq.count())
                .from(faq)
                .where(categoryEq(request.faqCategory()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression categoryEq(FaqCategory category) {
        if (category == null) {
            return null;
        }
        return faq.faqCategory.eq(category);
    }
}
