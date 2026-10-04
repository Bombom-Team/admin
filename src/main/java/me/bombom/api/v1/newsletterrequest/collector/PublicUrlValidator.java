package me.bombom.api.v1.newsletterrequest.collector;

import java.net.InetAddress;
import java.net.URI;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Locale;
import java.util.Set;

/**
 * 유저가 입력한 링크로 서버가 요청을 보내므로, 내부망 주소로 가는 요청을 막는다(SSRF 방지).
 */
public final class PublicUrlValidator {

    private static final Set<String> ALLOWED_SCHEMES = Set.of("http", "https");

    private PublicUrlValidator() {
    }

    public static void validate(String url) {
        URI uri = parse(url);
        if (isDisallowedScheme(uri.getScheme())) {
            throw new CollectFailedException("허용하지 않는 스킴: " + url);
        }
        if (uri.getHost() == null) {
            throw new CollectFailedException("호스트가 없는 링크: " + url);
        }
        if (resolvesToPrivateAddress(uri.getHost())) {
            throw new CollectFailedException("내부망 주소로는 요청할 수 없음: " + uri.getHost());
        }
    }

    private static URI parse(String url) {
        try {
            return URI.create(url.strip());
        } catch (IllegalArgumentException exception) {
            throw new CollectFailedException("링크 형식 오류: " + url);
        }
    }

    private static boolean isDisallowedScheme(String scheme) {
        if (scheme == null) {
            return true;
        }
        return ALLOWED_SCHEMES.stream()
                .noneMatch(allowed -> allowed.equals(scheme.toLowerCase(Locale.ROOT)));
    }

    private static boolean resolvesToPrivateAddress(String host) {
        try {
            return Arrays.stream(InetAddress.getAllByName(host))
                    .anyMatch(PublicUrlValidator::isPrivate);
        } catch (UnknownHostException exception) {
            throw new CollectFailedException("호스트를 찾을 수 없음: " + host);
        }
    }

    private static boolean isPrivate(InetAddress address) {
        return address.isAnyLocalAddress()
                || address.isLoopbackAddress()
                || address.isSiteLocalAddress()
                || address.isLinkLocalAddress()
                || address.isMulticastAddress();
    }
}
