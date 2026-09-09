package me.bombom.api.v1.inquiry.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateInquiryCategoryRequest(

        @NotBlank
        @Size(max = 10)
        String name
) {
}
