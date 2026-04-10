package com.icoder.submission.management.provider;

import com.icoder.core.config.PlaywrightService;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionContext;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.LoadState;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class CodeforcesSubmissionProvider implements OnlineJudgeSubmissionProvider {

    private static final Pattern SUBMISSION_ID_IN_URL = Pattern.compile("/submission/(\\d+)");
    private static final int NAV_TIMEOUT_MS = 90_000;

    private final PlaywrightService playwrightService;
    private final SubmissionUtils submissionUtils;

    private static String extractSubmissionId(String url) {
        Matcher m = SUBMISSION_ID_IN_URL.matcher(url);
        return m.find() ? m.group(1) : null;
    }

    private static SubmissionVerdict mapCodeforcesVerdict(String text) {
        if (text == null) {
            return SubmissionVerdict.PENDING;
        }
        String u = text.toUpperCase();
        if (u.contains("ACCEPTED") || u.contains("ACCEPTED (OK)")) {
            return SubmissionVerdict.ACCEPTED;
        }
        if (u.contains("WRONG ANSWER")) {
            return SubmissionVerdict.WRONG_ANSWER;
        }
        if (u.contains("TIME LIMIT")) {
            return SubmissionVerdict.TIME_LIMIT_EXCEEDED;
        }
        if (u.contains("MEMORY LIMIT")) {
            return SubmissionVerdict.MEMORY_LIMIT_EXCEEDED;
        }
        if (u.contains("COMPILATION ERROR") || u.contains("COMPILER")) {
            return SubmissionVerdict.COMPILATION_ERROR;
        }
        if (u.contains("RUNTIME ERROR") || u.contains("RUNTIME")) {
            return SubmissionVerdict.RUNTIME_ERROR;
        }
        if (u.contains("JUDGING") || u.contains("IN QUEUE") || u.contains("PENDING") || u.contains("QUEUED")) {
            return SubmissionVerdict.IN_QUEUE;
        }
        if (u.contains("SKIPPED") || u.contains("CHALLENGED")) {
            return SubmissionVerdict.FAILED;
        }
        return SubmissionVerdict.PENDING;
    }

    @Override
    public boolean supports(OJudgeType type) {
        return type == OJudgeType.CODEFORCES || type == OJudgeType.GYM;
    }

    @Override
    public SubmissionResult submit(Submission submission, SubmissionContext context) {
        return playwrightService.execute(page -> {
            try {
                authenticate(page, submission, context);

                CfRouting routing = CfRouting.from(submission);
                String submitUrl = routing.submitUrl();
                page.navigate(submitUrl, new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);

                log.info("CF submit URL after navigate: {}", page.url());
                log.info("CF page title: {}", page.title());
                log.info("CF page content snippet: {}", page.content().substring(0, Math.min(2000, page.content().length())));

                if (page.url().contains("/enter")) {
                    return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, "Not logged in to Codeforces");
                }

                page.waitForSelector("select[name=submittedProblemIndex]",
                        new Page.WaitForSelectorOptions().setTimeout(30_000));

                page.selectOption("select[name=submittedProblemIndex]", routing.problemIndex());
                String programTypeId = submissionUtils.resolveCodeforcesProgramTypeId(submission.getLanguage());
                page.selectOption("select[name=programTypeId]", programTypeId);
                page.fill("textarea[name=source]", submission.getSubmissionCode());

                page.click("input[type=submit].submit");
                page.waitForURL("**/submission/**", new Page.WaitForURLOptions().setTimeout(NAV_TIMEOUT_MS));

                String remoteId = extractSubmissionId(page.url());
                if (remoteId == null) {
                    return new SubmissionResult(null, SubmissionVerdict.FAILED_TO_SUBMIT, null, null, "Could not read submission id from URL");
                }

                if (context.account() != null) {
                    submissionUtils.saveCookies(page.context(), context.account());
                }

                return new SubmissionResult(remoteId, SubmissionVerdict.IN_QUEUE, null, null, null);
            } catch (Exception e) {
                log.error("Codeforces submit failed: {}", e.getMessage());
                return new SubmissionResult(null, SubmissionVerdict.FAILED, null, null, e.getMessage());
            }
        });
    }

    @Override
    public SubmissionResult checkVerdict(String remoteRunId, Submission submission) {
        return playwrightService.execute(page -> {
            try {
                restoreAuth(page, submission);

                CfRouting routing = CfRouting.from(submission);
                String viewUrl = routing.submissionUrl(remoteRunId);
                page.navigate(viewUrl, new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
                page.waitForLoadState(LoadState.DOMCONTENTLOADED);

                page.waitForSelector(".submissionVerdict, .verdict-accepted, .verdict-rejected, .verdict-waiting",
                        new Page.WaitForSelectorOptions().setTimeout(25_000));

                String verdictText = page.locator(".submissionVerdict").count() > 0
                        ? page.locator(".submissionVerdict").first().innerText().trim()
                        : page.locator("[class*='verdict-']").first().innerText().trim();

                SubmissionVerdict verdict = mapCodeforcesVerdict(verdictText);

                Integer timeMs = null;
                Integer memMb = null;
                if (submissionUtils.isFinalVerdict(verdict)) {
                    try {
                        if (page.locator(".time-consumed-cell").count() > 0) {
                            String t = page.locator(".time-consumed-cell").first().innerText().trim();
                            timeMs = submissionUtils.parseTime(t);
                        }
                        if (page.locator(".memory-consumed-cell").count() > 0) {
                            String m = page.locator(".memory-consumed-cell").first().innerText().trim();
                            memMb = submissionUtils.parseMemory(m);
                        }
                    } catch (Exception ex) {
                        log.debug("CF time/memory parse skipped: {}", ex.getMessage());
                    }
                }

                return new SubmissionResult(remoteRunId, verdict, timeMs, memMb, null);
            } catch (Exception e) {
                log.error("Codeforces checkVerdict {}: {}", remoteRunId, e.getMessage());
                return new SubmissionResult(remoteRunId, SubmissionVerdict.IN_QUEUE, null, null, e.getMessage());
            }
        });
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
        page.navigate("https://codeforces.com/enter", new Page.NavigateOptions().setTimeout(NAV_TIMEOUT_MS));
        page.waitForLoadState(LoadState.DOMCONTENTLOADED);

        var handle = page.locator("#handleOrEmail, input[name=handleOrEmail]");
        if (!handle.isVisible()) {
            return;
        }

        log.info("Logging into Codeforces as bot {}", account.getUsername());
        handle.fill(account.getUsername());
        page.locator("#password, input[name=password]").fill(account.getPassword());
        page.locator("input[type=submit].submit, input.submit[type=submit]").first().click();
        page.waitForLoadState(LoadState.NETWORKIDLE);
        submissionUtils.saveCookies(page.context(), account);
    }

    private record CfRouting(int contestId, String problemIndex, boolean gym) {

        static CfRouting from(Submission submission) {
            String code = submission.getProblem().getProblemCode();
            int i = 0;
            while (i < code.length() && Character.isDigit(code.charAt(i))) {
                i++;
            }
            if (i == 0) {
                throw new IllegalArgumentException("Invalid Codeforces problem code: " + code);
            }
            int contestId = Integer.parseInt(code.substring(0, i));
            String index = code.substring(i);
            if (index.isEmpty()) {
                throw new IllegalArgumentException("Missing problem index in code: " + code);
            }
            boolean gym = submission.getOnlineJudge() == OJudgeType.GYM;
            return new CfRouting(contestId, index, gym);
        }

        String submitUrl() {
            String type = gym ? "gym" : "contest";
            return "https://codeforces.com/" + type + "/" + contestId + "/submit";
        }

        String submissionUrl(String remoteRunId) {
            String type = gym ? "gym" : "contest";
            return "https://codeforces.com/" + type + "/" + contestId + "/submission/" + remoteRunId;
        }
    }
}
