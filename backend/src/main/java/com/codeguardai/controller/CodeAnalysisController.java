package com.codeguardai.controller;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import com.codeguardai.model.CodeAnalysisRequest;
import com.codeguardai.model.CodeAnalysisResponse;
import com.codeguardai.service.CodeAnalysisService;
import com.codeguardai.service.CodeAnalysisLimits;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:5500", "http://127.0.0.1:5500"})
@Tag(name = "Analysis", description = "Analyze Java source code")
public class CodeAnalysisController {

	private final CodeAnalysisService codeAnalysisService;

	public CodeAnalysisController(CodeAnalysisService codeAnalysisService) {
		this.codeAnalysisService = codeAnalysisService;
	}

	@PostMapping("/analyze")
	@Operation(
			summary = "Analyze Java source",
			description = "Analyzes Java source supplied as JSON and saves an issue-count summary to history.")
	@ApiResponse(responseCode = "200", description = "Analysis completed")
	@ApiResponse(responseCode = "400", description = "Code is missing, null, empty, or whitespace-only")
	@ApiResponse(responseCode = "413", description = "Source exceeds 100,000 characters")
	@ApiResponse(responseCode = "500", description = "Unexpected analysis or persistence error")
	public CodeAnalysisResponse analyze(@RequestBody CodeAnalysisRequest request) {
		return codeAnalysisService.analyze(request);
	}

	@PostMapping(path = "/analyze/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	@Operation(
			summary = "Analyze a Java file",
			description = "Analyzes a UTF-8 .java file up to 100,000 bytes and saves an issue-count summary to history.")
	@ApiResponse(responseCode = "200", description = "Analysis completed")
	@ApiResponse(responseCode = "400", description = "File is missing, empty, not a .java file, invalid UTF-8, or contains blank source")
	@ApiResponse(responseCode = "413", description = "File or multipart request exceeds the configured size limit")
	@ApiResponse(responseCode = "500", description = "Unable to read the uploaded file or save the analysis")
	public CodeAnalysisResponse analyzeFile(
			@Parameter(description = "Java source file. The multipart field must be named 'file'.", required = true)
			@RequestPart("file") MultipartFile file) {
		validateFile(file);

		byte[] contents;
		try {
			contents = file.getBytes();
		} catch (IOException exception) {
			throw new ResponseStatusException(
					HttpStatus.INTERNAL_SERVER_ERROR,
					"Unable to read the uploaded Java file.",
					exception);
		}

		try {
			String sourceCode = StandardCharsets.UTF_8.newDecoder()
					.onMalformedInput(CodingErrorAction.REPORT)
					.onUnmappableCharacter(CodingErrorAction.REPORT)
					.decode(ByteBuffer.wrap(contents))
					.toString();
			return codeAnalysisService.analyze(new CodeAnalysisRequest(sourceCode));
		} catch (CharacterCodingException exception) {
			throw new ResponseStatusException(
					HttpStatus.BAD_REQUEST,
					"Uploaded file must contain valid UTF-8 text.",
					exception);
		}
	}

	private static void validateFile(MultipartFile file) {
		if (file == null || file.isEmpty()) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Uploaded file must not be empty.");
		}
		if (file.getSize() > CodeAnalysisLimits.MAX_FILE_SIZE_BYTES) {
			throw new ResponseStatusException(
					HttpStatus.CONTENT_TOO_LARGE,
					"Uploaded file must not exceed " + CodeAnalysisLimits.MAX_FILE_SIZE_BYTES + " bytes.");
		}

		String filename = file.getOriginalFilename();
		if (filename == null || !filename.toLowerCase(Locale.ROOT).endsWith(".java")) {
			throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only .java files are allowed.");
		}
	}
}
