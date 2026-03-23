package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideFromImageRequest;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Challenge Daily Guide", description = "챌린지 데일리 가이드 관리 API")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "인증 실패 (로그인 필요)", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 없음", content = @Content)
})
public interface ChallengeDailyGuideControllerApi {

    @Operation(summary = "S3 이미지 목록 조회", description = """
            bombom-challenge S3 버킷에 저장된 전체 이미지 URL 목록을 반환합니다.

            **Frontend 구현 가이드:**
            - 데일리 가이드 생성/수정 화면 진입 시 이 API를 호출해 이미지 목록을 미리 로드하세요.
            - 반환된 URL을 이미지 썸네일로 보여주고 사용자가 선택하면, 해당 URL을 `POST /from-image`의 `imageUrl` 필드에 담아 전송하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<String> getChallengeImages();

    @Operation(summary = "데일리 가이드 생성 (이미지 업로드)", description = """
            새 이미지를 S3에 업로드하면서 데일리 가이드를 생성합니다.

            **Frontend 구현 가이드:**
            - `Content-Type: multipart/form-data`로 요청하세요.
            - `image`: 업로드할 이미지 파일
            - `request`: 아래 JSON을 `application/json` 파트로 전송
              ```json
              {
                "dayIndex": 1,
                "type": "READ",
                "fileName": "day1-read-guide",
                "notice": "오늘의 안내 메시지",
                "commentEnabled": true
              }
              ```
            - `fileName`은 S3 저장 파일명이 됩니다. 확장자는 자동으로 붙습니다. (예: `day1-read-guide.jpg`)
            - 기존 이미지를 재사용하려면 `POST /from-image`를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void create(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @RequestPart("image") MultipartFile image,
            @Valid @RequestPart("request") CreateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 생성 (기존 이미지 선택)", description = """
            S3에 이미 저장된 이미지 URL로 데일리 가이드를 생성합니다.

            **Frontend 구현 가이드:**
            - `Content-Type: application/json`으로 요청하세요.
            - `imageUrl`은 `GET /images` API로 조회한 URL을 그대로 사용하세요.
              ```json
              {
                "dayIndex": 1,
                "type": "READ",
                "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1-read-guide.jpg",
                "notice": "오늘의 안내 메시지",
                "commentEnabled": true
              }
              ```
            - 새 이미지를 업로드하려면 `POST /` (multipart)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void createFromImage(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Valid @RequestBody CreateDailyGuideFromImageRequest request);

    @Operation(summary = "데일리 가이드 목록 조회", description = "챌린지의 데일리 가이드 목록을 dayIndex 오름차순으로 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    List<GetDailyGuideResponse> getDailyGuides(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

    @Operation(summary = "데일리 가이드 단건 조회", description = "데일리 가이드 상세 정보를 조회합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    GetDailyGuideResponse getDailyGuide(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);

    @Operation(summary = "데일리 가이드 수정", description = """
            데일리 가이드 정보를 수정합니다.

            **Frontend 구현 가이드:**
            - `Content-Type: multipart/form-data`로 요청하세요.
            - 변경할 필드만 포함하면 됩니다. 포함하지 않은 필드는 기존 값이 유지됩니다.
            - 이미지 변경 방법은 두 가지입니다:
              1. 새 이미지 업로드: `image` 파트에 파일 + `request.fileName` 입력
              2. 기존 이미지로 변경: `request.imageUrl`에 URL 입력 (`image` 파트 생략)
            - 이미지를 변경하지 않으려면 `image`와 `imageUrl` 모두 생략하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void update(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") UpdateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 수정 (기존 이미지 선택)", description = """
            기존 S3 이미지로 데일리 가이드를 수정합니다.

            **Frontend 구현 가이드:**
            - `Content-Type: application/json`으로 요청하세요.
            - 변경할 필드만 포함하면 됩니다. 포함하지 않은 필드는 기존 값이 유지됩니다.
            - 이미지를 변경하지 않으려면 `imageUrl`을 생략하세요.
            - 새 이미지를 업로드하려면 `PATCH /{guideId}` (multipart)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void updateFromImage(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId,
            @Valid @RequestBody UpdateDailyGuideFromImageRequest request);

    @Operation(summary = "데일리 가이드 삭제", description = "데일리 가이드를 삭제합니다.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void delete(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);
}
