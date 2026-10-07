package com.codeguardai.analyzer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.codeguardai.model.AnalysisResult;
import com.codeguardai.model.Finding;
import com.codeguardai.rule.CodeRule;
import org.springframework.stereotype.Component;

@Component
public class JavaCodeAnalyzer implements CodeAnalyzer {

	private final List<CodeRule> rules;

	public JavaCodeAnalyzer(List<CodeRule> rules) {
		this.rules = List.copyOf(rules);
	}

	@Override
	public AnalysisResult analyze(String sourceCode) {
		if (sourceCode == null) {
			throw new IllegalArgumentException("Source code must not be null.");
		}
		if (sourceCode.isEmpty()) {
			return new AnalysisResult(List.of());
		}

		List<String> lines = sourceCode.lines().toList();
		List<Finding> findings = new ArrayList<>();
		for (CodeRule rule : rules) {
			findings.addAll(rule.analyze(lines));
		}

		findings.sort(Comparator.comparingInt(Finding::lineNumber)
				.thenComparing(Finding::ruleId));
		return new AnalysisResult(findings);
	}
}
