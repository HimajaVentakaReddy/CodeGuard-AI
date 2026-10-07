package com.codeguardai.rule.quality;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.Severity;
import com.codeguardai.rule.CodeRule;
import org.springframework.stereotype.Component;

@Component
public class SystemOutPrintlnRule implements CodeRule {

	private static final Pattern SYSTEM_OUT_PRINTLN = Pattern.compile(
			"\\bSystem\\s*\\.\\s*out\\s*\\.\\s*println\\s*\\(");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		for (int index = 0; index < lines.size(); index++) {
			if (SYSTEM_OUT_PRINTLN.matcher(lines.get(index)).find()) {
				findings.add(new Finding(
						"QUAL003",
						"System.out.println usage",
						"Printing directly to standard output is not suitable for application logging.",
						IssueCategory.STYLE,
						Severity.LOW,
						index + 1,
						"Use a logging framework instead of System.out.println."));
			}
		}
		return findings;
	}
}
