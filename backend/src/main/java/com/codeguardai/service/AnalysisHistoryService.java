package com.codeguardai.service;

import java.util.List;

import com.codeguardai.entity.Analysis;
import com.codeguardai.model.AnalysisSummary;
import com.codeguardai.model.CodeAnalysisResponse;
import com.codeguardai.repository.AnalysisRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalysisHistoryService {

	private final AnalysisRepository analysisRepository;

	public AnalysisHistoryService(AnalysisRepository analysisRepository) {
		this.analysisRepository = analysisRepository;
	}

	public AnalysisSummary save(CodeAnalysisResponse response) {
		Analysis savedAnalysis = analysisRepository.save(new Analysis(
				response.score(),
				response.riskLevel(),
				response.totalIssues(),
				response.criticalCount(),
				response.highCount(),
				response.mediumCount(),
				response.lowCount()));
		return AnalysisSummary.from(savedAnalysis);
	}

	public List<AnalysisSummary> getRecentAnalyses() {
		return analysisRepository.findAllByOrderByAnalyzedAtDescIdDesc()
				.stream()
				.map(AnalysisSummary::from)
				.toList();
	}

	public AnalysisSummary getAnalysis(Long id) {
		return analysisRepository.findById(id)
				.map(AnalysisSummary::from)
				.orElseThrow(() -> new ResponseStatusException(
						HttpStatus.NOT_FOUND,
						"Analysis " + id + " was not found."));
	}
}
