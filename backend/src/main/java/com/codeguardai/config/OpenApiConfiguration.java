package com.codeguardai.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

	@Bean
	OpenAPI codeGuardOpenApi() {
		return new OpenAPI().info(new Info()
				.title("CodeGuard AI API")
				.version("1.0.0")
				.description("Analyze Java source code for security and code quality issues, and review saved analysis summaries."));
	}
}
