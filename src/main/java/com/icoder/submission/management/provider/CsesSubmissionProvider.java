package com.icoder.submission.management.provider;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.OnlineJudgeAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.microsoft.playwright.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CsesSubmissionProvider implements OnlineJudgeSubmissionProvider {

    private static final String BASE_URL = "https://cses.fi";

    @Override
    public Submission submit(Submission submission, OnlineJudgeAccount account) {
        // بنفتح Playwright Browser (تقدر تخليه Singleton لو عايز توفر ريسورسز)
        try (Playwright playwright = Playwright.create()) {
            Browser browser = playwright.chromium().launch(new BrowserType.LaunchOptions().setHeadless(true));
            BrowserContext context = browser.newContext();
            Page page = context.newPage();

            try {
                // 1. Login
                log.info("Logging into CSES via Playwright...");
                page.navigate(BASE_URL + "/login");
                page.fill("input[name='nick']", account.getUsername());
                page.fill("input[name='pass']", account.getPassword());
                page.click("input[type='submit']");

                // 2. Navigate to Submit Page
                String submitUrl = BASE_URL + "/problemset/submit/" + submission.getProblem().getRemoteProblemId() + "/";
                page.navigate(submitUrl);

                // 3. Upload Code & Submit
                // ملاحظة: CSES ساعات بيستخدم dropdown للغة، Playwright بيسهل اختيارها
                page.selectOption("select[name='lang']", submission.getLanguage());
                page.fill("textarea[name='code']", submission.getSubmissionCode());
                page.click("input[type='submit']");

                // 4. الانتظار حتى التوجيه لصفحة النتائج
                page.waitForLoadState(LoadState.NETWORKIDLE);
                String resultUrl = page.url();
                submission.setRemoteRunId(resultUrl.substring(resultUrl.lastIndexOf("/") + 1));

                // 5. Polling باستخدام ميزات Playwright (الانتظار الذكي)
                log.info("Waiting for Verdict...");

                // هنفضل نعمل reload لحد ما الـ verdict يظهر (أو نراقب الـ element)
                boolean isPending = true;
                int attempts = 0;
                while (isPending && attempts < 30) { // Timeout بعد 60 ثانية تقريباً
                    String verdictText = page.innerText(".verdict"); // الـ Selector ده حسب CSES HTML

                    if (isValidVerdict(verdictText)) {
                        submission.setVerdict(parseVerdict(verdictText));
                        submission.setTimeUsage(parseTime(page.innerText(".time")));
                        submission.setMemoryUsage(parseMemory(page.innerText(".memory")));
                        submission.setStatus(SubmissionStatus.COMPLETED);
                        isPending = false;
                    } else {
                        page.waitForTimeout(2000); // استنى ثانيتين
                        page.reload();
                        attempts++;
                    }
                }

                return submission;

            } catch (Exception e) {
                log.error("Playwright execution error: ", e);
                submission.setStatus(SubmissionStatus.FAILED);
                return submission;
            } finally {
                browser.close();
            }
        }
    }

    private boolean isValidVerdict(String v) {
        return v != null && !v.isEmpty() && !v.contains("PENDING") && !v.contains("WAITING");
    }

    private SubmissionVerdict parseVerdict(String text) {
        if (text.contains("ACCEPTED")) return SubmissionVerdict.ACCEPTED;
        if (text.contains("WRONG ANSWER")) return SubmissionVerdict.WRONG_ANSWER;
        // ... كملي باقي الاحتمالات
        return SubmissionVerdict.OTHER;
    }

    @Override
    public boolean supports(OJudgeType type) {
        return type == OJudgeType.CSES;
    }
}