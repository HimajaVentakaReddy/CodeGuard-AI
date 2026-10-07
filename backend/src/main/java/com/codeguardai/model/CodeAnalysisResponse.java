package com.codeguardai.model;

import java.util.List;

public record CodeAnalysisResponse(
		int score,
		RiskLevel riskLevel,
		int totalIssues,
		int criticalCount,
		int highCount,
		int mediumCount,
		int lowCount,
		List<Finding> findings) {

	public CodeAnalysisResponse {
		findings = List.copyOf(findings);
	}
}
