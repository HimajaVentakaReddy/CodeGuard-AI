package com.codeguardai.controller;

import com.codeguardai.model.ProjectInfo;
import com.codeguardai.service.ProjectInfoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "System", description = "Service health and project metadata")
public class ProjectInfoController {

	private final ProjectInfoService projectInfoService;

	public ProjectInfoController(ProjectInfoService projectInfoService) {
		this.projectInfoService = projectInfoService;
	}

	@GetMapping("/api/info")
	@Operation(summary = "Get project information", description = "Returns the application name, version, and description.")
	@ApiResponse(responseCode = "200", description = "Project information returned")
	@ApiResponse(responseCode = "500", description = "Unexpected server error")
	public ProjectInfo getProjectInfo() {
		return projectInfoService.getProjectInfo();
	}
}
