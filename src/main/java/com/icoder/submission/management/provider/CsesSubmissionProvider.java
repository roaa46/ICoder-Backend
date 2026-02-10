package com.icoder.submission.management.provider;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.core.config.PlaywrightService;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.dto.SubmissionResult;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CsesSubmissionProvider implements OnlineJudgeSubmissionProvider {
    private final PlaywrightService playwrightService;
    private final BotAccountRepository accountRepository;

    private static final String BASE_URL = "https://cses.fi/problemset/";

    @Override
    public boolean supports(OJudgeType type) {
        return type == OJudgeType.CSES;
    }

    @Override
    public SubmissionResult submit(Submission submission, BotAccount account) {
        return playwrightService.execute(page -> {
            Path tempFile = null;
            try {
                loadCookies(page.context(), account);

                page.navigate("https://cses.fi/login");
                if (page.locator("input[name='nick']").isVisible()) {
                    page.locator("input[name='nick']").fill(account.getUsername());
                    page.locator("input[name='pass']").fill(account.getPassword());
                    page.click("input[type='submit']");
                    page.waitForCondition(() -> !page.url().contains("login"));
                    saveCookies(page.context(), account);
                }

                String submitUrl = "https://cses.fi/problemset/submit/" + submission.getProblem().getProblemCode() + "/";
                page.navigate(submitUrl);

                if (page.url().contains("login") || page.locator("input[name='nick']").isVisible()) {
                    return new SubmissionResult(null, SubmissionVerdict.FAILED, "Login failed or session expired");
                }

                String langValue = "C++";
                String optionValue = "C++11";

                if (submission.getLanguage().contains("C++")) {
                    langValue = "C++";
                    optionValue = submission.getLanguage();
                } else if (submission.getLanguage().contains("Python")) {
                    langValue = "Python3";
                    optionValue = "CPython3";
                }

                String extension = getExtension(langValue);
                tempFile = Files.createTempFile("submission-", extension);
                Files.writeString(tempFile, submission.getSubmissionCode());

                page.setInputFiles("input[name='file']", tempFile);
                page.selectOption("select[name='lang']", langValue);

                if (page.locator("select[name='option']").isVisible()) {
                    page.selectOption("select[name='option']", optionValue);
                }

                page.click("input[type='submit']");
                page.waitForURL("**/result/**");

                saveCookies(page.context(), account);
                return new SubmissionResult(extractRemoteId(page.url()), SubmissionVerdict.IN_QUEUE, null);

            } catch (Exception e) {
                page.screenshot(new Page.ScreenshotOptions().setPath(java.nio.file.Paths.get("debug-error.png")));
                log.error("CSES Submission failed: {}", e.getMessage());
                return new SubmissionResult(null, SubmissionVerdict.FAILED, e.getMessage());
            } finally {
                if (tempFile != null) {
                    try {
                        Files.deleteIfExists(tempFile);
                    } catch (Exception ignored) {
                    }
                }
            }
        });
    }

    @Override
    public SubmissionResult checkVerdict(String remoteRunId, BotAccount account) {
        return playwrightService.execute(page -> {
            try {
                loadCookies(page.context(), account);
                page.navigate(BASE_URL + "result/" + remoteRunId + "/");

                page.waitForSelector(".verdict", new Page.WaitForSelectorOptions().setTimeout(5000));

                String verdictText = page.locator(".verdict").textContent().trim().toUpperCase();
                log.info("CSES Raw Verdict for {}: {}", remoteRunId, verdictText);

                SubmissionVerdict verdict = mapCsesVerdict(verdictText);

                return new SubmissionResult(remoteRunId, verdict, null);
            } catch (Exception e) {
                log.error("Error checking CSES verdict for {}: {}", remoteRunId, e.getMessage());
                return new SubmissionResult(remoteRunId, SubmissionVerdict.IN_QUEUE, e.getMessage());
            }
        });
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

            saveCookies(page.context(), account);
            log.info("New cookies saved for bot: {}", account.getUsername());
        } else {
            log.info("Already logged in via cookies for: {}", account.getUsername());
        }
    }

    private void loadCookies(BrowserContext context, BotAccount account) {
        if (account.getCookies() != null && !account.getCookies().isBlank()) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                List<Cookie> cookies = mapper.readValue(account.getCookies(), new TypeReference<List<Cookie>>() {
                });
                context.addCookies(cookies);
                log.info("Cookies loaded for bot: {}", account.getUsername());
            } catch (Exception e) {
                log.warn("Failed to load cookies for bot: {}", account.getUsername());
            }
        }
    }

    private void saveCookies(BrowserContext context, BotAccount account) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<Cookie> cookies = context.cookies();
            String cookiesJson = mapper.writeValueAsString(cookies);
            account.setCookies(cookiesJson);
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("Failed to extract cookies: {}", e.getMessage());
        }
    }

    private String extractRemoteId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    private String getExtension(String language) {
        if (language.contains("C++")) return ".cpp";
        if (language.contains("Java")) return ".java";
        if (language.contains("Python")) return ".py";
        return ".txt"; // Default
    }
}