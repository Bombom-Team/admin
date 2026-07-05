package me.bombom.api.v1.dev.controller;

import java.sql.SQLException;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.dev.service.ScenarioFactoryService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1")
public class DevController implements DevControllerApi {

    private final DataSource dataSource;
    private final ScenarioFactoryService scenarioFactoryService;

    @Override
    @GetMapping("/ping")
    public String ping() throws SQLException {
        String dbUrl = dataSource.getConnection().getMetaData().getURL();
        log.info("Current DB URL: {}", dbUrl);
        return "Connected to: " + dbUrl;
    }

    @Override
    @PostMapping("/seed/challenge/stopped")
    public String seedStoppedChallenge() {
        scenarioFactoryService.createStoppedChallengeScenario();
        return "Stopped challenge created";
    }
}
