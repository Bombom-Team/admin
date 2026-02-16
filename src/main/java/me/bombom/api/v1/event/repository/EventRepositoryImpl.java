package me.bombom.api.v1.event.repository;

import static me.bombom.api.v1.event.QEvent.event;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.event.EventStatus;
import me.bombom.api.v1.event.dto.GetEventResponse;
import me.bombom.api.v1.event.dto.GetEventsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class EventRepositoryImpl implements CustomEventRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetEventResponse> findEvents(GetEventsRequest request, Pageable pageable) {
        List<GetEventResponse> content = getContent(request, pageable);
        JPAQuery<Long> countQuery = getCountQuery(request);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private List<GetEventResponse> getContent(GetEventsRequest request, Pageable pageable) {
        return queryFactory
                .select(Projections.constructor(
                        GetEventResponse.class,
                        event.id,
                        event.name,
                        event.startTime,
                        event.status))
                .from(event)
                .where(
                        nameContains(request.keyword()),
                        statusEq(request.status()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(event.startTime.desc(), event.id.desc())
                .fetch();
    }

    private JPAQuery<Long> getCountQuery(GetEventsRequest request) {
        return queryFactory
                .select(event.count())
                .from(event)
                .where(nameContains(request.keyword()), statusEq(request.status()));
    }

    private BooleanExpression nameContains(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return event.name.containsIgnoreCase(keyword);
    }

    private BooleanExpression statusEq(EventStatus status) {
        if (status == null) {
            return null;
        }
        return event.status.eq(status);
    }
}

