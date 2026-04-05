package com.icoder.submission.management.provider;

import com.icoder.core.config.PlaywrightService;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionContext;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import com.microsoft.playwright.options.WaitForSelectorOptions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class AtCoderSubmissionProvider implements OnlineJudgeSubmissionProvider {

    private static final Pattern SUBMISSION_ID_IN_URL = Pattern.compile("/submissions/(\\d+)");
    private static final int NAV_TIMEOUT_MS = 90_000;

    private final PlaywrightService playwrightService;
    private final SubmissionUtils submissionUtils;

    @Override
    public boolean supports(OJudgeType type) {
        return type == OJudgeType.AT_CODER;
    }

    @Override
    public SubmissionResult submit(Submission submission, SubmissionContext context) {
        return playwrightService.execute(page -> {
            try {
                authenticate(page, submission, context);

                String taskScreenName = submission.getProblem().getProblemCode();
                String contestId = contestIdFromTask(taskScreenName);
                String submitUrl = "https://atcoder.jp/contests/" + contestId + "/submit?taskScreenName=" + taskScreenName;

                page.navigate(submitUrl, new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);

                if (page.url().contains("/login")) {
                    return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, "Not logged in to AtCoder");
                }

                page.waitForSelector("textarea#sourceCode, textarea[name=sourceCode]",
                        new WaitForSelectorOptions().setTimeout(30_000));

                String langId = submissionUtils.resolveAtCoderLanguageId(submission.getLanguage());
                Locator langSelect = page.locator("select[name=\"data.LanguageId\"], select#select-lang").first();
                langSelect.selectOption(langId);

                page.locator("textarea#sourceCode, textarea[name=sourceCode]").first()
                        .fill(submission.getSubmissionCode());

                page.locator("button[type=submit].btn-primary, button:has-text(\"Submit\")").first().click();

                page.waitForURL("**/submissions/**", new Page.WaitForURLOptions().setTimeout(NAV_TIMEOUT_MS));

                String remoteId = extractSubmissionId(page.url());
                if (remoteId == null) {
                    return new SubmissionResult(null, SubmissionVerdict.FAILED_TO_SUBMIT, null, null, "Could not read AtCoder submission id from URL");
                }

                if (context.account() != null) {
                    submissionUtils.saveCookies(page.context(), context.account());
                }

                return new SubmissionResult(remoteId, SubmissionVerdict.IN_QUEUE, null, null, null);
            } catch (Exception e) {
                log.error("AtCoder submit failed: {}", e.getMessage());
                return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, e.getMessage());
            }
        });
    }

    @Override
    public SubmissionResult checkVerdict(String remoteRunId, Submission submission) {
        return playwrightService.execute(page -> {
            try {
                restoreAuth(page, submission);

                String contestId = contestIdFromTask(submission.getProblem().getProblemCode());
                String viewUrl = "https://atcoder.jp/contests/" + contestId + "/submissions/" + remoteRunId;

                page.navigate(viewUrl, new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);

                page.waitForSelector("span.label, td.submission-result span, .judge-status",
                        new WaitForSelectorOptions().setTimeout(25_000));

                String raw = page.locator("span.label").first().innerText().trim();
                SubmissionVerdict verdict = mapAtCoderVerdict(raw);

                Integer timeMs = null;
                Integer memMb = null;
                if (submissionUtils.isFinalVerdict(verdict)) {
                    try {
                        Locator row = page.locator("table tr:has-text(\"" + remoteRunId + "\")").first();
                        if (row.count() > 0) {
                            String cells = row.innerText();
                            for (String part : cells.split("\\s+")) {
                                if (part.endsWith("ms") && timeMs == null) {
                                    timeMs = parseMs(part);
                                }
                                if ((part.endsWith("KiB") || part.endsWith("MiB")) && memMb == null) {
                                    memMb = parseMemToMb(part);
                                }
                            }
                        }
                    } catch (Exception ex) {
                        log.debug("AtCoder time/memory parse skipped: {}", ex.getMessage());
                    }
                }

                return new SubmissionResult(remoteRunId, verdict, timeMs, memMb, null);
            } catch (Exception e) {
                log.error("AtCoder checkVerdict {}: {}", remoteRunId, e.getMessage());
                return new SubmissionResult(remoteRunId, SubmissionVerdict.IN_QUEUE, null, null, e.getMessage());
            }
        });
    }

    private static Integer parseMs(String part) {
        try {
            return Integer.parseInt(part.replace("ms", "").trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Integer parseMemToMb(String part) {
        try {
            part = part.trim();
            if (part.endsWith("MiB")) {
                return (int) Math.round(Double.parseDouble(part.replace("MiB", "").trim()));
            }
            if (part.endsWith("KiB")) {
                int kb = Integer.parseInt(part.replace("KiB", "").trim());
                return Math.max(1, kb / 1024);
            }
        } catch (NumberFormatException ignored) {
        }
        return null;
    }

    private void authenticate(Page page, Submission submission, SubmissionContext context) throws Exception {
        if (context.account() != null) {
            submissionUtils.loadCookies(page.context(), context.account());
            ensureLoggedIn(page, context.account());
        } else if (context.sessionId() != null && context.sessionId().trim().startsWith("[")) {
            submissionUtils.loadCookiesFromJson(page.context(), context.sessionId());
        }
    }

    private void restoreAuth(Page page, Submission submission) throws Exception {
        if (submission.getBotAccount() != null) {
            submissionUtils.loadCookies(page.context(), submission.getBotAccount());
            ensureLoggedIn(page, submission.getBotAccount());
        } else {
            String session = submissionUtils.getUserSession(submission.getUser(), submission.getOnlineJudge());
            if (session != null && session.trim().startsWith("[")) {
                submissionUtils.loadCookiesFromJson(page.context(), session);
            }
        }
    }

    private void ensureLoggedIn(Page page, BotAccount account) {
        page.navigate("https://atcoder.jp/login", new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        Locator user = page.locator("input[name=username], #username");
        if (!user.isVisible()) {
            return;
        }

        log.info("Logging into AtCoder as bot {}", account.getUsername());
        user.fill(account.getUsername());
        page.locator("input[name=password], #password").fill(account.getPassword());
        page.locator("button[type=submit]").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        submissionUtils.saveCookies(page.context(), account);
    }

    private static String contestIdFromTask(String taskScreenName) {
        int u = taskScreenName.lastIndexOf('_');
        if (u <= 0) {
            throw new IllegalArgumentException("Invalid AtCoder task screen name: " + taskScreenName);
        }
        return taskScreenName.substring(0, u);
    }

    private static String extractSubmissionId(String url) {
        Matcher m = SUBMISSION_ID_IN_URL.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private static SubmissionVerdict mapAtCoderVerdict(String code) {
        if (code == null) {
            return SubmissionVerdict.PENDING;
        }
        return switch (code.trim().toUpperCase()) {
            case "AC" -> SubmissionVerdict.ACCEPTED;
            case "WA" -> SubmissionVerdict.WRONG_ANSWER;
            case "CE" -> SubmissionVerdict.COMPILATION_ERROR;
            case "RE", "RTE" -> SubmissionVerdict.RUNTIME_ERROR;
            case "TLE" -> SubmissionVerdict.TIME_LIMIT_EXCEEDED;
            case "MLE" -> SubmissionVerdict.MEMORY_LIMIT_EXCEEDED;
            case "OLE" -> SubmissionVerdict.RUNTIME_ERROR;
            case "IE", "QLE" -> SubmissionVerdict.FAILED;
            case "WJ", "WR", "JUDGING" -> SubmissionVerdict.IN_QUEUE;
            default -> {
                String u = code.toUpperCase();
                if (u.contains("JUDG")) {
                    yield SubmissionVerdict.IN_QUEUE;
                }
                yield SubmissionVerdict.PENDING;
            }
        };
    }
}
