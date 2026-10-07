package com.codeguardai.model;

public record ApiErrorResponse(int status, String error, String message) {
}
