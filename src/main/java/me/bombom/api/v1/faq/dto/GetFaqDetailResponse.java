package me.bombom.api.v1.faq.dto;

import java.time.LocalDate;
import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.domain.FaqCategory;

public record GetFaqDetailResponse(

        String question,
        FaqCategory faqCategory,
        String answer,
        LocalDate createdAt
) {

    public static GetFaqDetailResponse from(Faq faq) {
        return new GetFaqDetailResponse(
                faq.getQuestion(),
                faq.getFaqCategory(),
                faq.getAnswer(),
                faq.getCreatedAt().toLocalDate());
    }
}
