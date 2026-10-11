package com.codeguardai.rule.security;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.Severity;
import com.codeguardai.rule.CodeRule;
import org.springframework.stereotype.Component;

@Component
public class HardcodedPasswordRule implements CodeRule {

	private static final Pattern PASSWORD_ASSIGNMENT = Pattern.compile(
			"(?i)\\b[\\w$]*(?:password|passwd|pwd)[\\w$]*\\s*=\\s*([\"'])[^\"']+\\1");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		for (int index = 0; index < lines.size(); index++) {
			Matcher matcher = PASSWORD_ASSIGNMENT.matcher(lines.get(index));
			while (matcher.find()) {
				findings.add(new Finding(
						"SEC001",
						"Hardcoded password",
						"A password-like variable is assigned a string literal.",
						IssueCategory.SECURITY,
						Severity.HIGH,
						index + 1,
						"Load the password from a secure secret store or environment variable."));
			}
		}
		return findings;
	}
}
