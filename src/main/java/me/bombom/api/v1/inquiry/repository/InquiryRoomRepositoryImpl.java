package me.bombom.api.v1.inquiry.repository;

import static me.bombom.api.v1.inquiry.domain.QInquiryRoom.inquiryRoom;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.domain.InquiryRoom;
import me.bombom.api.v1.inquiry.domain.InquiryStatus;
import me.bombom.api.v1.inquiry.dto.request.GetInquiryRoomsRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class InquiryRoomRepositoryImpl implements CustomInquiryRoomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<InquiryRoom> findRoomsForAdmin(GetInquiryRoomsRequest request, Pageable pageable) {
        List<InquiryRoom> content = queryFactory
                .selectFrom(inquiryRoom)
                .where(
                        statusEq(request.status()),
                        assigneeIdEq(request.assigneeId()),
                        categoryIdEq(request.categoryId()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(inquiryRoom.createdAt.desc(), inquiryRoom.id.desc())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(inquiryRoom.count())
                .from(inquiryRoom)
                .where(
                        statusEq(request.status()),
                        assigneeIdEq(request.assigneeId()),
                        categoryIdEq(request.categoryId()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression statusEq(InquiryStatus status) {
        if (status == null) {
            return null;
        }
        return inquiryRoom.status.eq(status);
    }

    private BooleanExpression assigneeIdEq(Long assigneeId) {
        if (assigneeId == null) {
            return null;
        }
        return inquiryRoom.assigneeId.eq(assigneeId);
    }

    private BooleanExpression categoryIdEq(Long categoryId) {
        if (categoryId == null) {
            return null;
        }
        return inquiryRoom.categoryId.eq(categoryId);
    }
}
