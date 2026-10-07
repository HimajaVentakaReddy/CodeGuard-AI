package com.codeguardai.analyzer;

import com.codeguardai.model.AnalysisResult;

public interface CodeAnalyzer {

	AnalysisResult analyze(String sourceCode);
}
