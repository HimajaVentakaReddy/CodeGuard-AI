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

	public static RiskLevel fromSeverity(Severity severity) {
		return switch (severity) {
			case CRITICAL -> CRITICAL;
			case HIGH -> HIGH;
			case MEDIUM -> MEDIUM;
			case LOW -> LOW;
			case INFO -> GOOD;
		};
	}

	public static RiskLevel moreSevere(RiskLevel first, RiskLevel second) {
		return first.ordinal() <= second.ordinal() ? first : second;
	}
}
