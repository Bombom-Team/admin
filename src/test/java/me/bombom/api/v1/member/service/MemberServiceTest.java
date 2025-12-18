package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.MembersOptionsRequest;
import me.bombom.api.v1.member.dto.UpdateRoleRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;
import me.bombom.api.v1.member.service.MemberService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
@Import({MemberService.class, QuerydslConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Test
    void 회원_목록_조회_name_role_필터_적용() {
        // given
        Role admin = roleRepository.save(Role.builder().authority("ADMIN").build());
        Role user = roleRepository.save(Role.builder().authority("USER").build());
        memberRepository.saveAll(List.of(
                member("kim", admin.getId()),
                member("lee", user.getId())
        ));
        MembersOptionsRequest req = new MembersOptionsRequest("kim", "ADMIN");

        // when
        Page<GetMemberResponse> page = memberService.getMembers(req, PageRequest.of(0, 10));
        GetMemberResponse first = page.getContent().getFirst();

        // then
        assertSoftly(softly -> {
            softly.assertThat(first.nickname()).isEqualTo("kim");
            softly.assertThat(first.role()).isEqualTo("ADMIN");
        });
    }

    @Test
    void 회원_역할_변경_성공() {
        // given
        Role admin = roleRepository.save(Role.builder().authority("ADMIN").build());
        Role user = roleRepository.save(Role.builder().authority("USER").build());
        Member member = memberRepository.save(member("hong", user.getId()));

        // when
        memberService.updateRole(member.getId(), new UpdateRoleRequest("ADMIN"));
        Member updated = memberRepository.findById(member.getId()).orElseThrow();

        // then
        assertThat(updated.getRoleId()).isEqualTo(admin.getId());
    }

    @Test
    void 회원이_없으면_예외() {
        // when, then
        assertThatThrownBy(() -> memberService.updateRole(999L, new UpdateRoleRequest("ADMIN")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessageContaining(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    @Test
    void 역할이_없으면_예외() {
        // given
        Role role = roleRepository.save(Role.builder().authority("USER").build());
        Member member = memberRepository.save(member("park", role.getId()));

        // when, then
        assertThatThrownBy(() -> memberService.updateRole(member.getId(), new UpdateRoleRequest("ADMIN")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessageContaining(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

    private Member member(String nickname, Long roleId) {
        return Member.builder()
                .provider("google")
                .providerId(UUID.randomUUID().toString())
                .email(nickname + "@email.com")
                .nickname(nickname)
                .profileImageUrl(null)
                .birthDate(LocalDate.of(1990, 1, 1))
                .gender(Gender.MALE)
                .roleId(roleId)
                .build();
    }
}

