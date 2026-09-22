package me.bombom.api.v1.inquiry.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import me.bombom.api.v1.inquiry.service.InquiryImageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/inquiries/images")
public class InquiryImageController implements InquiryImageControllerApi {

    private final InquiryImageService inquiryImageService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public InquiryImageUploadResponse uploadImages(@RequestPart("images") List<MultipartFile> images) {
        return inquiryImageService.uploadImages(images);
    }
}
