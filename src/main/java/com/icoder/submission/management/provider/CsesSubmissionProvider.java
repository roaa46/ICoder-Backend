package com.icoder.submission.management.provider;

import com.icoder.core.config.PlaywrightService;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsesSubmissionProvider implements OnlineJudgeSubmissionProvider {
    private final PlaywrightService playwrightService;
    private final SubmissionUtils submissionUtils;

    private static final String BASE_URL = "https://cses.fi/problemset/";
    private static final String RESULT_ROW_SELECTOR = "tr:has-text('Result:') span.verdict";
    private static final String SUBMISSION_ROWS_SELECTOR = "table.wide tbody tr";

    @Override
    public boolean supports(OJudgeType type) {
        return type == OJudgeType.CSES;
    }

    @Override
    public SubmissionResult submit(Submission submission, BotAccount account) {
        return playwrightService.execute(page -> {
            Path tempFile = null;
            try {
                handleAuthentication(page, account);

                String[] langSettings = determineLanguageSettings(submission.getLanguage());
                tempFile = createTempSubmissionFile(submission.getSubmissionCode(), langSettings[0]);

                String submitUrl = BASE_URL + "submit/" + submission.getProblem().getProblemCode() + "/";
                return performUpload(page, submitUrl, tempFile, langSettings, account);
            } catch (Exception e) {
                takeErrorScreenshot(page);
                return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, e.getMessage());
            } finally {
                cleanupTempFile(tempFile);
            }
        });
    }

    @Override
    public SubmissionResult checkVerdict(String remoteRunId, BotAccount account, Submission submission) {
        return playwrightService.execute(page -> {
            try {
                submissionUtils.loadCookies(page.context(), account);
                page.navigate(BASE_URL + "result/" + remoteRunId + "/");

                page.waitForSelector(
                        RESULT_ROW_SELECTOR,
                        new Page.WaitForSelectorOptions().setTimeout(10000)
                );

                String verdictText = page.locator(
                        RESULT_ROW_SELECTOR
                ).textContent().trim().toUpperCase();

                log.debug("CSES Checker: RemoteID {} - Highly accurate verdict found: {}", remoteRunId, verdictText);

                SubmissionVerdict verdict = mapCsesVerdict(verdictText);

                Integer timeUsage = null;

                if (submissionUtils.isFinalVerdict(verdict)) {
                    try {
                        timeUsage = fetchExecutionTime(page, submission.getProblem().getProblemCode(), submission);
                    } catch (Exception e) {
                        log.warn("Could not parse time/memory for submission {}: {}", remoteRunId, e.getMessage());
                    }
                }
                return new SubmissionResult(remoteRunId, verdict, timeUsage, null, null);
            } catch (Exception e) {
                log.error("CSES Checker Error for {}: {}", remoteRunId, e.getMessage());
                return new SubmissionResult(remoteRunId, SubmissionVerdict.IN_QUEUE, null, null, e.getMessage());
            }
        });
    }

    private Integer fetchExecutionTime(Page page, String problemCode, Submission submission) {
        page.navigate(BASE_URL + "view/" + problemCode + "/");

        page.waitForSelector("table.wide tbody tr a[href*='" + submission.getRemoteRunId() + "']");

        Locator row = page.locator(
                SUBMISSION_ROWS_SELECTOR + ":has(a[href*='" + submission.getRemoteRunId() + "'])"
        );

        if (row.count() > 0) {
            String timeText = row.first().locator("td").nth(2).textContent().trim();
            return submissionUtils.parseTime(timeText);
        }

        return null;
    }


    private SubmissionVerdict mapCsesVerdict(String text) {
        if (text.contains("READY") || text.contains("ACCEPTED")) return SubmissionVerdict.ACCEPTED;
        if (text.contains("WRONG ANSWER")) return SubmissionVerdict.WRONG_ANSWER;
        if (text.contains("TIME LIMIT EXCEEDED")) return SubmissionVerdict.TIME_LIMIT_EXCEEDED;
        if (text.contains("MEMORY LIMIT EXCEEDED")) return SubmissionVerdict.MEMORY_LIMIT_EXCEEDED;
        if (text.contains("COMPILE ERROR")) return SubmissionVerdict.COMPILATION_ERROR;
        if (text.contains("RUNTIME ERROR")) return SubmissionVerdict.RUNTIME_ERROR;
        if (text.contains("PENDING") || text.contains("TESTING") || text.isEmpty())
            return SubmissionVerdict.IN_QUEUE;

        return SubmissionVerdict.PENDING;
    }

    private void ensureLoggedIn(Page page, BotAccount account) {
        page.navigate("https://cses.fi/login");

        if (page.locator("input[name='nick']").isVisible()) {
            log.info("Performing manual login for: {}", account.getUsername());
            page.locator("input[name='nick']").fill(account.getUsername());
            page.locator("input[name='pass']").fill(account.getPassword());

            page.waitForNavigation(() -> page.locator("input[type='submit']").click());

            submissionUtils.saveCookies(page.context(), account);
            log.info("New cookies saved for bot: {}", account.getUsername());
        } else {
            log.info("Already logged in via cookies for: {}", account.getUsername());
        }
    }

    private SubmissionResult performUpload(Page page, String url, Path file, String[] settings, BotAccount account) {
        page.navigate(url);

        if (page.url().contains("login")) {
            return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, "Session expired after navigation");
        }

        page.setInputFiles("input[name='file']", file);
        page.selectOption("select[name='lang']", settings[0]); // langValue

        if (page.locator("select[name='option']").isVisible()) {
            page.selectOption("select[name='option']", settings[1]); // optionValue
        }

        page.click("input[type='submit']");
        page.waitForURL("**/result/**");

        submissionUtils.saveCookies(page.context(), account);
        return new SubmissionResult(submissionUtils.extractRemoteId(page.url()), SubmissionVerdict.IN_QUEUE, null, null, null);
    }

    private String[] determineLanguageSettings(String lang) {
        // [langValue, optionValue]
        if (lang.contains("C++")) return new String[]{"C++", lang};
        if (lang.contains("Python")) return new String[]{"Python3", "CPython3"};
        return new String[]{"C++", "C++11"}; // Default
    }

    private Path createTempSubmissionFile(String code, String lang) throws Exception {
        String ext = lang.contains("Python") ? ".py" : ".cpp";
        Path path = Files.createTempFile("sub-", ext);
        Files.writeString(path, code);
        return path;
    }

    private void cleanupTempFile(Path path) {
        if (path != null) {
            try {
                Files.deleteIfExists(path);
            } catch (Exception ignored) {
            }
        }
    }

    private void takeErrorScreenshot(Page page) {
        try {
            page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("debug-error.png")));
        } catch (Exception ignored) {
        }
    }

    private void handleAuthentication(Page page, BotAccount account) {
        submissionUtils.loadCookies(page.context(), account);
        ensureLoggedIn(page, account);
    }
}