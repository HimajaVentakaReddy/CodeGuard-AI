package com.codeguardai.model;

public record Finding(
		String ruleId,
		String title,
		String description,
		IssueCategory category,
		Severity severity,
		int lineNumber,
		String recommendation) {
}
