package me.bombom.api.v1.inquiry.dto.response;

import java.util.List;

public record InquiryImageUploadResponse(

        List<String> imageUrls
) {

    public static InquiryImageUploadResponse from(List<String> imageUrls) {
        return new InquiryImageUploadResponse(imageUrls);
    }
}
