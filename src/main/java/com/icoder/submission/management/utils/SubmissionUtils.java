package com.icoder.submission.management.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionUtils {
    private final SubmissionRepository submissionRepository;
    private final BotAccountRepository accountRepository;
    private final ObjectMapper mapper = new ObjectMapper();
    private final ProblemUserRelationRepository relationRepository;
    private final ProblemRepository problemRepository;
    private final UserRepository userRepository;

    public void handleFailure(Long id) {
        submissionRepository.findById(id).ifPresent(s -> {
            s.setStatus(SubmissionStatus.FAILED);
            s.setVerdict(SubmissionVerdict.FAILED);
            submissionRepository.save(s);
        });
    }

    public void loadCookies(BrowserContext context, BotAccount account) {
        if (account.getCookies() != null && !account.getCookies().isBlank()) {
            try {
                List<Map<String, Object>> cookieMaps = mapper.readValue(account.getCookies(), new TypeReference<>() {
                });

                for (Map<String, Object> map : cookieMaps) {
                    context.addCookies(List.of(new Cookie((String) map.get("name"), (String) map.get("value"))
                            .setDomain((String) map.get("domain"))
                            .setPath((String) map.get("path"))
                            .setExpires(((Number) map.get("expires")).doubleValue())
                            .setHttpOnly((Boolean) map.get("httpOnly"))
                            .setSecure((Boolean) map.get("secure"))
                            .setSameSite(com.microsoft.playwright.options.SameSiteAttribute.valueOf((String) map.get("sameSite")))
                    ));
                }
                log.info("Cookies loaded successfully for: {}", account.getUsername());
            } catch (Exception e) {
                log.error("Detailed Cookie Loading Error: {}", e.getMessage());
            }
        }
    }

    @Transactional
    public void saveCookies(BrowserContext context, BotAccount account) {
        try {
            List<Cookie> cookies = context.cookies();
            String cookiesJson = mapper.writeValueAsString(cookies);
            account.setCookies(cookiesJson);
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("Failed to extract cookies: {}", e.getMessage());
        }
    }

    public Integer parseTime(String timeText) {
        if (timeText.contains("--") || timeText.isEmpty()) return 0;
        double seconds = Double.parseDouble(timeText.replace("s", "").trim());
        return (int) (seconds * 1000);
    }

    public Integer parseMemory(String memText) {
        if (memText.contains("--") || memText.isEmpty()) return 0;
        double mb = Double.parseDouble(memText.replace("MB", "").trim());
        return (int) mb;
    }

    public boolean isFinalVerdict(SubmissionVerdict v) {
        return v != SubmissionVerdict.IN_QUEUE && v != SubmissionVerdict.PENDING && v != SubmissionVerdict.RUNNING;
    }

    @Transactional
    public void updateUserProblemRelation(User user, Problem problem) {
        boolean exists = relationRepository.existsByUserIdAndProblemId(user.getId(), problem.getId());

        if (!exists) {
            ProblemUserRelation relation = new ProblemUserRelation();
            relation.setUser(user);
            relation.setProblem(problem);
            relation.setAttempted(true);
            relationRepository.saveAndFlush(relation);

            problem.setAttemptedCount(problem.getAttemptedCount() + 1);
            problemRepository.saveAndFlush(problem);
        }
    }

    @Transactional
    public void updateRelationAsSolved(Submission submission) {
        Long userId = submission.getUser().getId();
        Long problemId = submission.getProblem().getId();

        log.info("Updating relation for User ID: {} and Problem ID: {}", userId, problemId);

        ProblemUserRelation relation = relationRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseGet(() -> {
                    log.info("No existing relation found, creating new one for user {} and problem {}", userId, problemId);
                    ProblemUserRelation newRelation = new ProblemUserRelation();
                    newRelation.setUser(userRepository.getReferenceById(userId));
                    newRelation.setProblem(problemRepository.getReferenceById(problemId));
                    return newRelation;
                });

        if (!relation.isSolved()) {
            relation.setSolved(true);
            relationRepository.saveAndFlush(relation);

            problemRepository.findById(problemId).ifPresent(problem -> {
                problem.setSolvedCount(problem.getSolvedCount() + 1);
                problemRepository.saveAndFlush(problem);
                log.info("Problem {} (ID: {}) solved count incremented to {}",
                        problem.getProblemCode(), problemId, problem.getSolvedCount());
            });
        } else {
            log.info("Problem already marked as solved for this user.");
        }
    }

    public String extractRemoteId(String url) {
        String[] parts = url.split("/");
        return parts[parts.length - 1];
    }

    public String mapLanguageToCfId(String submissionLanguage) {
        if (submissionLanguage == null) return "54";

        String lang = submissionLanguage.toLowerCase();

        if (lang.contains("cpp") || lang.contains("c++")) {
            return "91";
        }

        if (lang.contains("python")) {
            return "31";
        }

        if (lang.contains("java")) {
            return "87";
        }

        if (lang.contains("csharp") || lang.contains("c#")) {
            return "96";
        }

        if (lang.contains("javascript") || lang.contains("node")) {
            return "55";
        }

        if (lang.contains("rust")) {
            return "98";
        }

        if (lang.contains("go")) {
            return "32";
        }

        if (lang.contains("php")) {
            return "6";
        }

        if (lang.equals("c")) {
            return "43";
        }

        return "54";
    }
}
