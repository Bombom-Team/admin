package me.bombom.api.v1.inquiry.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import me.bombom.api.v1.inquiry.dto.response.InquiryImageUploadResponse;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "InquiryImage", description = "문의 채팅 이미지 업로드(어드민) API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface InquiryImageControllerApi {

    @Operation(summary = "문의 채팅 이미지 업로드", description = "최대 4장까지 업로드 가능합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "업로드 성공"),
            @ApiResponse(responseCode = "400", description = "이미지 개수 초과 등 잘못된 요청", content = @Content)
    })
    InquiryImageUploadResponse uploadImages(@RequestPart("images") List<MultipartFile> images);
}
