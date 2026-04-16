package com.icoder.submission.management.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.coding.editor.utils.LanguageMatcher;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.repository.ContestProblemRelationRepository;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.repository.ContestUserRelationRepository;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.submission.management.dto.LanguageOptionResponse;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.entity.UserJudgeSession;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.repository.UserJudgeSessionRepository;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

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
    private final UserJudgeSessionRepository sessionRepository;
    private final ContestRepository contestRepository;
    private final ContestUserRelationRepository contestUserRelationRepository;
    private final ContestProblemRelationRepository contestProblemRelationRepository;

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
                addCookiesFromMaps(context, cookieMaps);
                log.info("Cookies loaded successfully for: {}", account.getUsername());
            } catch (Exception e) {
                log.error("Detailed Cookie Loading Error: {}", e.getMessage());
            }
        }
    }

    /**
     * Restores cookies in the same JSON shape Playwright stores on {@link BotAccount} (user session flow).
     */
    public void loadCookiesFromJson(BrowserContext context, String cookiesJson) {
        if (cookiesJson == null || cookiesJson.isBlank()) {
            return;
        }
        try {
            List<Map<String, Object>> cookieMaps = mapper.readValue(cookiesJson, new TypeReference<>() {
            });
            addCookiesFromMaps(context, cookieMaps);
            log.info("Loaded {} cookies from judge session JSON", cookieMaps.size());
        } catch (Exception e) {
            log.error("Failed to parse session cookies JSON: {}", e.getMessage());
        }
    }

    private void addCookiesFromMaps(BrowserContext context, List<Map<String, Object>> cookieMaps) {
        for (Map<String, Object> map : cookieMaps) {
            context.addCookies(List.of(cookieFromMap(map)));
        }
    }

    private Cookie cookieFromMap(Map<String, Object> map) {
        Cookie c = new Cookie((String) map.get("name"), (String) map.get("value"));
        if (map.get("domain") != null) {
            c.setDomain((String) map.get("domain"));
        }
        if (map.get("path") != null) {
            c.setPath((String) map.get("path"));
        }
        if (map.get("expires") instanceof Number n) {
            c.setExpires(n.doubleValue());
        }
        if (map.get("httpOnly") instanceof Boolean b) {
            c.setHttpOnly(b);
        }
        if (map.get("secure") instanceof Boolean b) {
            c.setSecure(b);
        }
        if (map.get("sameSite") instanceof String s) {
            try {
                c.setSameSite(com.microsoft.playwright.options.SameSiteAttribute.valueOf(s));
            } catch (Exception ex) {
                c.setSameSite(com.microsoft.playwright.options.SameSiteAttribute.LAX);
            }
        }
        return c;
    }

    public String resolveCodeforcesProgramTypeId(String language) {
        if (language == null || language.isBlank()) {
            return "54";
        }
        String trimmed = language.trim();
        if (trimmed.matches("\\d+")) {
            return trimmed;
        }
        return getCodeforcesLanguages().stream()
                .filter(opt -> opt.getDisplayName().equalsIgnoreCase(trimmed) || opt.getId().equalsIgnoreCase(trimmed))
                .map(LanguageOptionResponse::getId)
                .findFirst()
                .orElse("54");
    }

    public String resolveAtCoderLanguageId(String language) {
        if (language == null || language.isBlank()) {
            return "6082";
        }
        String trimmed = language.trim();
        if (trimmed.matches("\\d+")) {
            return trimmed;
        }
        return getAtCoderLanguages().stream()
                .filter(opt -> opt.getDisplayName().equalsIgnoreCase(trimmed) || opt.getId().equalsIgnoreCase(trimmed))
                .map(LanguageOptionResponse::getId)
                .findFirst()
                .orElse("6082");
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

    public Contest validateSubmissionWithinContest(SubmissionCreateRequest request, User currentUser, Problem problem) {
        if (request.getContestId() != null) {
            Contest contest = contestRepository.findById(request.getContestId())
                    .orElseThrow(() -> new ResourceNotFoundException("Contest not found"));

            boolean isParticipant = contestUserRelationRepository
                    .findByContestIdAndUserId(contest.getId(), currentUser.getId())
                    .isPresent();
            if (!isParticipant) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "User is not participating in this contest");
            }

            if (contest.getContestStatus() == ContestStatus.SCHEDULED)
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Contest is not yet started");
            boolean isProblemInContest = contestProblemRelationRepository
                    .existsByContestIdAndProblemId(contest.getId(), problem.getId());

            if (!isProblemInContest) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Problem does not belong to this contest");
            }
            return contest;
        }
        return null;
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

    public List<LanguageOptionResponse> getCsesLanguages() {
        List<String> csesOptions = List.of(
                "Assembly", "C++11", "C++17", "C++20",
                "CPython2", "CPython3", "Haskell", "Java",
                "Node.js", "Pascal", "PyPy2", "PyPy3",
                "Ruby", "Rust", "Scala"
        );

        return csesOptions.stream()
                .filter(lang -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(lang)))
                .map(lang -> LanguageOptionResponse.builder()
                        .id(lang)
                        .displayName(lang)
                        .build())
                .toList();
    }

    public List<LanguageOptionResponse> getCodeforcesLanguages() {
        Map<Integer, String> cfRawData = Map.ofEntries(
                Map.entry(79, "C# 10, .NET SDK 6.0"), Map.entry(96, "C# 13, .NET SDK 9"),
                Map.entry(65, "C# 8, .NET Core 3.1"), Map.entry(9, "C# Mono 6.8"),
                Map.entry(54, "GNU G++17 7.3.0"), Map.entry(89, "GNU G++20 13.2 (64 bit, winlibs)"),
                Map.entry(91, "GNU G++23 14.2 (64 bit, msys2)"), Map.entry(43, "GNU GCC C11 5.1.0"),
                Map.entry(87, "Java 21 64bit"), Map.entry(36, "Java 8 32bit"),
                Map.entry(83, "Kotlin 1.7.20"), Map.entry(88, "Kotlin 1.9.21"),
                Map.entry(99, "Kotlin 2.2.0"), Map.entry(20, "Scala 2.12.8"),
                Map.entry(7, "Python 2.7.18"), Map.entry(31, "Python 3.13.2"),
                Map.entry(40, "PyPy 2.7.13 (7.3.0)"), Map.entry(41, "PyPy 3.6.9 (7.3.0)"),
                Map.entry(70, "PyPy 3.10 (7.3.15, 64bit)"), Map.entry(34, "JavaScript V8 4.8.0"),
                Map.entry(55, "Node.js 15.8.0 (64bit)"), Map.entry(75, "Rust 1.89.0 (2021)"),
                Map.entry(98, "Rust 1.89.0 (2024)"), Map.entry(32, "Go 1.22.2"),
                Map.entry(6, "PHP 8.1.7"), Map.entry(67, "Ruby 3.2.2"),
                Map.entry(4, "Free Pascal 3.2.2"), Map.entry(51, "PascalABC.NET 3.8.3"),
                Map.entry(3, "Delphi 7"), Map.entry(12, "Haskell GHC 8.10.1"),
                Map.entry(28, "D DMD32 v2.105.0"), Map.entry(97, "F# 9, .NET SDK 9"),
                Map.entry(19, "OCaml 4.02.1"), Map.entry(13, "Perl 5.20.1")
        );

        return cfRawData.entrySet().stream()
                .filter(entry -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(entry.getValue())))
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> LanguageOptionResponse.builder()
                        .id(entry.getKey().toString())
                        .displayName(entry.getValue())
                        .build())
                .toList();
    }

    public List<LanguageOptionResponse> getAtCoderLanguages() {
        Map<Integer, String> atCoderRawData = Map.ofEntries(
                Map.entry(6017, "C++23 (GCC 15.2.0)"), Map.entry(6116, "C++23 (Clang 21.1.0)"),
                Map.entry(6014, "C23 (GCC 14.2.0)"), Map.entry(6054, "C++ IOI-Style (GCC 14.2.0)"),
                Map.entry(6056, "Java 24 (OpenJDK 24.0.2)"), Map.entry(6062, "Kotlin (Kotlin/JVM 2.2.10)"),
                Map.entry(6090, "Scala (Dotty 3.7.2)"), Map.entry(6082, "Python (CPython 3.13.7)"),
                Map.entry(6083, "Python (PyPy 3.11-v7.3.20)"), Map.entry(6059, "JavaScript (Node.js 22.19.0)"),
                Map.entry(6102, "TypeScript 5.9 (Node.js 22.19.0)"), Map.entry(6015, "C# 13.0 (.NET 9.0.8)"),
                Map.entry(6088, "Rust (rustc 1.89.0)"), Map.entry(6051, "Go (go 1.25.1)"),
                Map.entry(6077, "PHP (PHP 8.4.12)"), Map.entry(6087, "Ruby 3.4 (ruby 3.4.5)"),
                Map.entry(6095, "Swift 6.2")
        );

        return atCoderRawData.entrySet().stream()
                .filter(entry -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(entry.getValue())))
                .map(entry -> LanguageOptionResponse.builder()
                        .id(entry.getKey().toString())
                        .displayName(entry.getValue())
                        .build())
                .toList();
    }

    public void applyUserSession(BrowserContext browserContext, String sessionId, String domain) {
        String cookieValue = sessionId.contains("=") ? sessionId.split("=")[1] : sessionId;

        Cookie cookie = new Cookie("PHPSESSID", cookieValue)
                .setDomain(domain)
                .setPath("/");

        browserContext.addCookies(List.of(cookie));
        log.info("User session cookie applied for domain: {}", domain);
    }

    public String getUserSession(User user, OJudgeType judgeType) {
        return sessionRepository.findByUserAndJudgeType(user, judgeType)
                .map(UserJudgeSession::getSessionData)
                .orElse(null);
    }
}
