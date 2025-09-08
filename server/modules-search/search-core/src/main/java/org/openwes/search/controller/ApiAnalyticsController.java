package org.openwes.search.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.openwes.search.application.ApiAnalyticsService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
@Tag(name = "Search Module Api")
public class ApiAnalyticsController {

    private final ApiAnalyticsService analyticsService;

    @GetMapping("/dashboard")
    public Map<String, Object> getDashboardData() {
        Map<String, Object> result = new HashMap<>();

        // Get all statistics using Bean Searcher
        result.put("stats", analyticsService.getApiStats());
        result.put("timeSeriesData", analyticsService.getTimeSeriesData());
        result.put("topEndpoints", analyticsService.getTopEndpoints());
        result.put("errorDistribution", analyticsService.getErrorDistribution());
        result.put("recentApiCalls", analyticsService.getRecentApiCalls());

        return result;
    }
}
