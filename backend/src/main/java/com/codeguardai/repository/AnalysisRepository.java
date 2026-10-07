package com.codeguardai.repository;

import java.util.List;

import com.codeguardai.entity.Analysis;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnalysisRepository extends JpaRepository<Analysis, Long> {

	List<Analysis> findAllByOrderByAnalyzedAtDescIdDesc();
}
