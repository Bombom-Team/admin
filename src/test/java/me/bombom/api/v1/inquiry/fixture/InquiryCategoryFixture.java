package me.bombom.api.v1.inquiry.fixture;

import static org.instancio.Select.field;

import me.bombom.api.v1.inquiry.domain.InquiryCategory;
import org.instancio.Instancio;

public class InquiryCategoryFixture {

    public static InquiryCategory createCategory(String name) {
        return Instancio.of(InquiryCategory.class)
                .set(field(InquiryCategory::getId), null)
                .set(field(InquiryCategory::getName), name)
                .create();
    }
}
