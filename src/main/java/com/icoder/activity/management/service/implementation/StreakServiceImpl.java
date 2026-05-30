package com.icoder.activity.management.service.implementation;

import com.icoder.activity.management.dto.StreakResponse;
import com.icoder.activity.management.repository.ActivityLogRepository;
import com.icoder.activity.management.service.interfaces.StreakService;
import com.icoder.core.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakServiceImpl implements StreakService {
    private final ActivityLogRepository activityLogRepository;
    private final SecurityUtils securityUtils;

    static int computeCurrentStreak(Set<LocalDate> acceptedDays, LocalDate todayUtc) {
        int streak = 0;
        LocalDate d = todayUtc;
        while (acceptedDays.contains(d)) {
            streak++;
            d = d.minusDays(1);
        }
        return streak;
    }

    static int computeMaxStreak(Set<LocalDate> acceptedDays) {
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

    @Transactional(readOnly = true)
    @Override
    public StreakResponse getUserStreak(String timezone) {
        Long userId = securityUtils.getCurrentUserId();
        ZoneId zoneId = ZoneId.of(timezone);
        LocalDate today = LocalDate.now(zoneId);

        List<Instant> activityInstants = activityLogRepository.findAllActivityInstantsByUserId(userId);

        Set<LocalDate> activityDates = activityInstants.stream()
                .map(instant -> instant.atZone(zoneId).toLocalDate())
                .collect(Collectors.toSet());

        int currentStreak = computeCurrentStreak(activityDates, today);
        int maxStreak = computeMaxStreak(activityDates);

        return StreakResponse.builder()
                .currentStreak(currentStreak)
                .maxStreak(maxStreak)
                .build();
    }
}