package com.codeguardai.service;

import com.codeguardai.analyzer.CodeAnalyzer;
import com.codeguardai.model.AnalysisResult;
import com.codeguardai.model.CodeAnalysisRequest;
import com.codeguardai.model.CodeAnalysisResponse;
import com.codeguardai.model.RiskAssessment;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CodeAnalysisService {

	private static final int MAX_SOURCE_LENGTH = 100_000;

	private final CodeAnalyzer codeAnalyzer;
	private final RiskScoringService riskScoringService;
	private final AnalysisHistoryService analysisHistoryService;

	public CodeAnalysisService(
			CodeAnalyzer codeAnalyzer,
			RiskScoringService riskScoringService,
			AnalysisHistoryService analysisHistoryService) {
		this.codeAnalyzer = codeAnalyzer;
		this.riskScoringService = riskScoringService;
		this.analysisHistoryService = analysisHistoryService;
	}

	public CodeAnalysisResponse analyze(CodeAnalysisRequest request) {
		if (request == null || request.code() == null) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code must not be null.");
		}
		if (request.code().length() > MAX_SOURCE_LENGTH) {
			throw new ResponseStatusException(
					HttpStatus.CONTENT_TOO_LARGE,
					"Code must not exceed " + MAX_SOURCE_LENGTH + " characters.");
		}
		if (request.code().isBlank()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code must not be empty.");
		}

		AnalysisResult result = codeAnalyzer.analyze(request.code());
		RiskAssessment riskAssessment = riskScoringService.assess(result);
		CodeAnalysisResponse response = new CodeAnalysisResponse(
				riskAssessment.score(),
				riskAssessment.riskLevel(),
				result.totalIssues(),
				result.criticalCount(),
				result.highCount(),
				result.mediumCount(),
				result.lowCount(),
				result.findings());
		analysisHistoryService.save(response);
		return response;
	}
}
