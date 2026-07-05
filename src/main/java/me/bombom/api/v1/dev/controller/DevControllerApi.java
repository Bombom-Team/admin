package me.bombom.api.v1.dev.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.sql.SQLException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Tag(name = "Dev API", description = "개발 환경 전용 API (운영 환경 사용 금지)")
public interface DevControllerApi {

    @Operation(summary = "DB 연결 확인", description = "현재 연결된 DB URL을 반환하여 라우팅이 정상 작동하는지 확인합니다.")
    @GetMapping("/ping")
    String ping() throws SQLException;

    @Operation(summary = "종료된 챌린지 생성", description = "강제로 종료된 상태의 챌린지 데이터(종료일이 1일 전)를 생성합니다.")
    @PostMapping("/seed/challenge/stopped")
    String seedStoppedChallenge();
}
