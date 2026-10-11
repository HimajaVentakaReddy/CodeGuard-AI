package com.codeguardai.controller;

import com.codeguardai.model.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice(basePackageClasses = CodeAnalysisController.class)
public class ApiExceptionHandler {

	@ExceptionHandler(ResponseStatusException.class)
	public ResponseEntity<ApiErrorResponse> handleResponseStatusException(ResponseStatusException exception) {
		HttpStatusCode status = exception.getStatusCode();
		String message = exception.getReason();
		if (message == null || message.isBlank()) {
			message = reasonPhrase(status);
		}
		return errorResponse(status, message);
	}

	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ApiErrorResponse> handleUnreadableRequest(HttpMessageNotReadableException exception) {
		return errorResponse(HttpStatus.BAD_REQUEST, "Request body must contain valid JSON for this endpoint.");
	}

	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public ResponseEntity<ApiErrorResponse> handleInvalidPathParameter(
			MethodArgumentTypeMismatchException exception) {
		return errorResponse(
				HttpStatus.BAD_REQUEST,
				"Path parameter '" + exception.getName() + "' must be a valid integer.");
	}

	@ExceptionHandler(MissingServletRequestPartException.class)
	public ResponseEntity<ApiErrorResponse> handleMissingRequestPart(MissingServletRequestPartException exception) {
		return errorResponse(HttpStatus.BAD_REQUEST, "Required multipart file part is missing.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ApiErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException exception) {
		return errorResponse(HttpStatus.CONTENT_TOO_LARGE, "Uploaded request exceeds the configured size limit.");
	}

	private static ResponseEntity<ApiErrorResponse> errorResponse(HttpStatusCode status, String message) {
		return ResponseEntity.status(status)
				.body(new ApiErrorResponse(status.value(), reasonPhrase(status), message));
	}

	private static String reasonPhrase(HttpStatusCode status) {
		HttpStatus knownStatus = HttpStatus.resolve(status.value());
		return knownStatus == null ? "HTTP Error" : knownStatus.getReasonPhrase();
	}
}
