package com.codeguardai.model;

import java.time.Instant;

import com.codeguardai.entity.Analysis;

public record AnalysisSummary(
		Long id,
		Instant analyzedAt,
		int score,
		RiskLevel riskLevel,
		int totalIssues,
		int criticalCount,
		int highCount,
		int mediumCount,
		int lowCount) {

	public static AnalysisSummary from(Analysis analysis) {
		return new AnalysisSummary(
				analysis.getId(),
				analysis.getAnalyzedAt(),
				analysis.getScore(),
				analysis.getRiskLevel(),
				analysis.getTotalIssues(),
				analysis.getCriticalCount(),
				analysis.getHighCount(),
				analysis.getMediumCount(),
				analysis.getLowCount());
	}
}
