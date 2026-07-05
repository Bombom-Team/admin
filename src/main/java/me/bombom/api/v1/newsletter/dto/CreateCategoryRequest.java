package me.bombom.api.v1.newsletter.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(

        @NotBlank
        @Size(max = 20)
        String name
) {
}
