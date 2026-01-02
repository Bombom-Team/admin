package me.bombom.api.v1.challenge.repository;

import static me.bombom.api.v1.challenge.domain.QChallenge.challenge;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeStatus;
import me.bombom.api.v1.challenge.dto.GetChallengeResponse;
import me.bombom.api.v1.challenge.dto.GetChallengesRequest;
import me.bombom.api.v1.challenge.dto.QGetChallengeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomChallengeRepositoryImpl implements CustomChallengeRepository {

        private final JPAQueryFactory queryFactory;

        @Override
        public Page<GetChallengeResponse> getChallenges(GetChallengesRequest request, Pageable pageable) {
                List<GetChallengeResponse> content = queryFactory
                                .select(new QGetChallengeResponse(
                                                challenge.id,
                                                challenge.name,
                                                challenge.generation,
                                                challenge.startDate,
                                                challenge.endDate)
                                ).where(filterByStatus(request.status()))
                                .offset(pageable.getOffset())
                                .limit(pageable.getPageSize())
                                .orderBy(challenge.id.desc())
                                .fetch();

                JPAQuery<Long> countQuery = queryFactory
                                .select(challenge.count())
                                .from(challenge)
                                .where(filterByStatus(request.status()));

                return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
        }

        private BooleanExpression filterByStatus(ChallengeStatus status) {
                if (status == null) {
                        return null;
                }

                java.time.LocalDate now = java.time.LocalDate.now();

                return switch (status) {
                        case BEFORE_START -> challenge.startDate.after(now);
                        case ONGOING -> challenge.startDate.loe(now).and(challenge.endDate.goe(now));
                        case COMPLETED -> challenge.endDate.before(now);
                };
        }
}
