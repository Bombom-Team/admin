package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryMessage.inquiryMessage;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
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

        List<InquiryMessage> messages = queryFactory
                .selectFrom(inquiryMessage)
                .where(inquiryMessage.roomId.in(roomIds))
                .orderBy(inquiryMessage.roomId.asc(), inquiryMessage.id.desc())
                .fetch();

        return messages.stream()
                .collect(Collectors.toMap(
                        InquiryMessage::getRoomId,
                        message -> message,
                        (first, second) -> first,
                        LinkedHashMap::new))
                .values()
                .stream()
                .sorted(Comparator.comparing(InquiryMessage::getRoomId))
                .toList();
    }

    private BooleanExpression cursorLt(Long cursor) {
        if (cursor == null) {
            return null;
        }
        return inquiryMessage.id.lt(cursor);
    }
}
