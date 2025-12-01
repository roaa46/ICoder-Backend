package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ApiException;
import com.icoder.core.exception.ProblemNotFoundException;
import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.mapper.PropertyMapper;
import com.icoder.problem.management.mapper.SectionMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.problem.management.scraping.service.ScrapingServiceImpl;
import com.icoder.problem.management.service.interfaces.ProblemService;
import com.icoder.problem.management.service.specification.ProblemSpecificationsBuilder;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.implementation.AuthenticationServiceImpl;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
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
    private final UserRepository userRepository;
    private final ProblemUserRelationRepository relationRepository;
    private final AuthenticationServiceImpl authenticationService;
    private static final long HYBRID_TTL_DAYS = 7;

    ///  get problem metadata
    @Override
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
    @Override
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

    /// update favorite status of a problem
    @Transactional
    public void setFavorite(FavoriteRequest request) {
        User user = userRepository.findById(authenticationService.getCurrentUserId())
                .orElseThrow(() -> new ApiException("User not found"));
        Problem problem = problemRepository.findById(request.getProblemId())
                .orElseThrow(() -> new ProblemNotFoundException("Problem not found"));
        ProblemUserRelation relation = relationRepository
                .findByUserIdAndProblemId(user.getId(), problem.getId())
                .orElse(new ProblemUserRelation());
        relation.setUser(user);
        relation.setProblem(problem);
        relation.setFavorite(request.isFavorite());
        relationRepository.save(relation);
    }

    /// get all problems
    @Override
    public Page<ProblemResponse> getAllProblems(String oj, String code, String title, Pageable pageable) {
        ProblemSpecificationsBuilder builder = new ProblemSpecificationsBuilder();

        if (oj != null) builder.with("onlineJudge", ":", OJudgeType.valueOf(oj.toUpperCase()));
        if (code != null) builder.with("problemCode", ":", code);
        if (title != null) builder.with("problemTitle", ":", title);
        if (title != null) builder.with("problemTitle", ":", title);

        Specification<Problem> spec = builder.build();

        Page<Problem> problems = problemRepository.findAll(spec, pageable);
        return problems.map(problemMapper::toResponseDTO);
    }

    /// get all attempted problems
    @Override
    public Page<ProblemResponse> getAttempted(Pageable pageable) {
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndIsAttemptedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all solved problems
    @Override
    public Page<ProblemResponse> getSolved(Pageable pageable) {
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndIsSolvedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all favorite problems
    @Override
    public Page<ProblemResponse> getFavorites(Pageable pageable) {
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndIsFavoriteTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }
}
