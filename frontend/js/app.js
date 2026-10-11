const ANALYZE_URL = "http://localhost:8080/api/analyze";
const HISTORY_URL = "http://localhost:8080/api/analyses";

const exampleCode = `public class UserService {
    // TODO: move credentials to a secure store
    private String password = "admin123";
    private String apiKey = "sk_live_example_key";

    public void runCommand(String command) throws Exception {
        try {
            Runtime.getRuntime().exec(command);
        } catch (Exception ignored) {
        }
    }

    public void debug() {
        System.out.println("service started");
    }
}`;

const elements = {
	analyzeButton: document.querySelector("#analyze-button"),
	analyzeLabel: document.querySelector("#analyze-label"),
	clearButton: document.querySelector("#clear-button"),
	codeInput: document.querySelector("#code-input"),
	criticalCount: document.querySelector("#critical-count"),
	categoryFilter: document.querySelector("#category-filter"),
	findingsBody: document.querySelector("#findings-body"),
	findingsSummary: document.querySelector("#findings-summary"),
	findingsTotal: document.querySelector("#findings-total"),
	highCount: document.querySelector("#high-count"),
	lineCount: document.querySelector("#line-count"),
	loadExampleButton: document.querySelector("#load-example"),
	lowCount: document.querySelector("#low-count"),
	mediumCount: document.querySelector("#medium-count"),
	reportState: document.querySelector("#report-state"),
	requestStatus: document.querySelector("#request-status"),
	riskLevel: document.querySelector("#risk-level"),
	severityFilter: document.querySelector("#severity-filter"),
	scoreCaption: document.querySelector("#score-caption"),
	scoreRing: document.querySelector("#score-ring"),
	scoreValue: document.querySelector("#score-value"),
	sourceLines: document.querySelector("#source-lines"),
	issueDensity: document.querySelector("#issue-density"),
	totalIssues: document.querySelector("#total-issues")
};

const historyElements = {
	body: document.querySelector("#history-body"),
	dashboardNav: document.querySelector("#dashboard-nav"),
	dashboardPage: document.querySelector("#dashboard-page"),
	historyNav: document.querySelector("#history-nav"),
	historyPage: document.querySelector("#history-page"),
	state: document.querySelector("#history-state"),
	tableWrap: document.querySelector("#history-table-wrap"),
	refreshButton: document.querySelector("#refresh-history"),
	exportButton: document.querySelector("#export-history"),
	dialog: document.querySelector("#summary-dialog"),
	closeDialog: document.querySelector("#close-summary"),
	summaryDate: document.querySelector("#summary-date"),
	summaryScore: document.querySelector("#summary-score"),
	summaryRisk: document.querySelector("#summary-risk"),
	summaryIssues: document.querySelector("#summary-issues"),
	summaryCritical: document.querySelector("#summary-critical"),
	summaryHigh: document.querySelector("#summary-high"),
	summaryMedium: document.querySelector("#summary-medium"),
	summaryLow: document.querySelector("#summary-low")
};

let currentFindings = [];
let currentHistory = [];

function updateLineCount() {
	const count = countSourceLines(elements.codeInput.value) || 1;
	elements.lineCount.textContent = `${count} ${count === 1 ? "line" : "lines"}`;
}

function countSourceLines(source) {
	if (!source) {
		return 0;
	}

	const lines = source.split(/\r\n|\r|\n/);
	return lines.length - (/\r\n|\r|\n$/.test(source) ? 1 : 0);
}

function setRequestStatus(message, state = "") {
	elements.requestStatus.textContent = message;
	elements.requestStatus.className = `request-status${state ? ` ${state}` : ""}`;
}

function setLoading(isLoading) {
	elements.analyzeButton.disabled = isLoading;
	elements.clearButton.disabled = isLoading;
	elements.loadExampleButton.disabled = isLoading;
	elements.analyzeLabel.textContent = isLoading ? "Analyzing..." : "Analyze code";
	if (isLoading) {
		elements.reportState.textContent = "Analysis in progress";
		elements.reportState.classList.remove("ready");
	}
}

function createCell(value, className = "") {
	const cell = document.createElement("td");
	cell.textContent = value ?? "";
	if (className) {
		cell.className = className;
	}
	return cell;
}

function renderFindings(findings) {
	const selectedSeverity = elements.severityFilter.value;
	const selectedCategory = elements.categoryFilter.value;
	const visibleFindings = findings.filter(finding =>
		(selectedSeverity === "ALL" || finding.severity === selectedSeverity)
		&& (selectedCategory === "ALL" || finding.category === selectedCategory));

	elements.findingsTotal.textContent = String(visibleFindings.length);
	elements.findingsSummary.textContent = visibleFindings.length === findings.length
		? `${findings.length} finding${findings.length === 1 ? "" : "s"} detected`
		: `${visibleFindings.length} of ${findings.length} findings shown`;
	elements.findingsBody.replaceChildren();

	if (!visibleFindings.length) {
		const row = document.createElement("tr");
		row.className = "empty-row";
		const cell = document.createElement("td");
		cell.colSpan = 6;
		const emptyState = document.createElement("div");
		emptyState.className = "empty-state";
		const icon = document.createElement("span");
		icon.className = "empty-icon";
		icon.setAttribute("aria-hidden", "true");
		icon.textContent = "✓";
		const title = document.createElement("strong");
		title.textContent = findings.length ? "No matching findings" : "No issues detected";
		const message = document.createElement("span");
		message.textContent = findings.length
			? "Change or clear the filters to see other findings."
			: "No findings matched the current analysis rules.";
		emptyState.append(icon, title, message);
		cell.append(emptyState);
		row.append(cell);
		elements.findingsBody.append(row);
		return;
	}

	for (const finding of visibleFindings) {
		const row = document.createElement("tr");
		const severity = String(finding.severity ?? "INFO").toLowerCase();
		const badgeCell = document.createElement("td");
		const badge = document.createElement("span");
		badge.className = `severity-badge severity-${severity}`;
		badge.textContent = finding.severity ?? "INFO";
		badgeCell.append(badge);

		const findingCell = document.createElement("td");
		findingCell.className = "finding-cell";
		const title = document.createElement("strong");
		title.className = "finding-title";
		title.textContent = finding.title ?? "Untitled finding";
		const ruleId = document.createElement("span");
		ruleId.className = "finding-rule-id";
		ruleId.textContent = finding.ruleId ?? "";
		findingCell.append(title, ruleId);

		row.append(
			badgeCell,
			findingCell,
			createCell(finding.category),
			createCell(finding.lineNumber),
			createCell(finding.description),
			createCell(finding.recommendation)
		);
		elements.findingsBody.append(row);
	}
}

function displayResult(result, analyzedSource) {
	const score = Math.max(0, Math.min(100, Number(result.score) || 0));
	const riskLevel = String(result.riskLevel ?? "UNKNOWN");
	const findings = Array.isArray(result.findings) ? result.findings : [];
	const sourceLines = countSourceLines(analyzedSource);

	elements.scoreValue.textContent = String(score);
	elements.scoreRing.style.setProperty("--score-angle", `${score * 3.6}deg`);
	elements.scoreRing.style.setProperty("--score-color", scoreColor(riskLevel));
	elements.scoreRing.setAttribute("aria-label", `CodeGuard score ${score} out of 100`);
	elements.riskLevel.textContent = `${riskLevel} RISK`;
	elements.riskLevel.dataset.risk = riskLevel.toLowerCase();
	elements.scoreCaption.textContent = scoreMessage(riskLevel);

	elements.totalIssues.textContent = result.totalIssues ?? findings.length;
	elements.criticalCount.textContent = result.criticalCount ?? 0;
	elements.highCount.textContent = result.highCount ?? 0;
	elements.mediumCount.textContent = result.mediumCount ?? 0;
	elements.lowCount.textContent = result.lowCount ?? 0;
	elements.sourceLines.textContent = String(sourceLines);
	elements.issueDensity.textContent = sourceLines
		? (findings.length * 100 / sourceLines).toFixed(1)
		: "0.0";

	elements.reportState.textContent = "Analysis complete";
	elements.reportState.classList.add("ready");
	elements.severityFilter.disabled = false;
	elements.categoryFilter.disabled = false;
	currentFindings = findings;
	renderFindings(findings);
}

function scoreColor(riskLevel) {
	switch (riskLevel) {
		case "CRITICAL": return "var(--red)";
		case "HIGH": return "var(--orange)";
		case "MEDIUM": return "var(--yellow)";
		case "LOW": return "var(--blue)";
		case "GOOD": return "var(--green)";
		default: return "var(--accent)";
	}
}

function scoreMessage(riskLevel) {
	switch (riskLevel) {
		case "CRITICAL": return "The combined findings indicate critical risk.";
		case "HIGH": return "The combined findings indicate high risk.";
		case "MEDIUM": return "The combined findings indicate medium risk.";
		case "LOW": return "The combined findings indicate low risk.";
		case "GOOD": return "No significant risks were detected.";
		default: return "The risk classification is unavailable.";
	}
}

async function analyzeCode() {
	const code = elements.codeInput.value;
	if (!code.trim()) {
		setRequestStatus("Add Java code before starting an analysis.", "error");
		elements.codeInput.focus();
		return;
	}

	setRequestStatus("");
	setLoading(true);

	try {
		const response = await fetch(ANALYZE_URL, {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ code })
		});
		const payload = await response.json().catch(() => null);

		if (!response.ok) {
			const message = payload?.detail || payload?.message || payload?.error;
			throw new Error(message || `Analysis failed with HTTP ${response.status}.`);
		}
		if (!payload || !Array.isArray(payload.findings)) {
			throw new Error("The analysis service returned an unexpected response.");
		}

		displayResult(payload, code);
		setRequestStatus("Analysis completed successfully.", "success");
		if (!historyElements.historyPage.hidden) {
			loadHistory();
		}
	} catch (error) {
		setRequestStatus(
			error instanceof TypeError
				? "Could not reach the API. Check that the backend is running on localhost:8080."
				: error.message,
			"error"
		);
		elements.reportState.textContent = "Analysis failed";
		elements.reportState.classList.remove("ready");
	} finally {
		setLoading(false);
	}
}

function showPage(page) {
	const showHistory = page === "history";
	historyElements.dashboardPage.hidden = showHistory;
	historyElements.historyPage.hidden = !showHistory;
	historyElements.dashboardNav.classList.toggle("active", !showHistory);
	historyElements.historyNav.classList.toggle("active", showHistory);
	if (showHistory) {
		historyElements.historyNav.setAttribute("aria-current", "page");
		historyElements.dashboardNav.removeAttribute("aria-current");
		loadHistory();
	} else {
		historyElements.dashboardNav.setAttribute("aria-current", "page");
		historyElements.historyNav.removeAttribute("aria-current");
	}
}

function setHistoryState(message, state) {
	historyElements.tableWrap.hidden = true;
	historyElements.state.hidden = false;
	historyElements.state.className = `history-state${state ? ` ${state}` : ""}`;
	historyElements.state.replaceChildren();

	if (state === "loading") {
		const spinner = document.createElement("span");
		spinner.className = "loading-spinner";
		spinner.setAttribute("aria-hidden", "true");
		historyElements.state.append(spinner);
	}

	const text = document.createElement("span");
	text.textContent = message;
	historyElements.state.append(text);

	if (state === "error") {
		const retryButton = document.createElement("button");
		retryButton.className = "button button-secondary button-small";
		retryButton.type = "button";
		retryButton.textContent = "Try again";
		retryButton.addEventListener("click", loadHistory);
		historyElements.state.append(retryButton);
	}
}

function formatAnalysisDate(value) {
	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return "Date unavailable";
	}
	return new Intl.DateTimeFormat(undefined, {
		dateStyle: "medium",
		timeStyle: "short"
	}).format(date);
}

function appendHistoryRow(analysis) {
	const row = document.createElement("tr");
	row.className = "history-row";

	const dateCell = document.createElement("td");
	dateCell.textContent = formatAnalysisDate(analysis.analyzedAt);
	const scoreCell = document.createElement("td");
	const score = document.createElement("span");
	score.className = "history-score";
	score.textContent = String(analysis.score);
	scoreCell.append(score);
	const riskCell = document.createElement("td");
	const risk = document.createElement("span");
	const riskLevel = String(analysis.riskLevel ?? "UNKNOWN");
	risk.className = `risk-badge risk-${riskLevel.toLowerCase()}`;
	risk.textContent = riskLevel;
	riskCell.append(risk);
	const issuesCell = document.createElement("td");
	issuesCell.textContent = String(analysis.totalIssues);
	const actionCell = document.createElement("td");
	const action = document.createElement("button");
	action.className = "history-open";
	action.type = "button";
	action.dataset.analysisId = String(analysis.id);
	action.setAttribute("aria-label",
		`View analysis from ${formatAnalysisDate(analysis.analyzedAt)}, score ${analysis.score}`);
	action.textContent = "View summary →";
	actionCell.append(action);

	row.append(dateCell, scoreCell, riskCell, issuesCell, actionCell);
	historyElements.body.append(row);
}

function csvField(value) {
	return `"${String(value ?? "").replaceAll('"', '""')}"`;
}

function exportHistoryCsv() {
	if (!currentHistory.length) {
		return;
	}

	const columns = [
		"Analysis ID",
		"Analyzed at",
		"Score",
		"Risk level",
		"Total issues",
		"Critical",
		"High",
		"Medium",
		"Low"
	];
	const rows = currentHistory.map(analysis => [
		analysis.id,
		analysis.analyzedAt,
		analysis.score,
		analysis.riskLevel,
		analysis.totalIssues,
		analysis.criticalCount,
		analysis.highCount,
		analysis.mediumCount,
		analysis.lowCount
	]);
	const csv = [columns, ...rows]
		.map(row => row.map(csvField).join(","))
		.join("\r\n");
	const url = URL.createObjectURL(new Blob([csv], { type: "text/csv;charset=utf-8" }));
	const download = document.createElement("a");
	download.href = url;
	download.download = `codeguard-history-${new Date().toISOString().replaceAll(":", "-")}.csv`;
	document.body.append(download);
	download.click();
	download.remove();
	setTimeout(() => URL.revokeObjectURL(url), 0);
}

async function loadHistory() {
	historyElements.refreshButton.disabled = true;
	historyElements.exportButton.disabled = true;
	currentHistory = [];
	setHistoryState("Loading analysis history...", "loading");

	try {
		const response = await fetch(HISTORY_URL);
		const payload = await response.json().catch(() => null);
		if (!response.ok) {
			throw new Error(payload?.detail || payload?.message || `History request failed with HTTP ${response.status}.`);
		}
		if (!Array.isArray(payload)) {
			throw new Error("The history service returned an unexpected response.");
		}

		historyElements.body.replaceChildren();
		if (!payload.length) {
			setHistoryState("No analyses saved yet. Run an analysis and it will appear here.", "empty");
			return;
		}

		currentHistory = payload;
		historyElements.exportButton.disabled = false;
		for (const analysis of payload) {
			appendHistoryRow(analysis);
		}
		historyElements.state.hidden = true;
		historyElements.tableWrap.hidden = false;
	} catch (error) {
		setHistoryState(
			error instanceof TypeError
				? "Could not connect to the history API. Check that the backend and database are running."
				: error.message,
			"error"
		);
	} finally {
		historyElements.refreshButton.disabled = false;
	}
}

async function showAnalysisSummary(id) {
	try {
		const response = await fetch(`${HISTORY_URL}/${encodeURIComponent(id)}`);
		const payload = await response.json().catch(() => null);
		if (!response.ok) {
			throw new Error(payload?.detail || payload?.message || `Could not load analysis summary (HTTP ${response.status}).`);
		}
		if (!payload || payload.id === undefined) {
			throw new Error("The history service returned an unexpected response.");
		}

		historyElements.summaryDate.textContent = formatAnalysisDate(payload.analyzedAt);
		historyElements.summaryScore.textContent = `${payload.score} / 100`;
		historyElements.summaryRisk.textContent = payload.riskLevel;
		historyElements.summaryIssues.textContent = payload.totalIssues;
		historyElements.summaryCritical.textContent = payload.criticalCount;
		historyElements.summaryHigh.textContent = payload.highCount;
		historyElements.summaryMedium.textContent = payload.mediumCount;
		historyElements.summaryLow.textContent = payload.lowCount;
		historyElements.dialog.showModal();
	} catch (error) {
		setHistoryState(
			error instanceof TypeError
				? "Could not connect to the history API. Check that the backend and database are running."
				: error.message,
			"error"
		);
	}
}

function clearWorkspace() {
	elements.codeInput.value = "";
	elements.severityFilter.value = "ALL";
	elements.categoryFilter.value = "ALL";
	elements.severityFilter.disabled = true;
	elements.categoryFilter.disabled = true;
	currentFindings = [];
	updateLineCount();
	setRequestStatus("");
	elements.scoreValue.textContent = "--";
	elements.scoreRing.style.setProperty("--score-angle", "0deg");
	elements.scoreRing.style.setProperty("--score-color", "var(--accent)");
	elements.scoreRing.setAttribute("aria-label", "No score yet");
	elements.riskLevel.textContent = "Not analyzed";
	elements.riskLevel.removeAttribute("data-risk");
	elements.scoreCaption.textContent = "Run an analysis to see your code health score.";
	elements.totalIssues.textContent = "--";
	elements.sourceLines.textContent = "--";
	elements.issueDensity.textContent = "--";
	elements.criticalCount.textContent = "--";
	elements.highCount.textContent = "--";
	elements.mediumCount.textContent = "--";
	elements.lowCount.textContent = "--";
	elements.findingsTotal.textContent = "0";
	elements.findingsSummary.textContent = "Run an analysis to see detected issues.";
	elements.reportState.textContent = "Waiting for analysis";
	elements.reportState.classList.remove("ready");
	renderInitialEmptyState();
}

function renderInitialEmptyState() {
	const row = document.createElement("tr");
	row.className = "empty-row";
	const cell = document.createElement("td");
	cell.colSpan = 6;
	const emptyState = document.createElement("div");
	emptyState.className = "empty-state";
	const icon = document.createElement("span");
	icon.className = "empty-icon";
	icon.setAttribute("aria-hidden", "true");
	icon.textContent = "⌕";
	const title = document.createElement("strong");
	title.textContent = "No analysis results yet";
	const message = document.createElement("span");
	message.textContent = "Add Java code above and select Analyze code to begin.";
	emptyState.append(icon, title, message);
	cell.append(emptyState);
	row.append(cell);
	elements.findingsBody.replaceChildren(row);
}

elements.analyzeButton.addEventListener("click", analyzeCode);
elements.clearButton.addEventListener("click", clearWorkspace);
elements.loadExampleButton.addEventListener("click", () => {
	elements.codeInput.value = exampleCode;
	updateLineCount();
	setRequestStatus("Example loaded. Select Analyze code to run it.");
	elements.codeInput.focus();
});
elements.codeInput.addEventListener("input", updateLineCount);
elements.severityFilter.addEventListener("change", () => renderFindings(currentFindings));
elements.categoryFilter.addEventListener("change", () => renderFindings(currentFindings));
historyElements.dashboardNav.addEventListener("click", () => showPage("dashboard"));
historyElements.historyNav.addEventListener("click", () => showPage("history"));
historyElements.refreshButton.addEventListener("click", loadHistory);
historyElements.exportButton.addEventListener("click", exportHistoryCsv);
historyElements.body.addEventListener("click", event => {
	const button = event.target.closest("button[data-analysis-id]");
	if (button) {
		showAnalysisSummary(button.dataset.analysisId);
	}
});
historyElements.closeDialog.addEventListener("click", () => historyElements.dialog.close());
historyElements.dialog.addEventListener("click", event => {
	if (event.target === historyElements.dialog) {
		historyElements.dialog.close();
	}
});

updateLineCount();
