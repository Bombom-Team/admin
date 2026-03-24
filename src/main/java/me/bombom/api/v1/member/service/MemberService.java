package me.bombom.api.v1.member.service;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.MembersOptionsRequest;
import me.bombom.api.v1.member.dto.UpdateRoleRequest;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    public Page<GetMemberResponse> getMembers(MembersOptionsRequest request, Pageable pageable) {
        return memberRepository.findMemberInfo(pageable, request.name(), request.role());
    }

    @Transactional
    public void updateRole(Long memberId, UpdateRoleRequest request) {
        String authority = request.authority();
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.OPERATION, "updateRole")
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "member")
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId));
        Role role = roleRepository.findByAuthority(authority)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.OPERATION, "updateRole")
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "role")
                        .addContext("authority", authority));
        member.updateRole(role.getId());
    }
}
