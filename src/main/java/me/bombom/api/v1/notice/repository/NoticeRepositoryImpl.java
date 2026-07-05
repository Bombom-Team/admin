package me.bombom.api.v1.notice.repository;

import me.bombom.api.v1.notice.domain.NoticeCategory;
import me.bombom.api.v1.notice.dto.GetNoticeResponse;
import me.bombom.api.v1.notice.dto.GetNoticesRequest;
import me.bombom.api.v1.notice.dto.QGetNoticeResponse;

import static me.bombom.api.v1.notice.domain.QNotice.notice;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@RequiredArgsConstructor
public class NoticeRepositoryImpl implements CustomNoticeRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetNoticeResponse> findNotices(GetNoticesRequest request, Pageable pageable) {
        List<GetNoticeResponse> content = getContent(request, pageable);

        JPAQuery<Long> countQuery = getCountQuery(request);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<GetNoticeResponse> getContent(GetNoticesRequest request, Pageable pageable) {
        return queryFactory
                .select(
                        new QGetNoticeResponse(
                                notice.id,
                                notice.title,
                                notice.noticeCategory.stringValue(),
                                notice.createdAt))
                .from(notice)
                .where(
                        titleOrContentContains(request.keyword()),
                        categoryEq(request.category()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(notice.createdAt.desc(), notice.id.desc())
                .fetch();
    }

    private JPAQuery<Long> getCountQuery(GetNoticesRequest request) {
        return queryFactory
                .select(notice.count())
                .from(notice)
                .where(
                        titleOrContentContains(request.keyword()),
                        categoryEq(request.category()));
    }

    private BooleanExpression titleOrContentContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return notice.title.contains(keyword)
                .or(notice.content.contains(keyword));
    }

    private BooleanExpression categoryEq(NoticeCategory category) {
        if (category == null) {
            return null;
        }
        return notice.noticeCategory.eq(category);
    }
}
