package me.bombom.api.v1.faq.dto;

import jakarta.validation.constraints.Size;
import me.bombom.api.v1.faq.domain.FaqCategory;

public record UpdateFaqRequest(

        @Size(max = 75)
        String question,

        String answer,
        FaqCategory faqCategory
) {
}
