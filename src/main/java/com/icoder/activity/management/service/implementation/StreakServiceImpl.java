package com.icoder.activity.management.service.implementation;

import com.icoder.activity.management.dto.StreakResponse;
import com.icoder.activity.management.service.interfaces.StreakService;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements StreakService {

    private final SubmissionService submissionService;
    private final SecurityUtils securityUtils;

    @Override
    @Transactional(readOnly = true)
    public StreakResponse getMyStreak() {
        Long userId = securityUtils.getCurrentUserId();
        List<Date> sqlDates = submissionService.getDistinctAcceptedDates(userId);
        Set<LocalDate> acceptedDays = new HashSet<>();
        for (Date d : sqlDates) {
            acceptedDays.add(d.toLocalDate());
        }
        LocalDate todayUtc = LocalDate.now(ZoneOffset.UTC);
        int current = computeCurrentStreak(acceptedDays, todayUtc);
        int longest = computeLongestStreak(acceptedDays);
        Optional<Instant> lastAccepted = submissionService.getLastAcceptedDate(userId);
        return StreakResponse.builder()
                .currentStreakDays(current)
                .longestStreakDays(longest)
                .lastAcceptedAt(lastAccepted.orElse(null))
                .todayUtc(todayUtc.toString())
                .build();
    }
    static int computeCurrentStreak(Set<LocalDate> acceptedDays, LocalDate todayUtc) {
        int streak = 0;
        LocalDate d = todayUtc;
        while (acceptedDays.contains(d)) {
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }

    static int computeLongestStreak(Set<LocalDate> acceptedDays) {
        if (acceptedDays.isEmpty()) {
            return 0;
        }
        List<LocalDate> sorted = acceptedDays.stream().sorted().toList();
        int longest = 1;
        int run = 1;
        for (int i = 1; i < sorted.size(); i++) {
            if (sorted.get(i).toEpochDay() == sorted.get(i - 1).toEpochDay() + 1) {
                run++;
            } else {
                run = 1;
            }
            longest = Math.max(longest, run);
        }
        return longest;
    }
}