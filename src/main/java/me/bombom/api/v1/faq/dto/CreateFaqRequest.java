package me.bombom.api.v1.faq.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.faq.domain.FaqCategory;

public record CreateFaqRequest(

        @NotBlank
        @Size(max = 75)
        String question,

        @NotBlank
        String answer,

        @NotNull
        FaqCategory faqCategory
) {
}
