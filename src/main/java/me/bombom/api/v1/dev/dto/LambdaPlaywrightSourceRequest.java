package me.bombom.api.v1.dev.dto;

import jakarta.validation.constraints.NotBlank;

public record LambdaPlaywrightSourceRequest(

        @NotBlank
        String content
) {
}
