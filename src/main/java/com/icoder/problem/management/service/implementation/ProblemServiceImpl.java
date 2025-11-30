package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ProblemNotFoundException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.mapper.PropertyMapper;
import com.icoder.problem.management.mapper.SectionMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.scraping.service.ScrapingServiceImpl;
import com.icoder.problem.management.service.interfaces.ProblemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final PropertyMapper propertyMapper;
    private final SectionMapper sectionMapper;
    private final ScrapingServiceImpl scrapingService;
    private static final long HYBRID_TTL_DAYS = 7;

    ///  get problem metadata
    public ProblemResponse getProblemMetadata(String source, String code) {
        Optional<Problem> existingProblem = problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase()));

        if (existingProblem.isPresent() && existingProblem.get().getProblemTitle() != null) {
            return problemMapper.toResponseDTO(existingProblem.get());
        }

        ProblemResponse response = scrapingService.scrapMetaData(source, code);
        Problem newProblem = new Problem(
                response.getProblemCode(),
                OJudgeType.valueOf(source.toUpperCase()),
                response.getContestTitle(),
                response.getContestLink(),
                response.getProblemTitle(),
                response.getProblemLink()
        );
        problemRepository.save(newProblem);
        ProblemResponse problemResponse = problemMapper.toResponseDTO(newProblem);
        problemResponse.setOnlineJudge(source);

        return problemMapper.toResponseDTO(newProblem);
    }

    ///  get a problem statement
    public ProblemStatementResponse getProblemStatement(String source, String code) {
        Problem problem = getProblemFromCacheOrDb(source, code);
        if (problem.getFetchedAt() != null) {
            if (isEligibleForMigration(problem.getFetchedAt())) {
                log.info("problem exceeded 7 days, scrap again.");
                problem = scrapAndCacheFullStatement(source, code);
                putStableProblemToCache(problem);
                evictNewProblemCache(source, code);
            }
            log.info("problem doesn't exceed 7 days, get from cache.");
        }
        else {

            log.info("problem metadata exists but there's no problem statement. fetch and scrap for the first time");
            problem = scrapAndCacheFullStatement(source, code);
        }

        ProblemStatementResponse problemResponse = problemMapper.toStatementDTO(problem);
        problemResponse.setOnlineJudge(source);

        return problemResponse;
    }

    private Problem getProblemFromCacheOrDb(String source, String code) {
        Problem stable = getStableProblemFromCache(source, code);
        if (stable != null && stable.isCached()) return stable;

        Problem newest = getNewProblemFromCache(source, code);
        if (newest != null && newest.isCached()) return newest;

        return problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase()))
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found in DB or external judge"));
    }

    @Cacheable(value = "stableProblemsCache", key = "#source + '-' + #code")
    public Problem getStableProblemFromCache(String source, String code) {
        return problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase())).orElse(null);
    }

    @Cacheable(value = "newProblemsCache", key = "#source + '-' + #code")
    public Problem getNewProblemFromCache(String source, String code) {
        return problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase())).orElse(null);
    }

    /// scrap problem statement
    @CachePut(value = "newProblemsCache", key = "#source + '-' + #code")
    public Problem scrapAndCacheFullStatement(String source, String code) {
        ProblemStatementResponse scrapedResponse = scrapingService.scrapFullStatement(source, code);

        Problem problemToUpdate = problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase()))
                .orElseThrow(() -> new ProblemNotFoundException("Metadata not found for problem " + source + "-" + code));
        problemToUpdate.setProperties(propertyMapper.toListEntity(scrapedResponse.getProperties()));
        problemToUpdate.setSections(sectionMapper.toListEntity(scrapedResponse.getSections()));
        problemToUpdate.setAttemptedCount(scrapedResponse.getAttemptedCount());
        problemToUpdate.setFetchedAt(Instant.now());
        problemToUpdate.setCached(true);

        return problemRepository.save(problemToUpdate);
    }

    @CachePut(value = "stableProblemsCache", key = "#problem.onlineJudge.toString().toLowerCase() + '-' + #problem.problemCode")
    public Problem putStableProblemToCache(Problem problem) {
        return problem;
    }

    @CacheEvict(value = "newProblemsCache", key = "#source + '-' + #code")
    public void evictNewProblemCache(String source, String code) {

    }

    private boolean isEligibleForMigration(Instant fetchedAt) {
        if (fetchedAt == null) return false;
        return fetchedAt.isBefore(Instant.now().minus(HYBRID_TTL_DAYS, ChronoUnit.DAYS));
    }

    /// get all problems
    public Page<ProblemResponse> getAllProblems(Pageable pageable) {
        Page<Problem> problemEntities = problemRepository.findAll(pageable);
        return problemEntities.map(problemMapper::toResponseDTO);
    }
}
