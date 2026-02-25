package me.bombom.api.v1.member.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.Role;
import me.bombom.api.v1.member.enums.Gender;
import org.instancio.Instancio;

public class MemberFixture {

    public static Member createMember(String nickname) {
        return createMemberWithRole(nickname, 1L);
    }

    public static Member createMemberWithRole(String nickname, Long roleId) {
        return Instancio.of(Member.class)
                .set(field(Member::getId), null)
                .set(field(Member::getNickname), nickname)
                .set(field(Member::getEmail), nickname + "@example.com")
                .set(field(Member::getProviderId), "provider-" + nickname)
                .set(field(Member::getProvider), "GOOGLE")
                .set(field(Member::getRoleId), roleId)
                .set(field(Member::getGender), Gender.MALE)
                .create();
    }

    public static Role createRole(String authority) {
        return Instancio.of(Role.class)
                .set(field(Role::getId), null)
                .set(field(Role::getAuthority), authority)
                .create();
    }
}
