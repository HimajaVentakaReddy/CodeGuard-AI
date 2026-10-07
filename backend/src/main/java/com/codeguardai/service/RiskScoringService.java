package com.codeguardai.service;

import com.codeguardai.model.AnalysisResult;
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

		return new RiskAssessment(score, RiskLevel.fromScore(score));
	}
}
