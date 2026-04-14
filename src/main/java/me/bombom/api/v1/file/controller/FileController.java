package me.bombom.api.v1.file.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.file.dto.UploadFileResponse;
import me.bombom.api.v1.file.service.FileUploadService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/files")
public class FileController implements FileControllerApi {

    private final FileUploadService fileUploadService;

    @Override
    public UploadFileResponse upload(MultipartFile multipartFile) {
        return fileUploadService.uploadNoticeImage(multipartFile);
    }
}
