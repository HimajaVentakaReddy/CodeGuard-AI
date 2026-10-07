package com.codeguardai.entity;

import java.time.Instant;

import com.codeguardai.model.RiskLevel;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "analyses")
public class Analysis {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, updatable = false)
	private Instant analyzedAt;

	@Column(nullable = false)
	private int score;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 16)
	private RiskLevel riskLevel;

	@Column(nullable = false)
	private int totalIssues;

	@Column(nullable = false)
	private int criticalCount;

	@Column(nullable = false)
	private int highCount;

	@Column(nullable = false)
	private int mediumCount;

	@Column(nullable = false)
	private int lowCount;

	protected Analysis() {
	}

	public Analysis(
			int score,
			RiskLevel riskLevel,
			int totalIssues,
			int criticalCount,
			int highCount,
			int mediumCount,
			int lowCount) {
		this.score = score;
		this.riskLevel = riskLevel;
		this.totalIssues = totalIssues;
		this.criticalCount = criticalCount;
		this.highCount = highCount;
		this.mediumCount = mediumCount;
		this.lowCount = lowCount;
	}

	@PrePersist
	void setAnalyzedAt() {
		if (analyzedAt == null) {
			analyzedAt = Instant.now();
		}
	}

	public Long getId() {
		return id;
	}

	public Instant getAnalyzedAt() {
		return analyzedAt;
	}

	public int getScore() {
		return score;
	}

	public RiskLevel getRiskLevel() {
		return riskLevel;
	}

	public int getTotalIssues() {
		return totalIssues;
	}

	public int getCriticalCount() {
		return criticalCount;
	}

	public int getHighCount() {
		return highCount;
	}

	public int getMediumCount() {
		return mediumCount;
	}

	public int getLowCount() {
		return lowCount;
	}
}
