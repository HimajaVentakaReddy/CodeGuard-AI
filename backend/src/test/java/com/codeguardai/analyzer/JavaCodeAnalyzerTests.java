package com.codeguardai.analyzer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import com.codeguardai.model.AnalysisResult;
import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.Severity;
import com.codeguardai.rule.CodeRule;
import com.codeguardai.rule.quality.EmptyCatchBlockRule;
import com.codeguardai.rule.quality.SystemOutPrintlnRule;
import com.codeguardai.rule.quality.TodoCommentRule;
import com.codeguardai.rule.security.DangerousRuntimeExecutionRule;
import com.codeguardai.rule.security.HardcodedApiKeyRule;
import com.codeguardai.rule.security.HardcodedPasswordRule;
import com.codeguardai.rule.security.ProcessBuilderExecutionRule;
import org.junit.jupiter.api.Test;

class JavaCodeAnalyzerTests {

	private final CodeAnalyzer analyzer = new JavaCodeAnalyzer(List.of(
			new HardcodedPasswordRule(),
			new HardcodedApiKeyRule(),
			new DangerousRuntimeExecutionRule(),
			new ProcessBuilderExecutionRule(),
			new EmptyCatchBlockRule(),
			new TodoCommentRule(),
			new SystemOutPrintlnRule()));

	@Test
	void detectsHardcodedPasswordWithLineNumber() {
		Finding finding = analyzeSingleFinding("""
				class Sample {
				    String password = "admin123";
				}
				""");

		assertEquals("SEC001", finding.ruleId());
		assertEquals(2, finding.lineNumber());
		assertEquals(Severity.HIGH, finding.severity());
		assertEquals(IssueCategory.SECURITY, finding.category());
	}

	@Test
	void detectsHardcodedApiKeyTokenAndSecretAssignments() {
		AnalysisResult result = analyzer.analyze("""
				String apiKey = "key-value";
				String api_key = "another-key";
				String accessToken = "token-value";
				String secret = "secret-value";
				""");

		assertEquals(4, result.totalIssues());
		assertTrue(result.findings().stream().allMatch(finding -> finding.ruleId().equals("SEC002")));
		assertEquals(List.of(1, 2, 3, 4),
				result.findings().stream().map(Finding::lineNumber).toList());
	}

	@Test
	void detectsMultipleCredentialAssignmentsOnOneLine() {
		AnalysisResult result = analyzer.analyze(
				"String password = \"first\"; String passwd = \"second\"; String apiKey = \"third\";");

		assertEquals(3, result.totalIssues());
		assertEquals(List.of("SEC001", "SEC001", "SEC002"),
				result.findings().stream().map(Finding::ruleId).toList());
		assertEquals(List.of(1, 1, 1), result.findings().stream().map(Finding::lineNumber).toList());
	}

	@Test
	void ignoresEmptyOrNonLiteralCredentialValues() {
		AnalysisResult result = analyzer.analyze("""
				String password = "";
				String apiKey = null;
				String passwordHint = readPassword();
				""");

		assertEquals(0, result.totalIssues());
	}

	@Test
	void detectsDangerousRuntimeExecution() {
		Finding finding = analyzeSingleFinding("""
				class Sample {
				    void run() throws Exception {
				        Runtime.getRuntime().exec("whoami");
				    }
				}
				""");

		assertEquals("SEC003", finding.ruleId());
		assertEquals(3, finding.lineNumber());
		assertEquals(Severity.HIGH, finding.severity());
	}

	@Test
	void detectsStartedProcessBuilderCommands() {
		AnalysisResult result = analyzer.analyze("""
				ProcessBuilder direct = new ProcessBuilder("sh", "-c", command);
				direct.start();
				new ProcessBuilder("git", "status").start();
				""");

		assertEquals(2, result.totalIssues());
		assertEquals(List.of(2, 3), result.findings().stream().map(Finding::lineNumber).toList());
		assertTrue(result.findings().stream().allMatch(finding -> finding.ruleId().equals("SEC004")));
		assertTrue(result.findings().stream().allMatch(finding -> finding.severity() == Severity.HIGH));
	}

	@Test
	void doesNotFlagProcessBuilderThatIsNotStarted() {
		AnalysisResult result = analyzer.analyze(
				"ProcessBuilder builder = new ProcessBuilder(\"git\", \"status\");");

		assertEquals(0, result.totalIssues());
	}

	@Test
	void detectsSingleLineAndMultilineEmptyCatchBlocks() {
		AnalysisResult result = analyzer.analyze("""
				try { work(); } catch (Exception exception) { }
				try {
				    work();
				} catch (Exception exception) {
				}
				""");

		assertEquals(2, result.totalIssues());
		assertEquals(List.of(1, 4), result.findings().stream().map(Finding::lineNumber).toList());
		assertTrue(result.findings().stream().allMatch(finding -> finding.ruleId().equals("QUAL001")));
	}

	@Test
	void detectsTodoAndFixmeComments() {
		AnalysisResult result = analyzer.analyze("""
				// TODO: finish validation
				/* FIXME: handle error */
				String text = "TODO is only text";
				""");

		assertEquals(2, result.totalIssues());
		assertEquals(List.of(1, 2), result.findings().stream().map(Finding::lineNumber).toList());
		assertTrue(result.findings().stream().allMatch(finding -> finding.ruleId().equals("QUAL002")));
	}

	@Test
	void detectsSystemOutPrintln() {
		Finding finding = analyzeSingleFinding("""
				class Sample {
				    void print() {
				        System.out.println("debug");
				    }
				}
				""");

		assertEquals("QUAL003", finding.ruleId());
		assertEquals(3, finding.lineNumber());
		assertEquals(Severity.LOW, finding.severity());
	}

	@Test
	void calculatesIssueAndSeverityCountsForSampleCode() {
		AnalysisResult result = analyzer.analyze("""
				String password = "admin123";
				String apiKey = "key-value";
				try { work(); } catch (Exception exception) { }
				// TODO: replace debug output
				System.out.println("debug");
				""");

		assertEquals(5, result.totalIssues());
		assertEquals(0, result.criticalCount());
		assertEquals(2, result.highCount());
		assertEquals(1, result.mediumCount());
		assertEquals(1, result.lowCount());
		assertEquals(1, result.findings().stream()
				.filter(finding -> finding.severity() == Severity.INFO)
				.count());
		assertEquals(List.of(1, 2, 3, 4, 5),
				result.findings().stream().map(Finding::lineNumber).toList());
	}

	@Test
	void returnsNoFindingsForEmptySource() {
		assertEquals(0, analyzer.analyze("").totalIssues());
	}

	@Test
	void rejectsNullSource() {
		assertThrows(IllegalArgumentException.class, () -> analyzer.analyze(null));
	}

	private Finding analyzeSingleFinding(String sourceCode) {
		AnalysisResult result = analyzer.analyze(sourceCode);
		assertEquals(1, result.totalIssues());
		return result.findings().get(0);
	}
}
