package me.bombom.api.v1.faq.dto;

import java.time.LocalDate;
import me.bombom.api.v1.faq.domain.Faq;

public record GetFaqResponse(

        Long id,
        String question,
        String faqCategory,
        LocalDate createdAt
) {

    public static GetFaqResponse from(Faq faq) {
        return new GetFaqResponse(
                faq.getId(),
                faq.getQuestion(),
                faq.getFaqCategory().getValue(),
                faq.getCreatedAt().toLocalDate());
    }
}
