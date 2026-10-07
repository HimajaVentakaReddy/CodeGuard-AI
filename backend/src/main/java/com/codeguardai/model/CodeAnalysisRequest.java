package com.codeguardai.model;

import io.swagger.v3.oas.annotations.media.Schema;

public record CodeAnalysisRequest(
		@Schema(description = "Java source code to analyze.", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100_000)
		String code) {
}
