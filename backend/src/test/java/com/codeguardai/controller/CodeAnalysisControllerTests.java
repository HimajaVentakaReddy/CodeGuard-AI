package com.codeguardai.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.mock.web.MockMultipartFile;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;

@SpringBootTest
@AutoConfigureMockMvc
class CodeAnalysisControllerTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void analyzesRealisticCleanJavaSample() throws Exception {
		String source = readSample("CleanAccountService.java");

		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content(jsonRequest(source)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.score").value(100))
				.andExpect(jsonPath("$.totalIssues").value(0))
				.andExpect(jsonPath("$.findings").isEmpty());
	}

	@Test
	void analyzesRealisticRiskyJavaSampleUpload() throws Exception {
		byte[] source = readSample("LegacyAccountService.java").getBytes(StandardCharsets.UTF_8);
		MockMultipartFile file = new MockMultipartFile(
				"file", "LegacyAccountService.java", "text/plain", source);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalIssues").value(6))
				.andExpect(jsonPath("$.findings.length()").value(6))
				.andExpect(jsonPath("$.findings[*].ruleId").value(
						org.hamcrest.Matchers.containsInAnyOrder(
								"SEC001", "SEC002", "SEC003", "QUAL001", "QUAL002", "QUAL003")));
	}

	@Test
	void analyzesValidJavaCodeWithNoFindings() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"public class Test {\\n    public void run() {}\\n}"}
								"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.score").value(100))
								.andExpect(jsonPath("$.riskLevel").value("GOOD"))
								.andExpect(jsonPath("$.totalIssues").value(0))
				.andExpect(jsonPath("$.findings").isEmpty());
	}

	@Test
	void rejectsEmptyCode() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"\"}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void returnsConsistentJsonForEmptyAndBlankCode() throws Exception {
		for (String code : new String[] {"", "  \\n\\t "}) {
			mockMvc.perform(post("/api/analyze")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{\"code\":\"" + code + "\"}"))
					.andExpect(status().isBadRequest())
					.andExpect(jsonPath("$.status").value(400))
					.andExpect(jsonPath("$.error").value("Bad Request"))
					.andExpect(jsonPath("$.message").value("Code must not be empty."));
		}
	}

	@Test
	void returnsVulnerabilityFindingsAndSeverityCounts() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("""
								{"code":"String password = \\"admin123\\";\\ntry { work(); } catch (Exception exception) { }\\nSystem.out.println(\\"debug\\");"}
								"""))
				.andExpect(status().isOk())
								.andExpect(jsonPath("$.score").value(74))
								.andExpect(jsonPath("$.riskLevel").value("LOW"))
				.andExpect(jsonPath("$.totalIssues").value(3))
				.andExpect(jsonPath("$.criticalCount").value(0))
				.andExpect(jsonPath("$.highCount").value(1))
				.andExpect(jsonPath("$.mediumCount").value(1))
				.andExpect(jsonPath("$.lowCount").value(1))
				.andExpect(jsonPath("$.findings.length()").value(3))
				.andExpect(jsonPath("$.findings[0].ruleId").value("SEC001"))
				.andExpect(jsonPath("$.findings[0].category").value("SECURITY"))
				.andExpect(jsonPath("$.findings[0].severity").value("HIGH"))
				.andExpect(jsonPath("$.findings[0].lineNumber").value(1))
				.andExpect(jsonPath("$.findings[0].recommendation").isNotEmpty())
				.andExpect(jsonPath("$.findings[1].ruleId").value("QUAL001"))
				.andExpect(jsonPath("$.findings[1].category").value("BUG"))
				.andExpect(jsonPath("$.findings[1].severity").value("MEDIUM"))
				.andExpect(jsonPath("$.findings[1].lineNumber").value(2))
				.andExpect(jsonPath("$.findings[2].ruleId").value("QUAL003"))
				.andExpect(jsonPath("$.findings[2].category").value("STYLE"))
				.andExpect(jsonPath("$.findings[2].severity").value("LOW"))
				.andExpect(jsonPath("$.findings[2].lineNumber").value(3));
	}

	@Test
	void returnsBadRequestWhenCodeIsNull() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":null}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message").value("Code must not be null."));
	}

	@Test
	void rejectsWhitespaceOnlyCode() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"code\":\"  \\n  \"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.message").value("Code must not be empty."));
	}

	@Test
	void rejectsCodeAboveMaximumSourceLength() throws Exception {
		String oversizedCode = "a".repeat(100_001);
		String requestBody = "{\"code\":\"" + oversizedCode + "\"}";

		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content(requestBody))
				.andExpect(status().is(413))
				.andExpect(jsonPath("$.status").value(413))
				.andExpect(jsonPath("$.error").value("Content Too Large"))
				.andExpect(jsonPath("$.message").value("Code must not exceed 100000 characters."));
	}

	@Test
	void returnsConsistentJsonForMalformedJson() throws Exception {
		mockMvc.perform(post("/api/analyze")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{invalid-json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.error").value("Bad Request"))
				.andExpect(jsonPath("$.message")
						.value("Request body must contain valid JSON for this endpoint."));
	}

	@Test
	void analyzesUploadedJavaFileAsUtf8() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"Sample.java",
				"text/plain",
				"// Café\nString password = \"admin123\";\n".getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.totalIssues").value(1))
				.andExpect(jsonPath("$.findings[0].ruleId").value("SEC001"))
				.andExpect(jsonPath("$.findings[0].lineNumber").value(2));
	}

	@Test
	void rejectsUploadedFileWithInvalidExtension() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "Sample.txt", "text/plain", "class Sample {}".getBytes(StandardCharsets.UTF_8));

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.status").value(400))
						.andExpect(jsonPath("$.error").value("Bad Request"))
						.andExpect(jsonPath("$.message").value("Only .java files are allowed."));
	}

	@Test
	void rejectsEmptyUploadedFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "Sample.java", "text/plain", new byte[0]);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.status").value(400))
						.andExpect(jsonPath("$.error").value("Bad Request"))
						.andExpect(jsonPath("$.message").value("Uploaded file must not be empty."));
	}

	@Test
	void rejectsUploadedFileWithBlankSource() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "Blank.java", "text/plain", new byte[] {' ', '\n', '\t', ' '});

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
								.multipart("/api/analyze/file")
								.file(file))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.status").value(400))
						.andExpect(jsonPath("$.error").value("Bad Request"))
						.andExpect(jsonPath("$.message").value("Code must not be empty."));
	}

	@Test
	void rejectsOversizedUploadedFile() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file",
				"Sample.java",
				"text/plain",
				new byte[100_001]);

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
						.andExpect(status().is(413))
						.andExpect(jsonPath("$.status").value(413))
						.andExpect(jsonPath("$.error").value("Content Too Large"))
						.andExpect(jsonPath("$.message").value("Uploaded file must not exceed 100000 bytes."));
	}

	@Test
	void rejectsUploadedFileWithInvalidUtf8() throws Exception {
		MockMultipartFile file = new MockMultipartFile(
				"file", "Sample.java", "text/plain", new byte[] {(byte) 0xc3, (byte) 0x28});

		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
						.multipart("/api/analyze/file")
						.file(file))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.status").value(400))
						.andExpect(jsonPath("$.error").value("Bad Request"))
						.andExpect(jsonPath("$.message").value("Uploaded file must contain valid UTF-8 text."));
	}

	@Test
	void returnsConsistentJsonWhenUploadPartIsMissing() throws Exception {
		mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders
								.multipart("/api/analyze/file"))
						.andExpect(status().isBadRequest())
						.andExpect(jsonPath("$.status").value(400))
						.andExpect(jsonPath("$.error").value("Bad Request"))
						.andExpect(jsonPath("$.message").value("Required multipart file part is missing."));
	}

	private String readSample(String fileName) throws Exception {
		try (var stream = getClass().getResourceAsStream("/samples/" + fileName)) {
			if (stream == null) {
				throw new IllegalStateException("Missing test sample: " + fileName);
			}
			return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private String jsonRequest(String source) {
		String escaped = source.replace("\\", "\\\\")
				.replace("\"", "\\\"")
				.replace("\t", "\\t")
				.replace("\r", "\\r")
				.replace("\n", "\\n");
		return "{\"code\":\"" + escaped + "\"}";
	}
}
