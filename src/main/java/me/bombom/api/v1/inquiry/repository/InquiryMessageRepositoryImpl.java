package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryMessage;

@RequiredArgsConstructor
public class InquiryMessageRepositoryImpl implements CustomInquiryMessageRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<InquiryMessage> findMessagesByCursor(Long roomId, Long cursor, int size) {
        return queryFactory
                .selectFrom(inquiryMessage)
                .where(
                        inquiryMessage.roomId.eq(roomId),
                        cursorLt(cursor))
                .orderBy(inquiryMessage.id.desc())
                .limit(size)
                .fetch();
    }

    @Override
    public List<InquiryMessage> findLatestMessagesByRoomIds(List<Long> roomIds) {
        if (roomIds.isEmpty()) {
            return List.of();
        }

        List<Long> latestMessageIds = queryFactory
                .select(inquiryMessage.id.max())
                .from(inquiryMessage)
                .where(inquiryMessage.roomId.in(roomIds))
                .groupBy(inquiryMessage.roomId)
                .fetch();

        return queryFactory
                .selectFrom(inquiryMessage)
                .where(inquiryMessage.id.in(latestMessageIds))
                .fetch();
    }

    private BooleanExpression cursorLt(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiryMessage.id.lt(cursor);
    }
}
