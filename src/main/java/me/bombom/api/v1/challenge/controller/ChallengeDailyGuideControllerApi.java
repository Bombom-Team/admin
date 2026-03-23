package me.bombom.api.v1.challenge.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import me.bombom.api.v1.challenge.dto.CreateDailyGuideRequest;
import me.bombom.api.v1.challenge.dto.GetDailyGuideResponse;
import me.bombom.api.v1.challenge.dto.UpdateDailyGuideRequest;
import org.springframework.web.bind.annotation.PathVariable;
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

            **전체 사용 흐름:**
            1. 이 API로 이미지 목록 조회 → 썸네일 갤러리로 표시
            2. 관리자가 기존 이미지 선택 또는 새 이미지 업로드 선택
            3. **기존 이미지 선택** → `POST /daily-guides`의 `request.imageUrl`에 해당 URL 지정
            4. **새 이미지 업로드** → `POST /daily-guides`의 `image` 파트에 파일 첨부

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

    @Operation(summary = "데일리 가이드 생성", description = """
            챌린지의 데일리 가이드를 생성합니다.

            **이미지 지정 방식 (둘 중 하나 필수):**
            | 방식 | 사용 필드 |
            |------|----------|
            | 새 이미지 업로드 | `image` 파트 + `request.fileName` |
            | 기존 S3 이미지 선택 | `request.imageUrl` (`GET /images`에서 조회한 URL) |

            > `image` 파트와 `imageUrl`을 모두 지정한 경우 `image` 파트가 우선 적용됩니다.

            **요청 형식:** `Content-Type: multipart/form-data`

            **파트 구성:**
            - `image` (선택): 업로드할 이미지 파일 (jpg, png, gif, webp 지원). 이미지는 최대 1000x1000으로 리사이즈됩니다.
            - `request` (필수, `Content-Type: application/json`):

            **새 이미지 업로드 예시:**
            ```json
            {
              "dayIndex": 1,
              "type": "READ",
              "fileName": "day1-read-guide",
              "notice": "오늘의 아티클을 읽어보세요."
            }
            ```

            **기존 이미지 선택 예시:**
            ```json
            {
              "dayIndex": 1,
              "type": "READ",
              "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1-read.jpg",
              "notice": "오늘의 아티클을 읽어보세요."
            }
            ```

            **필드 설명:**
            | 필드 | 필수 | 설명 |
            |------|------|------|
            | `dayIndex` | ✅ | 챌린지 일차 (1 이상). `GET /schedule`에서 유효한 dayIndex 확인 가능. 챌린지 내 중복 불가 |
            | `type` | ✅ | 가이드 유형. `READ` \\| `COMMENT` \\| `SHARING` \\| `REMIND` |
            | `fileName` | 새 이미지 업로드 시 필수 | S3 저장 파일명 (확장자 제외). 예: `day1-read` → `day1-read.jpg` |
            | `imageUrl` | 기존 이미지 선택 시 필수 | `GET /images`에서 반환된 URL |
            | `notice` | `type=COMMENT` 시 필수 | 안내 메시지 (최대 1000자) |

            **가이드 유형별 설명:**
            - `READ`: 아티클 읽기 미션
            - `COMMENT`: 댓글 달기 미션 (`commentEnabled = true` 자동 설정)
            - `SHARING`: 공유 미션
            - `REMIND`: 리마인드 알림

            **주의사항:**
            - `commentEnabled`는 요청에 포함하지 않습니다. `type == COMMENT`이면 자동으로 `true`, 그 외엔 `false`로 설정됩니다.
            - 동일 챌린지 내에서 같은 `dayIndex`를 가진 가이드는 하나만 존재할 수 있습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "생성 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 입력값. 다음 경우에 발생합니다:
                    - `image`와 `imageUrl` 모두 없음
                    - `dayIndex` 누락 또는 1 미만
                    - `type` 누락
                    - `type=COMMENT`인데 `notice` 없음
                    - 해당 챌린지에 이미 같은 `dayIndex`의 가이드 존재
                    """, content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    void create(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") CreateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 목록 조회", description = """
            챌린지의 전체 데일리 가이드 목록을 `dayIndex` 오름차순으로 조회합니다.

            **응답 필드 설명:**
            | 필드 | 설명 |
            |------|------|
            | `id` | 가이드 ID |
            | `challengeId` | 소속 챌린지 ID |
            | `dayIndex` | 챌린지 일차 |
            | `type` | 가이드 유형 (`READ` \\| `COMMENT` \\| `SHARING` \\| `REMIND`) |
            | `imageUrl` | S3 이미지 URL |
            | `notice` | 안내 메시지 (nullable) |
            | `commentEnabled` | 댓글 활성화 여부. `type == COMMENT`이면 `true` |

            **응답 예시:**
            ```json
            [
              {
                "id": 1,
                "challengeId": 10,
                "dayIndex": 1,
                "type": "READ",
                "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day1-read.jpg",
                "notice": "오늘의 아티클을 읽어보세요.",
                "commentEnabled": false
              },
              {
                "id": 2,
                "challengeId": 10,
                "dayIndex": 2,
                "type": "COMMENT",
                "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day2-comment.jpg",
                "notice": "댓글을 남겨주세요.",
                "commentEnabled": true
              }
            ]
            ```
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지", content = @Content)
    })
    List<GetDailyGuideResponse> getDailyGuides(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId);

    @Operation(summary = "데일리 가이드 단건 조회 (guideId)", description = """
            가이드 ID로 데일리 가이드 상세 정보를 조회합니다.

            **응답 필드 설명:**
            | 필드 | 설명 |
            |------|------|
            | `id` | 가이드 ID |
            | `challengeId` | 소속 챌린지 ID |
            | `dayIndex` | 챌린지 일차 |
            | `type` | 가이드 유형 (`READ` \\| `COMMENT` \\| `SHARING` \\| `REMIND`) |
            | `imageUrl` | S3 이미지 URL |
            | `notice` | 안내 메시지 (nullable) |
            | `commentEnabled` | 댓글 활성화 여부. `type == COMMENT`이면 `true` |
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    GetDailyGuideResponse getDailyGuide(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);

    @Operation(summary = "데일리 가이드 단건 조회 (dayIndex)", description = """
            챌린지 일차(`dayIndex`)로 데일리 가이드를 조회합니다.

            앱에서 특정 날짜의 가이드를 조회할 때 사용하세요.

            **사용 예시:** `GET /challenges/10/daily-guides/days/3`

            **응답 필드 설명:**
            | 필드 | 설명 |
            |------|------|
            | `id` | 가이드 ID |
            | `challengeId` | 소속 챌린지 ID |
            | `dayIndex` | 챌린지 일차 |
            | `type` | 가이드 유형 (`READ` \\| `COMMENT` \\| `SHARING` \\| `REMIND`) |
            | `imageUrl` | S3 이미지 URL |
            | `notice` | 안내 메시지 (nullable) |
            | `commentEnabled` | 댓글 활성화 여부. `type == COMMENT`이면 `true` |
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 해당 dayIndex의 가이드 없음", content = @Content)
    })
    GetDailyGuideResponse getDailyGuideByDayIndex(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "챌린지 일차 (1 이상)", required = true) @PathVariable int dayIndex);

    @Operation(summary = "데일리 가이드 수정", description = """
            데일리 가이드를 수정합니다. **변경할 필드만 포함하면 되며, 포함하지 않은 필드는 기존 값이 유지됩니다.**

            **이미지 변경 방식 (모두 선택 사항):**
            | 방식 | 사용 필드 |
            |------|----------|
            | 새 이미지 업로드 | `image` 파트 + `request.fileName` |
            | 기존 S3 이미지로 교체 | `request.imageUrl` |
            | 이미지 변경 없음 | `image` 파트와 `imageUrl` 모두 생략 |

            > `image` 파트와 `imageUrl`을 모두 지정한 경우 `image` 파트가 우선 적용됩니다.

            **요청 형식:** `Content-Type: multipart/form-data`

            **파트 구성:**
            - `image` (선택): 새로 업로드할 이미지 파일 (jpg, png, gif, webp 지원). 이미지는 최대 1000x1000으로 리사이즈됩니다.
            - `request` (필수, `Content-Type: application/json`):

            **새 이미지 업로드 예시:**
            ```json
            {
              "dayIndex": 3,
              "type": "SHARING",
              "fileName": "day3-sharing-guide",
              "notice": "오늘은 공유 미션입니다."
            }
            ```

            **기존 이미지 교체 예시:**
            ```json
            {
              "imageUrl": "https://bombom-challenge.s3.ap-northeast-2.amazonaws.com/day3-sharing.jpg"
            }
            ```

            **이미지 변경 없이 다른 필드만 수정 예시:**
            ```json
            {
              "type": "COMMENT",
              "notice": "댓글을 남겨주세요."
            }
            ```

            **필드 설명 (모두 선택 사항):**
            | 필드 | 설명 |
            |------|------|
            | `dayIndex` | 변경할 챌린지 일차 (1 이상). 챌린지 내 중복 불가 |
            | `type` | 변경할 가이드 유형. `READ` \\| `COMMENT` \\| `SHARING` \\| `REMIND` |
            | `fileName` | 새 이미지 업로드 시 S3 파일명 (확장자 제외) |
            | `imageUrl` | 기존 S3 이미지로 교체 시 사용할 URL |
            | `notice` | 변경할 안내 메시지 (최대 1000자). `type=COMMENT`로 변경 시 필수 |

            **주의사항:**
            - `commentEnabled`는 요청에 포함하지 않습니다. `type` 변경 시 `type == COMMENT` 여부에 따라 자동 갱신되며, `type`을 생략하면 기존 값이 유지됩니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공"),
            @ApiResponse(responseCode = "400", description = """
                    잘못된 입력값. 다음 경우에 발생합니다:
                    - `dayIndex` 지정 시 1 미만
                    - `type=COMMENT`로 변경하는데 `notice` 없음
                    - 해당 챌린지에 이미 같은 `dayIndex`의 다른 가이드 존재
                    """, content = @Content),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void update(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @Valid @RequestPart("request") UpdateDailyGuideRequest request);

    @Operation(summary = "데일리 가이드 삭제", description = """
            데일리 가이드를 삭제합니다.

            삭제된 가이드는 복구할 수 없습니다. S3에 저장된 이미지는 삭제되지 않습니다.
            """)
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "존재하지 않는 챌린지 또는 가이드", content = @Content)
    })
    void delete(
            @Parameter(description = "챌린지 ID", required = true) @PathVariable Long challengeId,
            @Parameter(description = "가이드 ID", required = true) @PathVariable Long guideId);
}
