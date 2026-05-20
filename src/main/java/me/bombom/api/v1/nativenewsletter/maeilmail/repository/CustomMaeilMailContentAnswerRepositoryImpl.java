package me.bombom.api.v1.nativenewsletter.maeilmail.repository;

import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailContent.maeilMailContent;
import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailContentAnswer.maeilMailContentAnswer;
import static me.bombom.api.v1.nativenewsletter.maeilmail.domain.QMaeilMailTopic.maeilMailTopic;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.nativenewsletter.maeilmail.domain.MaeilMailTrack;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswerResponse;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.GetMaeilMailContentAnswersRequest;
import me.bombom.api.v1.nativenewsletter.maeilmail.dto.QGetMaeilMailContentAnswerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomMaeilMailContentAnswerRepositoryImpl implements CustomMaeilMailContentAnswerRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetMaeilMailContentAnswerResponse> getContentAnswers(
            GetMaeilMailContentAnswersRequest request,
            Pageable pageable
    ) {
        List<GetMaeilMailContentAnswerResponse> content = queryFactory
                .select(new QGetMaeilMailContentAnswerResponse(
                        maeilMailContentAnswer.id,
                        maeilMailContent.title,
                        maeilMailTopic.track))
                .from(maeilMailContentAnswer)
                .join(maeilMailContent).on(maeilMailContentAnswer.contentId.eq(maeilMailContent.id))
                .join(maeilMailTopic).on(maeilMailContent.topicId.eq(maeilMailTopic.id))
                .where(
                        filterByTrack(request.track()),
                        filterByTitle(request.title()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(maeilMailContentAnswer.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(maeilMailContentAnswer.count())
                .from(maeilMailContentAnswer)
                .join(maeilMailContent).on(maeilMailContentAnswer.contentId.eq(maeilMailContent.id))
                .join(maeilMailTopic).on(maeilMailContent.topicId.eq(maeilMailTopic.id))
                .where(
                        filterByTrack(request.track()),
                        filterByTitle(request.title()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression filterByTrack(MaeilMailTrack track) {
        if (track == null) {
            return null;
        }
        return maeilMailTopic.track.eq(track);
    }

    private BooleanExpression filterByTitle(String title) {
        if (title == null || title.isBlank()) {
            return null;
        }
        return maeilMailContent.title.contains(title);
    }
}
