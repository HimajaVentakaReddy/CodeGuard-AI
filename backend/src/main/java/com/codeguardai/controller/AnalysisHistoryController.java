package com.codeguardai.controller;

import java.util.List;

import com.codeguardai.model.AnalysisSummary;
import com.codeguardai.service.AnalysisHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analyses")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500", "https://himajaventakareddy.github.io"})
@Tag(name = "History", description = "Read saved analysis summaries")
public class AnalysisHistoryController {

	private final AnalysisHistoryService analysisHistoryService;

	public AnalysisHistoryController(AnalysisHistoryService analysisHistoryService) {
		this.analysisHistoryService = analysisHistoryService;
	}

	@GetMapping
	@Operation(summary = "List recent analyses", description = "Returns saved analysis summaries in most-recent-first order.")
	@ApiResponse(responseCode = "200", description = "Analysis summaries returned")
	@ApiResponse(responseCode = "500", description = "Unable to read analysis history")
	public List<AnalysisSummary> getRecentAnalyses() {
		return analysisHistoryService.getRecentAnalyses();
	}

	@GetMapping("/{id}")
	@Operation(summary = "Get an analysis summary", description = "Returns one saved analysis summary. Source code and individual findings are not stored.")
	@Parameter(name = "id", description = "Positive identifier of a saved analysis.", example = "1")
	@ApiResponse(responseCode = "200", description = "Analysis summary returned")
	@ApiResponse(responseCode = "400", description = "Identifier is not a valid integer")
	@ApiResponse(responseCode = "404", description = "No analysis exists with this identifier")
	@ApiResponse(responseCode = "500", description = "Unable to read analysis history")
	public AnalysisSummary getAnalysis(@PathVariable Long id) {
		return analysisHistoryService.getAnalysis(id);
	}
}
