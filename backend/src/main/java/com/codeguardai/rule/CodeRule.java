package com.codeguardai.rule;

import java.util.List;

import com.codeguardai.model.Finding;

public interface CodeRule {

	List<Finding> analyze(List<String> lines);
}
