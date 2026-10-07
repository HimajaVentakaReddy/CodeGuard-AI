package com.codeguardai.controller;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "Service health and project metadata")
public class HealthController {

	@GetMapping("/api/health")
	@Operation(summary = "Check service health", description = "Returns the current service status.")
	@ApiResponse(responseCode = "200", description = "Service is healthy")
	@ApiResponse(responseCode = "500", description = "Unexpected server error")
	public Map<String, String> health() {
		return Map.of(
				"status", "UP",
				"service", "CodeGuard AI");
	}
}
