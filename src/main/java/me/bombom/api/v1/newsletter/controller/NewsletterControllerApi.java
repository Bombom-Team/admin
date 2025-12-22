package me.bombom.api.v1.newsletter.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import me.bombom.api.v1.newsletter.dto.CreateNewsletterRequest;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Newsletter", description = "뉴스레터 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface NewsletterControllerApi {

    @Operation(summary = "뉴스레터 생성", description = "새로운 뉴스레터를 등록합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "뉴스레터 생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 요청 값", content = @Content)
    })
    void createNewsletter(@Valid @RequestBody CreateNewsletterRequest request);
}
