package me.bombom.api.v1.file.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.file.dto.UploadFileResponse;
import me.bombom.api.v1.file.service.S3FileService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/files")
public class FileController implements FileControllerApi {

    private final S3FileService s3FileService;

    @Override
    public UploadFileResponse upload(MultipartFile multipartFile) {
        String url = s3FileService.uploadToNoticeBucket(multipartFile);
        return new UploadFileResponse(url);
    }
}
