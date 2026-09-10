package me.bombom.api.v1.inquiry.dto.response;

import java.util.List;

public record InquiryMessagePageResponse(

        List<InquiryMessageResponse> messages,
        boolean hasNext
) {

    public static InquiryMessagePageResponse of(List<InquiryMessageResponse> messages, boolean hasNext) {
        return new InquiryMessagePageResponse(messages, hasNext);
    }
}
