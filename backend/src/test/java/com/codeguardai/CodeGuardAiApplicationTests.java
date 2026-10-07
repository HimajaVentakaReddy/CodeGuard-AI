package com.codeguardai;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CodeGuardAiApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Value("${spring.servlet.multipart.max-file-size}")
	private String maxFileSize;

	@Value("${spring.servlet.multipart.max-request-size}")
	private String maxRequestSize;

	@Test
	void contextLoads() {
	}

	@Test
	void multipartUploadLimitsRemainBounded() {
		assertEquals(100 * 1024L, DataSize.parse(maxFileSize).toBytes());
		assertEquals(120 * 1024L, DataSize.parse(maxRequestSize).toBytes());
	}

	@Test
	void projectInfoEndpointReturnsProjectDetails() throws Exception {
		mockMvc.perform(get("/api/info"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.name").value("CodeGuard AI"))
				.andExpect(jsonPath("$.version").value("1.0.0"))
				.andExpect(jsonPath("$.description")
						.value("Intelligent Code Quality & Vulnerability Analyzer"));
	}

	@Test
	void healthEndpointRemainsAvailable() throws Exception {
		mockMvc.perform(get("/api/health"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.status").value("UP"))
				.andExpect(jsonPath("$.service").value("CodeGuard AI"));
	}

	@Test
	void openApiDocumentDescribesAllPublicEndpoints() throws Exception {
		mockMvc.perform(get("/v3/api-docs"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.info.title").value("CodeGuard AI API"))
				.andExpect(jsonPath("$.paths['/api/health'].get").exists())
				.andExpect(jsonPath("$.paths['/api/info'].get").exists())
				.andExpect(jsonPath("$.paths['/api/analyze'].post").exists())
				.andExpect(jsonPath("$.paths['/api/analyze/file'].post").exists())
				.andExpect(jsonPath("$.paths['/api/analyses'].get").exists())
				.andExpect(jsonPath("$.paths['/api/analyses/{id}'].get").exists());
	}

	@Test
	void swaggerUiIsAvailable() throws Exception {
		mockMvc.perform(get("/swagger-ui/index.html"))
				.andExpect(status().isOk());
	}
}
