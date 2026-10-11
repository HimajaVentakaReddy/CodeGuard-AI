package com.codeguardai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.codeguardai.repository.AnalysisRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class AnalysisHistoryControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private AnalysisRepository analysisRepository;

	@BeforeEach
	void clearHistory() {
		analysisRepository.deleteAll();
	}

	@Test
	void savesSuccessfulAnalysesAndReturnsRecentSummaries() throws Exception {
		long firstId = analyzeAndGetId("{\"code\":\"public class First {}\"}");
		long secondId = analyzeAndGetId("{\"code\":\"String password = \\\"admin123\\\";\"}");

		mockMvc.perform(get("/api/analyses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(2))
				.andExpect(jsonPath("$[0].id").value(secondId))
				.andExpect(jsonPath("$[0].score").value(85))
				.andExpect(jsonPath("$[0].riskLevel").value("HIGH"))
				.andExpect(jsonPath("$[0].totalIssues").value(1))
				.andExpect(jsonPath("$[0].highCount").value(1))
				.andExpect(jsonPath("$[0].analyzedAt").isNotEmpty())
				.andExpect(jsonPath("$[0].code").doesNotExist())
				.andExpect(jsonPath("$[1].id").value(firstId));

		mockMvc.perform(get("/api/analyses/{id}", secondId))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.id").value(secondId))
				.andExpect(jsonPath("$.score").value(85))
				.andExpect(jsonPath("$.highCount").value(1))
				.andExpect(jsonPath("$.code").doesNotExist());
	}

	@Test
	void returnsNotFoundForUnknownAnalysisId() throws Exception {
		mockMvc.perform(get("/api/analyses/{id}", Long.MAX_VALUE))
				.andExpect(status().isNotFound());
	}

	@Test
	void returnsConsistentJsonForNonNumericAnalysisId() throws Exception {
		mockMvc.perform(get("/api/analyses/not-a-number"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message")
						.value("Path parameter 'id' must be a valid integer."));
	}

	@Test
	void doesNotSaveRejectedAnalysisRequests() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"  \"}"))
				.andExpect(status().isBadRequest());

		mockMvc.perform(get("/api/analyses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.length()").value(0));
	}

	private Long analyzeAndGetId(String requestBody) throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalIssues").isNumber());
		return analysisRepository.findAllByOrderByAnalyzedAtDescIdDesc().get(0).getId();
	}
}
