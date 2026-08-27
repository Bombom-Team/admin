package me.bombom.api.v1.faq.fixture;

import me.bombom.api.v1.faq.domain.Faq;
import me.bombom.api.v1.faq.domain.FaqCategory;

import static org.instancio.Select.field;

import org.instancio.Instancio;

public class FaqFixture {

    public static Faq createFaq() {
        return createFaq("질문", "답변", FaqCategory.FEATURE);
    }

    public static Faq createFaq(String question, String answer, FaqCategory category) {
        return Instancio.of(Faq.class)
                .set(field(Faq::getId), null)
                .set(field(Faq::getQuestion), question)
                .set(field(Faq::getAnswer), answer)
                .set(field(Faq::getFaqCategory), category)
                .create();
    }
}
