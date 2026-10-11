package com.codeguardai.rule.security;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.Severity;
import com.codeguardai.rule.CodeRule;
import org.springframework.stereotype.Component;

@Component
public class ProcessBuilderExecutionRule implements CodeRule {

	private static final Pattern PROCESS_BUILDER_DECLARATION = Pattern.compile(
			"\\b(?:ProcessBuilder|var)\\s+([\\w$]+)\\s*=\\s*new\\s+(?:java\\.lang\\.)?ProcessBuilder\\s*\\(");
	private static final Pattern INLINE_PROCESS_BUILDER_START = Pattern.compile(
			"\\bnew\\s+(?:java\\.lang\\.)?ProcessBuilder\\s*\\([^;]*\\)\\s*\\.\\s*start\\s*\\(");
	private static final Pattern PROCESS_BUILDER_START = Pattern.compile("\\b([\\w$]+)\\s*\\.\\s*start\\s*\\(");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		Set<String> processBuilderVariables = new HashSet<>();

		for (int index = 0; index < lines.size(); index++) {
			String line = lines.get(index);
			Matcher declaration = PROCESS_BUILDER_DECLARATION.matcher(line);
			while (declaration.find()) {
				processBuilderVariables.add(declaration.group(1));
			}

			boolean startsProcess = INLINE_PROCESS_BUILDER_START.matcher(line).find();
			Matcher start = PROCESS_BUILDER_START.matcher(line);
			while (start.find()) {
				if (processBuilderVariables.contains(start.group(1))) {
					startsProcess = true;
					break;
				}
			}

			if (startsProcess) {
				findings.add(new Finding(
						"SEC004",
						"ProcessBuilder command execution",
						"ProcessBuilder starts an operating-system process from the application.",
						IssueCategory.SECURITY,
						Severity.HIGH,
						index + 1,
						"Avoid process execution where possible; validate and constrain every command argument."));
			}
		}
		return findings;
	}
}
