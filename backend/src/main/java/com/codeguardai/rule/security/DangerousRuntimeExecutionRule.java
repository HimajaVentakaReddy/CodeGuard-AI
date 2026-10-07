package com.codeguardai.rule.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.Severity;
import com.codeguardai.rule.CodeRule;
import org.springframework.stereotype.Component;

@Component
public class DangerousRuntimeExecutionRule implements CodeRule {

	private static final Pattern RUNTIME_EXEC = Pattern.compile(
			"\\bRuntime\\s*\\.\\s*getRuntime\\s*\\(\\s*\\)\\s*\\.\\s*exec\\s*\\(");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		for (int index = 0; index < lines.size(); index++) {
			if (RUNTIME_EXEC.matcher(lines.get(index)).find()) {
				findings.add(new Finding(
						"SEC003",
						"Dangerous runtime command execution",
						"Runtime.exec executes an operating-system command from the application.",
						IssueCategory.SECURITY,
						Severity.HIGH,
						index + 1,
						"Avoid shell execution where possible; validate and constrain all command inputs."));
			}
		}
		return findings;
	}
}
