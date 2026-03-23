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
            `bombom-challenge` S3 버킷에 저장된 전체 이미지의 URL 목록을 반환합니다.

            **사용 흐름:**
            1. 데일리 가이드 생성/수정 화면 진입 시 이 API를 호출해 이미지 목록을 미리 로드하세요.
            2. 반환된 URL 목록을 썸네일 갤러리로 보여주고, 사용자가 선택하면 해당 URL을 그대로 사용하세요.
            3. 기존 이미지를 선택한 경우 → `POST /daily-guides` (`Content-Type: application/json`)로 생성
            4. 새 이미지를 직접 업로드하는 경우 → `POST /daily-guides` (`Content-Type: multipart/form-data`)로 생성

            **응답 예시:**
            ```json
            [
              "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1-read.jpg",
              "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day2-comment.jpg"
            ]
            ```
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공")
    })
    List<String> getChallengeImages();

    @Operation(summary = "데일리 가이드 생성 (새 이미지 업로드)", description = """
            새 이미지를 S3에 업로드하면서 데일리 가이드를 생성합니다.

            **요청 방식:** `Content-Type: multipart/form-data`

            **파트 구성:**
            - `image` (필수): 업로드할 이미지 파일 (jpg, png 등)
            - `request` (필수, Content-Type: application/json):
              ```json
              {
                "dayIndex": 1,
                "type": "READ",
                "fileName": "day1-read-guide",
                "notice": "오늘의 아티클을 읽어보세요."
              }
              ```

            **필드 설명:**
            - `dayIndex`: 챌린지 일차 (1 이상, `GET /schedule`에서 확인)
            - `type`: 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `fileName`: S3 저장 시 사용할 파일명 (확장자 제외). 예: `day1-read-guide` → `day1-read-guide.jpg`로 저장됨
            - `notice`: 안내 메시지. **`type`이 `COMMENT`인 경우 필수**

            **주의사항:**
            - `commentEnabled`는 입력하지 않습니다. `type == COMMENT`이면 자동으로 `true`로 설정됩니다.
            - 기존 S3 이미지를 재사용하려면 `POST /daily-guides` (`Content-Type: application/json`)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (필수 필드 누락, type=COMMENT인데 notice 없음 등)", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void create(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @RequestPart("image") MultipartFile image,
            @Valid @RequestPart("request") CreateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 생성 (기존 이미지 선택)", description = """
            S3에 이미 저장된 이미지의 URL로 데일리 가이드를 생성합니다.

            **요청 방식:** `Content-Type: application/json`

            **요청 예시:**
            ```json
            {
              "dayIndex": 2,
              "type": "COMMENT",
              "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day2-comment.jpg",
              "notice": "오늘 아티클을 읽고 댓글을 남겨주세요."
            }
            ```

            **필드 설명:**
            - `dayIndex`: 챌린지 일차 (1 이상, `GET /schedule`에서 확인)
            - `type`: 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `imageUrl`: `GET /images`에서 반환된 S3 URL을 그대로 사용하세요 (필수)
            - `notice`: 안내 메시지. **`type`이 `COMMENT`인 경우 필수**

            **주의사항:**
            - `commentEnabled`는 입력하지 않습니다. `type == COMMENT`이면 자동으로 `true`로 설정됩니다.
            - 새 이미지를 업로드하려면 `POST /daily-guides` (`Content-Type: multipart/form-data`)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (imageUrl 누락, type=COMMENT인데 notice 없음 등)", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void createFromImage(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Valid @RequestBody CreateDailyGuideFromImageRequest request);

    @Operation(summary = "데일리 가이드 목록 조회", description = """
            챌린지의 데일리 가이드 목록을 `dayIndex` 오름차순으로 조회합니다.

            **응답 필드 설명:**
            - `id`: 가이드 ID
            - `challengeId`: 소속 챌린지 ID
            - `dayIndex`: 챌린지 일차
            - `type`: 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `imageUrl`: S3 이미지 URL
            - `notice`: 안내 메시지 (nullable)
            - `commentEnabled`: 댓글 활성화 여부 (`type == COMMENT`이면 `true`)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    List<GetDailyGuideResponse> getDailyGuides(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

    @Operation(summary = "데일리 가이드 단건 조회", description = """
            데일리 가이드 상세 정보를 조회합니다.

            **응답 필드 설명:**
            - `id`: 가이드 ID
            - `challengeId`: 소속 챌린지 ID
            - `dayIndex`: 챌린지 일차
            - `type`: 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `imageUrl`: S3 이미지 URL
            - `notice`: 안내 메시지 (nullable)
            - `commentEnabled`: 댓글 활성화 여부 (`type == COMMENT`이면 `true`)
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    GetDailyGuideResponse getDailyGuide(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);

    @Operation(summary = "데일리 가이드 수정 (새 이미지 업로드)", description = """
            새 이미지를 S3에 업로드하면서 데일리 가이드를 수정합니다.

            **요청 방식:** `Content-Type: multipart/form-data`

            **파트 구성:**
            - `image` (선택): 새로 업로드할 이미지 파일. 생략하면 기존 이미지 URL이 유지됩니다.
            - `request` (필수, Content-Type: application/json): 변경할 필드만 포함하세요.
              ```json
              {
                "dayIndex": 3,
                "type": "SHARING",
                "fileName": "day3-sharing-guide",
                "notice": "오늘은 공유 미션입니다."
              }
              ```

            **필드 설명 (모두 선택 사항):**
            - `dayIndex`: 변경할 챌린지 일차
            - `type`: 변경할 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `fileName`: 새 이미지 업로드 시 S3 파일명 (확장자 제외). `image` 파트와 함께 사용
            - `notice`: 변경할 안내 메시지. **`type`을 `COMMENT`로 변경하는 경우 필수**

            **주의사항:**
            - `commentEnabled`는 입력하지 않습니다. `type` 변경 시 `type == COMMENT` 여부에 따라 자동 갱신됩니다. `type`을 생략하면 기존 값이 유지됩니다.
            - 포함하지 않은 필드는 기존 값이 유지됩니다.
            - 기존 S3 이미지로 교체하려면 `PATCH /{guideId}` (`Content-Type: application/json`)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (type=COMMENT인데 notice 없음 등)", content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void update(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") UpdateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 수정 (기존 이미지 선택)", description = """
            S3에 이미 저장된 이미지의 URL로 데일리 가이드를 수정합니다.

            **요청 방식:** `Content-Type: application/json`

            **요청 예시 (변경할 필드만 포함):**
            ```json
            {
              "type": "COMMENT",
              "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day3-comment.jpg",
              "notice": "댓글 미션입니다."
            }
            ```

            **필드 설명 (모두 선택 사항):**
            - `dayIndex`: 변경할 챌린지 일차
            - `type`: 변경할 가이드 유형 (`READ` | `COMMENT` | `SHARING` | `REMIND`)
            - `imageUrl`: `GET /images`에서 반환된 URL로 이미지를 교체. 생략하면 기존 이미지 유지
            - `notice`: 변경할 안내 메시지. **`type`을 `COMMENT`로 변경하는 경우 필수**

            **주의사항:**
            - `commentEnabled`는 입력하지 않습니다. `type` 변경 시 `type == COMMENT` 여부에 따라 자동 갱신됩니다. `type`을 생략하면 기존 값이 유지됩니다.
            - 포함하지 않은 필드는 기존 값이 유지됩니다.
            - 새 이미지를 업로드하려면 `PATCH /{guideId}` (`Content-Type: multipart/form-data`)를 사용하세요.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = "잘못된 입력값 (type=COMMENT인데 notice 없음 등)", content = @Content),
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
