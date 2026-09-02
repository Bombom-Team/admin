package me.bombom.api.v1.dashboard.controller;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.core.util.Json;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import me.bombom.api.v1.dashboard.dto.DashboardStatsResponse;
import org.junit.jupiter.api.Test;

class DashboardSchemaTest {

    @Test
    void 대시보드_응답_스키마를_검증하고_프론트_타입생성용으로_내보낸다() throws Exception {
        // when
        Map<String, Schema> schemas = ModelConverters.getInstance()
                .readAll(DashboardStatsResponse.class);
        Schema<?> response = schemas.get("DashboardStatsResponse");
        Schema<?> point = schemas.get("DailyJoinedMembersResponse");

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getProperties().get("dailyJoinedTrend").getType()).isEqualTo("array");
            softly.assertThat(response.getProperties().get("aggregatedAt").getFormat()).isEqualTo("date-time");
            softly.assertThat(point.getProperties().get("date").getFormat()).isEqualTo("date");
            softly.assertThat(point.getProperties().get("count").getType()).isEqualTo("integer");
        });

        Info info = new Info()
                .title("Admin dashboard response schemas")
                .version("BOM-1218");
        Components components = new Components()
                .schemas(schemas);
        OpenAPI contract = new OpenAPI()
                .info(info)
                .paths(new Paths())
                .components(components);
        Path output = Path.of("build/openapi/dashboard.json");
        Files.createDirectories(output.getParent());
        Files.writeString(output, Json.pretty(contract));
    }
}
