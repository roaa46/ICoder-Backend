package com.icoder.summary.management.service;

import com.icoder.problem.management.entity.ProblemProperty;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.summary.management.dto.UserStatsDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SummaryAggregationService {

    private final SubmissionRepository submissionRepository;

    @Transactional(readOnly = true)
    public UserStatsDto aggregateUserStats(Long userId) {

        List<Submission> submissions = submissionRepository
                .findCompletedSubmissionsByUserId(userId);

        if (submissions.isEmpty()) {
            throw new RuntimeException("No submission data found for user: " + userId);
        }

        Instant thirtyDaysAgo = Instant.now().minus(30, ChronoUnit.DAYS);

        // --- Counters ---
        long totalRecent = 0, acRecent = 0;
        Map<SubmissionVerdict, Integer> verdictCounts = new HashMap<>();
        Map<String, Integer> successfulTopics = new HashMap<>();
        Map<String, Integer> failedTopics    = new HashMap<>();
        Map<String, Integer> languageCounts  = new HashMap<>();

        for (Submission sub : submissions) {

            SubmissionVerdict verdict = sub.getVerdict();

            // 1. Tally verdicts
            verdictCounts.merge(verdict, 1, Integer::sum);

            // 2. Recent window (last 30 days)
            if (sub.getSubmittedAt() != null
                    && sub.getSubmittedAt().isAfter(thirtyDaysAgo)) {
                totalRecent++;
                if (verdict == SubmissionVerdict.ACCEPTED) acRecent++;
            }

            // 3. Language tracking
            if (sub.getLanguage() != null) {
                languageCounts.merge(sub.getLanguage(), 1, Integer::sum);
            }

            // 4. Topic extraction (handles missing tags gracefully)
            List<String> topics = extractTopics(sub);

            if (verdict == SubmissionVerdict.ACCEPTED) {
                topics.forEach(t -> successfulTopics.merge(t, 1, Integer::sum));
            } else {
                topics.forEach(t -> failedTopics.merge(t, 1, Integer::sum));
            }
        }

        // --- Calculate rates ---
        long totalSubs   = submissions.size();
        long totalAc     = verdictCounts.getOrDefault(SubmissionVerdict.ACCEPTED, 0);
        double overallAcRate = (double) totalAc / totalSubs * 100;
        double recentAcRate  = totalRecent > 0
                ? (double) acRecent / totalRecent * 100 : 0.0;

        // --- Most used language ---
        String mostUsedLang = languageCounts.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("Unknown");

        // --- Top 3 strengths & weaknesses ---
        List<String> strengths  = topEntries(successfulTopics, 3);
        List<String> weaknesses = topEntries(failedTopics, 3);

        return UserStatsDto.builder()
                .totalSubmissions(totalSubs)
                .overallAcRate(Math.round(overallAcRate * 10.0) / 10.0)
                .recentAcRate(Math.round(recentAcRate * 10.0) / 10.0)
                .tleCount(verdictCounts.getOrDefault(
                        SubmissionVerdict.TIME_LIMIT_EXCEEDED, 0))
                .rteCount(verdictCounts.getOrDefault(
                        SubmissionVerdict.RUNTIME_ERROR, 0))
                .mleCount(verdictCounts.getOrDefault(
                        SubmissionVerdict.MEMORY_LIMIT_EXCEEDED, 0))
                .compilationErrorCount(verdictCounts.getOrDefault(
                        SubmissionVerdict.COMPILATION_ERROR, 0))
                .strengths(strengths)
                .weaknesses(weaknesses)
                .mostUsedLanguage(mostUsedLang)
                .build();
    }

    // ---------------------------------------------------------------
    // Topic extraction — handles CF (has tags), CSES (no tags),
    // AtCoder (no tags), and infers difficulty from solvedCount
    // ---------------------------------------------------------------
    private List<String> extractTopics(Submission sub) {
        List<String> topics = new ArrayList<>();

        // Try to read real tags from ProblemProperty
        if (sub.getProblem() != null
                && sub.getProblem().getProperties() != null) {
            for (ProblemProperty prop : sub.getProblem().getProperties()) {
                String title = prop.getTitle();
                if (title != null &&
                        (title.equalsIgnoreCase("Tags")
                                || title.equalsIgnoreCase("Topic")
                                || title.equalsIgnoreCase("Category"))) {
                    if (prop.getContent() != null
                            && !prop.getContent().isBlank()) {
                        topics.add(prop.getContent().trim());
                    }
                }
            }
        }

        // Fallback: no tags found — use OJ-specific strategy
        if (topics.isEmpty() && sub.getProblem() != null) {
            OJudgeType oj = sub.getProblem().getOnlineJudge();

            if (oj == OJudgeType.CSES) {
                // CSES: group by contest section (e.g. "Sorting and Searching")
                String section = sub.getProblem().getContestTitle();
                topics.add(section != null ? "CSES: " + section : "CSES Problem");

            } else if (oj == OJudgeType.AT_CODER) {
                // AtCoder: group by contest title (e.g. "ABC300")
                String contest = sub.getProblem().getContestTitle();
                topics.add(contest != null ? "AtCoder: " + contest : "AtCoder Problem");

            } else {
                // Codeforces / GYM: infer difficulty from community solve count
                long solvedCount = sub.getProblem().getSolvedCount();
                if (solvedCount > 10_000) {
                    topics.add("Fundamentals");
                } else if (solvedCount > 2_000) {
                    topics.add("Intermediate Algorithms");
                } else {
                    topics.add("Advanced Logic");
                }
            }
        }

        return topics;
    }

    private List<String> topEntries(Map<String, Integer> map, int limit) {
        return map.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
