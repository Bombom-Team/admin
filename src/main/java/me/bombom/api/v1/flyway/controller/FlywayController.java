package me.bombom.api.v1.flyway.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.flyway.dto.request.CreateWipIssueRequest;
import me.bombom.api.v1.flyway.dto.response.CreateWipIssueResponse;
import me.bombom.api.v1.flyway.dto.response.FlywayOverviewResponse;
import me.bombom.api.v1.flyway.dto.response.MigrationScriptResponse;
import me.bombom.api.v1.flyway.service.FlywayService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/api/v1/flyway")
public class FlywayController implements FlywayControllerApi {

    private final FlywayService flywayService;

    @Override
    @GetMapping("/overview")
    public FlywayOverviewResponse getOverview() {
        return flywayService.getOverview();
    }

    @Override
    @GetMapping("/script")
    public MigrationScriptResponse getScript(@RequestParam String fileName) {
        return flywayService.getScript(fileName);
    }

    @Override
    @PostMapping("/wip")
    @ResponseStatus(HttpStatus.CREATED)
    public CreateWipIssueResponse createWipIssue(@Valid @RequestBody CreateWipIssueRequest request) {
        return flywayService.createWipIssue(request);
    }
}
