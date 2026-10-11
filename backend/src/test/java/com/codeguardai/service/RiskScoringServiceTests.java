package com.codeguardai.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import com.codeguardai.model.AnalysisResult;
import com.codeguardai.model.Finding;
import com.codeguardai.model.IssueCategory;
import com.codeguardai.model.RiskAssessment;
import com.codeguardai.model.RiskLevel;
import com.codeguardai.model.Severity;
import org.junit.jupiter.api.Test;

class RiskScoringServiceTests {

	private final RiskScoringService scoringService = new RiskScoringService();

	@Test
	void appliesSpecifiedSeverityWeights() {
		AnalysisResult result = resultWithCounts(1, 1, 1, 1, 1);

		RiskAssessment assessment = scoringService.assess(result);

		assertEquals(49, assessment.score());
		assertEquals(RiskLevel.CRITICAL, assessment.riskLevel());
	}

	@Test
	void highSeverityFindingPreventsGoodRiskClassification() {
		RiskAssessment assessment = scoringService.assess(resultWithCounts(0, 1, 0, 0, 0));

		assertEquals(85, assessment.score());
		assertEquals(RiskLevel.HIGH, assessment.riskLevel());
	}

	@Test
	void lowSeverityFindingPreventsGoodRiskClassification() {
		RiskAssessment assessment = scoringService.assess(resultWithCounts(0, 0, 0, 1, 0));

		assertEquals(97, assessment.score());
		assertEquals(RiskLevel.LOW, assessment.riskLevel());
	}

	@Test
	void scoreBandCanRaiseRiskAboveTheHighestFindingSeverity() {
		RiskAssessment assessment = scoringService.assess(resultWithCounts(0, 0, 0, 14, 0));

		assertEquals(58, assessment.score());
		assertEquals(RiskLevel.MEDIUM, assessment.riskLevel());
	}

	@Test
	void infoFindingsDoNotReduceScore() {
		RiskAssessment assessment = scoringService.assess(resultWithCounts(0, 0, 0, 0, 5));

		assertEquals(100, assessment.score());
		assertEquals(RiskLevel.GOOD, assessment.riskLevel());
	}

	@Test
	void scoreNeverDropsBelowZero() {
		RiskAssessment assessment = scoringService.assess(resultWithCounts(5, 0, 0, 0, 0));

		assertEquals(0, assessment.score());
		assertEquals(RiskLevel.CRITICAL, assessment.riskLevel());
	}

	@Test
	void mapsScoreBoundariesToRiskLevels() {
		assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(0));
		assertEquals(RiskLevel.CRITICAL, RiskLevel.fromScore(20));
		assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(21));
		assertEquals(RiskLevel.HIGH, RiskLevel.fromScore(40));
		assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(41));
		assertEquals(RiskLevel.MEDIUM, RiskLevel.fromScore(60));
		assertEquals(RiskLevel.LOW, RiskLevel.fromScore(61));
		assertEquals(RiskLevel.LOW, RiskLevel.fromScore(80));
		assertEquals(RiskLevel.GOOD, RiskLevel.fromScore(81));
		assertEquals(RiskLevel.GOOD, RiskLevel.fromScore(100));
	}

	private static AnalysisResult resultWithCounts(
			int critical, int high, int medium, int low, int info) {
		List<Finding> findings = new ArrayList<>();
		addFindings(findings, critical, Severity.CRITICAL);
		addFindings(findings, high, Severity.HIGH);
		addFindings(findings, medium, Severity.MEDIUM);
		addFindings(findings, low, Severity.LOW);
		addFindings(findings, info, Severity.INFO);
		return new AnalysisResult(findings);
	}

	private static void addFindings(List<Finding> findings, int count, Severity severity) {
		for (int index = 0; index < count; index++) {
			findings.add(new Finding(
					"TEST",
					"Test finding",
					"Used to test scoring.",
					IssueCategory.CODE_SMELL,
					severity,
					index + 1,
					"Resolve the test finding."));
		}
	}
}
