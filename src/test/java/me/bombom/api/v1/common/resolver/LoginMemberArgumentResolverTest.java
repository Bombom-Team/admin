package me.bombom.api.v1.common.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class LoginMemberArgumentResolverTest {

    private final LoginMemberArgumentResolver resolver = new LoginMemberArgumentResolver();

    @BeforeEach
    void 인증정보를_초기화한다() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void 인증정보를_정리한다() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 비로그인_요청에는_관리자를_생성하지_않는다() throws Exception {
        // given
        MethodParameter parameter = memberParameter();

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, null, null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 익명인증_요청에는_관리자를_생성하지_않는다() throws Exception {
        // given
        MethodParameter parameter = memberParameter();
        var authentication = new AnonymousAuthenticationToken(
                "test", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when & then
        assertThatThrownBy(() -> resolver.resolveArgument(parameter, null, null, null))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void 인증된_회원은_기존_principal의_회원을_반환한다() throws Exception {
        // given
        MethodParameter parameter = memberParameter();
        Member member = Member.builder()
                .id(1L)
                .provider("GOOGLE")
                .providerId("admin-provider-id")
                .email("admin@example.com")
                .nickname("admin")
                .gender(Gender.MALE)
                .roleId(2L)
                .build();
        var principal = new CustomOAuth2User(Map.of("name", "admin"), member, null, null);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, List.of(new SimpleGrantedAuthority("ROLE_2")));
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // when
        Object resolved = resolver.resolveArgument(parameter, null, null, null);

        // then
        assertThat(resolved).isSameAs(member);
    }

    private MethodParameter memberParameter() throws Exception {
        return new MethodParameter(Endpoint.class.getDeclaredMethod("member", Member.class), 0);
    }

    static class Endpoint {

        void member(@LoginMember Member member) {
        }
    }
}
