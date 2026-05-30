package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.LeaderboardRowResponse;
import com.icoder.contest.management.dto.ProblemResultDto;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.repository.ContestProblemRelationRepository;
import com.icoder.contest.management.repository.ContestUserRelationRepository;
import com.icoder.contest.management.service.interfaces.LeaderboardService;
import com.icoder.submission.management.dto.SubmissionSummary;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LeaderboardServiceImpl implements LeaderboardService {
    private final ContestUserRelationRepository userRelationRepository;
    private final ContestProblemRelationRepository problemRelationRepository;
    private final SubmissionRepository submissionRepository;

    private final Map<Long, CopyOnWriteArrayList<SseEmitter>> emitters = new ConcurrentHashMap<>();

    private final List<SubmissionVerdict> PENALTY_VERDICTS = List.of(
            SubmissionVerdict.WRONG_ANSWER,
            SubmissionVerdict.RUNTIME_ERROR,
            SubmissionVerdict.TIME_LIMIT_EXCEEDED,
            SubmissionVerdict.MEMORY_LIMIT_EXCEEDED
    );

    @Override
    public List<LeaderboardRowResponse> getLeaderboard(Long contestId) {
        List<ContestUserRelation> participants = userRelationRepository.findAllByContestIdOrderByRank(contestId);

        List<ContestProblemRelation> problems = problemRelationRepository.findByContestId(contestId);
        problems.sort(Comparator.comparing(ContestProblemRelation::getId));

        List<SubmissionSummary> allSubmissions =
                submissionRepository.findAllByContestIdOrderByCreatedAtAsc(contestId);

        Map<Long, Map<Long, List<SubmissionSummary>>> userProblemSubmissions =
                allSubmissions.stream()
                        .collect(Collectors.groupingBy(SubmissionSummary::getUserId,
                                Collectors.groupingBy(SubmissionSummary::getProblemId)));

        List<LeaderboardRowResponse> leaderboard = new ArrayList<>();
        int currentRank = 1;

        for (ContestUserRelation relation : participants) {
            Long userId = relation.getUser().getId();
            var userSubs = userProblemSubmissions.getOrDefault(userId, Collections.emptyMap());

            if (userSubs.isEmpty()) continue;

            Map<String, ProblemResultDto> problemResults = new HashMap<>();

            for (int i = 0; i < problems.size(); i++) {
                ContestProblemRelation probRel = problems.get(i);
                Long probId = probRel.getProblem().getId();

                String dynamicAlias = String.valueOf((char) ('A' + i));

                var subsForProblem = userSubs.getOrDefault(probId, Collections.emptyList());

                boolean solved = false;
                long solvedTime = 0;
                int wrongAttempts = 0;
                boolean isFirstAccepted = false;

                for (var sub : subsForProblem) {
                    if (PENALTY_VERDICTS.contains(sub.getVerdict())) {
                        wrongAttempts++;
                    } else if (sub.getVerdict() == SubmissionVerdict.ACCEPTED) {
                        solved = true;
                        solvedTime = Duration.between(probRel.getContest().getBeginTime(), sub.getCreatedAt()).toMinutes();

                        if (probRel.getFirstAcceptedSubmission() != null &&
                                probRel.getFirstAcceptedSubmission().getId().equals(sub.getId())) {
                            isFirstAccepted = true;
                        }
                        break;
                    }
                }

                if (solved || wrongAttempts > 0) {
                    problemResults.put(dynamicAlias, ProblemResultDto.builder()
                            .solved(solved)
                            .solvedTime(solvedTime)
                            .wrongAttempts(wrongAttempts)
                            .firstAccepted(isFirstAccepted)
                            .build());
                }
            }

            leaderboard.add(LeaderboardRowResponse.builder()
                    .rank(currentRank++)
                    .userId(userId)
                    .handle(relation.getUser().getHandle())
                    .totalScore(relation.getScore())
                    .totalPenalty(relation.getPenalty())
                    .problemResults(problemResults)
                    .build());
        }
        return leaderboard;
    }

    @Override
    public SseEmitter subscribeToLeaderboard(Long contestId) {
        SseEmitter emitter = new SseEmitter(60 * 60 * 1000L);
        emitters.computeIfAbsent(contestId, k -> new CopyOnWriteArrayList<>()).add(emitter);

        emitter.onCompletion(() -> removeEmitter(contestId, emitter));
        emitter.onTimeout(() -> removeEmitter(contestId, emitter));
        emitter.onError((e) -> removeEmitter(contestId, emitter));

        try {
            emitter.send(SseEmitter.event().name("leaderboard").data(getLeaderboard(contestId)));
        } catch (Exception e) {
            removeEmitter(contestId, emitter);
        }

        return emitter;
    }

    private void removeEmitter(Long contestId, SseEmitter emitter) {
        List<SseEmitter> list = emitters.get(contestId);
        if (list != null) {
            list.remove(emitter);
        }
    }

    @Scheduled(fixedRate = 30000)
    @Override
    public void broadcastLeaderboards() {
        emitters.forEach((contestId, emitterList) -> {
            if (!emitterList.isEmpty()) {
                List<LeaderboardRowResponse> leaderboard = getLeaderboard(contestId);
                List<SseEmitter> deadEmitters = new ArrayList<>();

                for (SseEmitter emitter : emitterList) {
                    try {
                        emitter.send(SseEmitter.event().name("leaderboard").data(leaderboard));
                    } catch (Exception e) {
                        deadEmitters.add(emitter);
                    }
                }
                emitterList.removeAll(deadEmitters);
            }
        });
    }
}
