package me.bombom.api.v1.challenge.repository;

import static me.bombom.api.v1.challenge.domain.QChallengeParticipant.challengeParticipant;
import static me.bombom.api.v1.challenge.domain.QChallengeTeam.challengeTeam;
import static me.bombom.api.v1.member.domain.QMember.member;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantResponse;
import me.bombom.api.v1.challenge.dto.GetChallengeParticipantsRequest;
import me.bombom.api.v1.challenge.dto.QGetChallengeParticipantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CustomChallengeParticipantRepositoryImpl implements CustomChallengeParticipantRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GetChallengeParticipantResponse> getChallengeParticipants(Long challengeId,
            GetChallengeParticipantsRequest request, Pageable pageable) {
        List<GetChallengeParticipantResponse> content = queryFactory
                .select(new QGetChallengeParticipantResponse(
                        member.id,
                        member.nickname,
                        challengeTeam.id,
                        challengeParticipant.completedDays,
                        challengeParticipant.isSurvived))
                .from(challengeParticipant)
                .join(member).on(challengeParticipant.memberId.eq(member.id))
                .leftJoin(challengeTeam).on(challengeParticipant.challengeTeamId.eq(challengeTeam.id))
                .where(
                        challengeParticipant.challengeId.eq(challengeId),
                        eqChallengeTeamId(request.challengeTeamId()),
                        filterHasTeam(request.hasTeam())
                )
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(challengeParticipant.id.asc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(challengeParticipant.count())
                .from(challengeParticipant)
                .where(
                        challengeParticipant.challengeId.eq(challengeId),
                        eqChallengeTeamId(request.challengeTeamId()),
                        filterHasTeam(request.hasTeam()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression eqChallengeTeamId(Long challengeTeamId) {
        if (challengeTeamId == null) {
            return null;
        }
        return challengeParticipant.challengeTeamId.eq(challengeTeamId);
    }

    private BooleanExpression filterHasTeam(boolean hasTeam) {
        if (hasTeam) {
            return challengeParticipant.challengeTeamId.isNotNull();
        }
        return challengeParticipant.challengeTeamId.isNull();
    }
}
