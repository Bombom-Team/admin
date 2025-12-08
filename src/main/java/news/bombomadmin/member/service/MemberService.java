package news.bombomadmin.member.service;

import lombok.RequiredArgsConstructor;
import news.bombomadmin.common.exception.CIllegalArgumentException;
import news.bombomadmin.common.exception.ErrorContextKeys;
import news.bombomadmin.common.exception.ErrorDetail;
import news.bombomadmin.member.domain.Member;
import news.bombomadmin.member.domain.Role;
import news.bombomadmin.member.repository.MemberRepository;
import news.bombomadmin.member.repository.RoleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberService {

    private final MemberRepository memberRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void updateRole(Long memberId, String authority) {
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
