package com.codeguardai.service;

import com.codeguardai.model.AnalysisResult;
import com.codeguardai.model.Finding;
import com.codeguardai.model.RiskAssessment;
import com.codeguardai.model.RiskLevel;
import org.springframework.stereotype.Service;

@Service
public class RiskScoringService {

	public RiskAssessment assess(AnalysisResult result) {
		long deductions = (long) result.criticalCount() * 25
				+ (long) result.highCount() * 15
				+ (long) result.mediumCount() * 8
				+ (long) result.lowCount() * 3;
		int score = (int) Math.max(0, Math.min(100, 100 - deductions));

		RiskLevel riskLevel = RiskLevel.fromScore(score);
		for (Finding finding : result.findings()) {
			RiskLevel findingRisk = RiskLevel.fromSeverity(finding.severity());
			riskLevel = RiskLevel.moreSevere(riskLevel, findingRisk);
		}

		return new RiskAssessment(score, riskLevel);
	}
}
