package com.codeguardai.service;

import com.codeguardai.model.ProjectInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class ProjectInfoService {

	private final ProjectInfo projectInfo;

	public ProjectInfoService(
			@Value("${codeguard.info.name}") String name,
			@Value("${codeguard.info.version}") String version,
			@Value("${codeguard.info.description}") String description) {
		this.projectInfo = new ProjectInfo(name, version, description);
	}

	public ProjectInfo getProjectInfo() {
		return projectInfo;
	}
}
