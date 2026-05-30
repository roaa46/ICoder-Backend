package com.icoder.activity.management.service.interfaces;

import com.icoder.activity.management.dto.StreakResponse;

public interface StreakService {

    StreakResponse getUserStreak(String timezone);
}
