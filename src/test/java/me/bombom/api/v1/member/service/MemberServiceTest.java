package me.bombom.api.v1.member.service;

import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.dto.GetMemberResponse;
import me.bombom.api.v1.member.dto.MembersOptionsRequest;
import me.bombom.api.v1.member.dto.UpdateRoleRequest;
import me.bombom.api.v1.member.fixture.MemberFixture;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.RoleRepository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

@DataJpaTest
@Import({ MemberService.class, QuerydslConfig.class })
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
        Role admin = roleRepository.save(MemberFixture.createRole("ADMIN"));
        Role user = roleRepository.save(MemberFixture.createRole("USER"));
        memberRepository.saveAll(List.of(
                MemberFixture.createMemberWithRole("kim", admin.getId()),
                MemberFixture.createMemberWithRole("lee", user.getId())));
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
        Role admin = roleRepository.save(MemberFixture.createRole("ADMIN"));
        Role user = roleRepository.save(MemberFixture.createRole("USER"));
        Member member = memberRepository.save(MemberFixture.createMemberWithRole("hong", user.getId()));

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
        Role role = roleRepository.save(MemberFixture.createRole("USER"));
        Member member = memberRepository.save(MemberFixture.createMemberWithRole("park", role.getId()));

        // when, then
        assertThatThrownBy(() -> memberService.updateRole(member.getId(), new UpdateRoleRequest("ADMIN")))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasMessageContaining(ErrorDetail.ENTITY_NOT_FOUND.getMessage());
    }

}
