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
public class TodoCommentRule implements CodeRule {

	private static final Pattern TODO_OR_FIXME = Pattern.compile(
			"(?i)(//|/\\*|\\*).*\\b(TODO|FIXME)\\b");

	@Override
	public List<Finding> analyze(List<String> lines) {
		List<Finding> findings = new ArrayList<>();
		for (int index = 0; index < lines.size(); index++) {
			if (TODO_OR_FIXME.matcher(lines.get(index)).find()) {
				findings.add(new Finding(
						"QUAL002",
						"TODO or FIXME comment",
						"A TODO or FIXME comment marks unfinished or potentially problematic work.",
						IssueCategory.CODE_SMELL,
						Severity.INFO,
						index + 1,
						"Complete the noted work or track it in the project's issue system."));
			}
		}
		return findings;
	}
}
