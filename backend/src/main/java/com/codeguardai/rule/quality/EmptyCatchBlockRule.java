package com.codeguardai.rule.quality;

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
public class EmptyCatchBlockRule implements CodeRule {

	private static final Pattern CATCH_BLOCK = Pattern.compile("\\bcatch\\s*\\([^)]*\\)\\s*\\{");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		for (int index = 0; index < lines.size(); index++) {
			Matcher matcher = CATCH_BLOCK.matcher(lines.get(index));
			if (!matcher.find()) {
				continue;
			}

			String remainder = lines.get(index).substring(matcher.end()).trim();
			if (remainder.startsWith("}") && remainder.substring(1).isBlank()) {
				addFinding(findings, index + 1);
			} else if (remainder.isEmpty()) {
				for (int bodyIndex = index + 1; bodyIndex < lines.size(); bodyIndex++) {
					String bodyLine = lines.get(bodyIndex).trim();
					if (bodyLine.isEmpty()) {
						continue;
					}
					if (bodyLine.equals("}")) {
						addFinding(findings, index + 1);
					}
					break;
				}
			}
		}
		return findings;
	}

	private static void addFinding(List<Finding> findings, int lineNumber) {
		findings.add(new Finding(
				"QUAL001",
				"Empty catch block",
				"An exception is caught and ignored without handling or reporting it.",
				IssueCategory.BUG,
				Severity.MEDIUM,
				lineNumber,
				"Handle the exception or log it with enough context for diagnosis."));
	}
}
