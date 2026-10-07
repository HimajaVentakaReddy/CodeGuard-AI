package com.codeguardai.model;

import java.util.List;

public record AnalysisResult(
		List<Finding> findings,
		int totalIssues,
		int criticalCount,
		int highCount,
		int mediumCount,
		int lowCount) {

	public AnalysisResult {
		findings = List.copyOf(findings);
	}

	public AnalysisResult(List<Finding> findings) {
		this(
				findings,
				findings.size(),
				count(findings, Severity.CRITICAL),
				count(findings, Severity.HIGH),
				count(findings, Severity.MEDIUM),
				count(findings, Severity.LOW));
	}

	private static int count(List<Finding> findings, Severity severity) {
		return (int) findings.stream()
				.filter(finding -> finding.severity() == severity)
				.count();
	}
}
