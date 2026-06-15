package me.bombom.api.v1.flyway.dto.response;

import java.util.List;

/**
 * 같은 버전 번호 충돌. 어떤 소스들이 같은 번호를 쓰는지와 제안 번호를 담는다.
 */
public record FlywayConflictResponse(
        String version,
        List<String> sources,
        String suggestedVersion
) {
}
