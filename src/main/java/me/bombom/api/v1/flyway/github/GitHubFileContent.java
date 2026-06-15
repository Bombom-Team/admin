package me.bombom.api.v1.flyway.github;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record GitHubFileContent(
        String content,
        String encoding
) {

    public String decode() {
        if (content == null) {
            return "";
        }
        byte[] decoded = Base64.getMimeDecoder()
                .decode(content);
        return new String(decoded, StandardCharsets.UTF_8);
    }
}
