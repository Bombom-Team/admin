package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.Min;

public record GrantShieldRequest(

        @Min(1)
        int count
) {
}
