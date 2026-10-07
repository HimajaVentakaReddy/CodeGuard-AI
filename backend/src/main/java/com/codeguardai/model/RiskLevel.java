package com.codeguardai.model;

public enum RiskLevel {
	CRITICAL,
	HIGH,
	MEDIUM,
	LOW,
	GOOD;

	public static RiskLevel fromScore(int score) {
		if (score <= 20) {
			return CRITICAL;
		}
		if (score <= 40) {
			return HIGH;
		}
		if (score <= 60) {
			return MEDIUM;
		}
		if (score <= 80) {
			return LOW;
		}
		return GOOD;
	}
}
