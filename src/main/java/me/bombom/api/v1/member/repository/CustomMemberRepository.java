package me.bombom.api.v1.member.repository;

import me.bombom.api.v1.member.dto.GetMemberResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomMemberRepository {

    Page<GetMemberResponse> findMemberInfo(Pageable pageable, String name, String role);

    long countNewMembersThisMonth();

    long countTodayJoinedMembers();
}
