package com.icoder.contest.management.service.interfaces;

import com.icoder.contest.management.dto.LeaderboardRowResponse;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface LeaderboardService {
    List<LeaderboardRowResponse> getLeaderboard(Long contestId);

    SseEmitter subscribeToLeaderboard(Long contestId);

    @Scheduled(fixedRate = 30000)
    void broadcastLeaderboards();
}
