package me.bombom.api.v1.subscribe.dto.request;

import jakarta.validation.constraints.NotBlank;

public record UnsubscribePatternRequest(

        @NotBlank
        String patternKey,

        @NotBlank
        String patternValue
) {
}
