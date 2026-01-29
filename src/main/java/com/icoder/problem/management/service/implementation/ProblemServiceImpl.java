package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ScrapingException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.problem.management.scraping.service.ScrapingServiceImpl;
import com.icoder.problem.management.service.interfaces.ProblemPersistenceService;
import com.icoder.problem.management.service.interfaces.ProblemService;
import com.icoder.problem.management.service.specification.ProblemSpecificationsBuilder;
import com.icoder.user.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final SecurityUtils securityUtils;
    private final ScrapingServiceImpl scrapingService;
    private final UserRepository userRepository;
    private final ProblemUserRelationRepository relationRepository;
    private final ProblemPersistenceService persistenceService;

    ///  get problem metadata
    @Override
    public ProblemResponse getProblemMetadata(String source, String code) {
        OJudgeType judgeType = OJudgeType.fromString(source);

        Optional<Problem> existingProblem = problemRepository.findByProblemCodeAndOnlineJudge(code, judgeType);

        if (existingProblem.isPresent() && existingProblem.get().getProblemTitle() != null) {
            log.info("Problem metadata is found in DB: {} - {}", source, code);
            return problemMapper.toResponseDTO(existingProblem.get());
        }

        log.info("Problem metadata will be scrapped for: {} - {}", source, code);
        ProblemResponse response = scrapingService.scrapMetaData(source, code);

        return persistenceService.saveScrapedMetadata(response, judgeType);
    }

    ///  get a problem statement
    @Override
    @Transactional
    public ProblemStatementResponse getProblemStatement(String source, String code) {
        try {
            OJudgeType judgeType = OJudgeType.fromString(source);
            Optional<Problem> existingProblem = problemRepository.findByProblemCodeAndOnlineJudge(code, judgeType);

            if (existingProblem.isPresent() && existingProblem.get().getFetchedAt() != null) {
                log.info("problem is found in DB");
                return problemMapper.toStatementDTO(existingProblem.get());
            }

            log.info("problem will be scrapped");
            ProblemStatementResponse scraped =
                    scrapingService.scrapFullStatement(source, code);
            log.info("problem statement scrapped");

            if (scraped.getSections() == null || scraped.getSections().isEmpty()) {
                throw new ScrapingException("Cannot get full statement of the problem.");
            }

//            scraped.setProblemId(existingProblem.get().getId());
            scraped.setProblemCode(existingProblem.get().getProblemCode());
//            scraped.setProblemLink(existingProblem.get().getProblemLink());
//            scraped.setProblemTitle(existingProblem.get().getProblemTitle());
            scraped.setOnlineJudge(existingProblem.get().getOnlineJudge());

            return persistenceService.saveFullStatement(scraped, judgeType);
        } catch (ScrapingException e) {
            log.error("Failed to fetch problem statement: {}", e.getMessage());
            throw e;
        }
    }

    @Override
    @Transactional
    public void setFavorite(FavoriteRequest request) {
        Long userId = securityUtils.getCurrentUserId();
        Long problemId = request.getProblemId();

        ProblemUserRelation relation = relationRepository.findByUserIdAndProblemId(userId, problemId)
                .orElseGet(() -> {
                    ProblemUserRelation newRel = new ProblemUserRelation();
                    newRel.setUser(userRepository.getReferenceById(userId));
                    newRel.setProblem(problemRepository.getReferenceById(problemId));
                    return newRel;
                });

        relation.setFavorite(request.isFavorite());
        relationRepository.save(relation);
    }

    /// get all problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getAllProblems(String oj, String code, String title, Pageable pageable) {
        Long currentUserId = securityUtils.getCurrentUserId();
        ProblemSpecificationsBuilder builder = new ProblemSpecificationsBuilder();

        if (oj != null) builder.with("onlineJudge", ":", OJudgeType.fromString(oj));
        if (code != null) builder.with("problemCode", ":", code);
        if (title != null) builder.with("problemTitle", ":", title);
        if (title != null) builder.with("problemTitle", ":", title);

        Specification<Problem> spec = builder.build();

        Page<Problem> problemsPage = problemRepository.findAll(spec, pageable);

        List<Problem> problems = problemsPage.getContent();
        List<ProblemUserRelation> relations =
                relationRepository.findByUserIdAndProblemIn(currentUserId, problems);
        Map<Long, ProblemUserRelation> solvedStatusMap = relations.stream()
                .collect(Collectors.toMap(
                        relation -> relation.getProblem().getId(),
                        relation -> relation
                ));

        Page<ProblemResponse> responsePage = problemsPage.map(problem -> {
            ProblemResponse responseDTO = problemMapper.toResponseDTO(problem);

            ProblemUserRelation relation = solvedStatusMap.get(problem.getId());

            if (relation != null) {
                responseDTO.setSolved(relation.isSolved());
                responseDTO.setAttempted(relation.isAttempted());
                responseDTO.setFavorite(relation.isFavorite());
            } else {
                responseDTO.setSolved(false);
                responseDTO.setAttempted(false);
                responseDTO.setFavorite(false);
            }

            return responseDTO;
        });

        return responsePage;
    }

    /// get all attempted problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getAttempted(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndAttemptedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all solved problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getSolved(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndSolvedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all favorite problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getFavorites(Pageable pageable) {
        Long userId = securityUtils.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndFavoriteTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }
}