package me.bombom.api.v1.member.repository;

import static me.bombom.api.v1.member.domain.QMember.member;
import static me.bombom.api.v1.member.domain.QRole.role;

import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.ComparableExpressionBase;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.QGetMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
public class MemberRepositoryImpl implements CustomMemberRepository {

    private final JPAQueryFactory queryFactory;
    private static final Map<String, ComparableExpressionBase<?>> SORT_FIELD_WHITELIST_MAP = Map.of(
            "id", member.id,
            "nickname", member.nickname,
            "email", member.email);

    @Override
    public Page<GetMemberResponse> findMemberInfo(Pageable pageable, String name, String roleParam) {
        BooleanExpression predicate = combineConditions(
                nameContainsIgnoreCase(name),
                hasRole(roleParam));

        Long total = findTotal(predicate);
        List<GetMemberResponse> content = findContents(pageable, predicate);
        return new PageImpl<>(content, pageable, total == null ? 0 : total);
    }

    private List<GetMemberResponse> findContents(Pageable pageable, BooleanExpression predicate) {
        return queryFactory
                .select(new QGetMemberResponse(
                        member.id,
                        member.nickname,
                        member.email,
                        role.authority))
                .from(member)
                .join(role).on(member.roleId.eq(role.id))
                .where(predicate)
                .orderBy(applySort(pageable.getSort()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();
    }

    private Long findTotal(BooleanExpression predicate) {
        var query = queryFactory
                .select(member.count())
                .from(member)
                .join(role).on(member.roleId.eq(role.id));
        if (predicate != null) {
            query.where(predicate);
        }
        return query.fetchOne();
    }

    private OrderSpecifier<?>[] applySort(Sort sort) {
        OrderSpecifier<?>[] defaultOrder = new OrderSpecifier<?>[] { member.id.desc() };
        if (sort == null || sort.isUnsorted()) {
            return defaultOrder;
        }
        var orders = StreamSupport.stream(sort.spliterator(), false)
                .map(this::getOrderSpecifier)
                .filter(Objects::nonNull)
                .toList();
        return orders.isEmpty() ? defaultOrder : orders.toArray(OrderSpecifier[]::new);
    }

    private OrderSpecifier<?> getOrderSpecifier(Sort.Order order) {
        var path = SORT_FIELD_WHITELIST_MAP.get(order.getProperty());
        return path == null ? null : (order.isAscending() ? path.asc() : path.desc());
    }

    private BooleanExpression nameContainsIgnoreCase(String name) {
        if (!StringUtils.hasText(name)) {
            return null;
        }
        return member.nickname.containsIgnoreCase(name);
    }

    private BooleanExpression hasRole(String roleParam) {
        if (!StringUtils.hasText(roleParam)) {
            return null;
        }
        return role.authority.equalsIgnoreCase(roleParam);
    }

    private BooleanExpression combineConditions(BooleanExpression... expressions) {
        return Stream.of(expressions)
                .filter(Objects::nonNull)
                .reduce(BooleanExpression::and)
                .orElse(null);
    }
}
