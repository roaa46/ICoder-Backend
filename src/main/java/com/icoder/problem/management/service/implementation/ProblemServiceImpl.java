package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ProblemNotFoundException;
import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemProperty;
import com.icoder.problem.management.entity.ProblemSection;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.enums.FormatType;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.mapper.PropertyMapper;
import com.icoder.problem.management.mapper.SectionMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.problem.management.scraping.service.ScrapingServiceImpl;
import com.icoder.problem.management.service.interfaces.ProblemService;
import com.icoder.problem.management.service.specification.ProblemSpecificationsBuilder;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.implementation.AuthenticationServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    private final PropertyMapper propertyMapper;
    private final SectionMapper sectionMapper;
    private final ScrapingServiceImpl scrapingService;
    private final UserRepository userRepository;
    private final ProblemUserRelationRepository relationRepository;
    private final AuthenticationServiceImpl authenticationService;

    ///  get problem metadata
    @Override
    @Transactional
    public ProblemResponse getProblemMetadata(String source, String code) {
        Optional<Problem> existingProblem = problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase()));

        if (existingProblem.isPresent() && existingProblem.get().getProblemTitle() != null) {
            log.info("problem metadata is found in DB");
            return problemMapper.toResponseDTO(existingProblem.get());
        }

        log.info("problem metadata will be scrapped");
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
        problemResponse.setOnlineJudge(source.toUpperCase());

        return problemMapper.toResponseDTO(newProblem);
    }

    ///  get a problem statement
    @Override
    @Transactional
    public ProblemStatementResponse getProblemStatement(String source, String code) {
        Optional<Problem> existingProblem = problemRepository.findByProblemCodeAndOnlineJudge(code, OJudgeType.valueOf(source.toUpperCase()));

        if (existingProblem.isPresent() && existingProblem.get().getFetchedAt() != null) {
            log.info("problem is found in DB");
            return problemMapper.toStatementDTO(existingProblem.get());
        }
        log.info("problem will be scrapped");

        return scrapFullStatement(source, code);
    }

    @Override
    @Transactional
    public ProblemStatementResponse scrapFullStatement(String source, String code) {

        ProblemStatementResponse scrapedResponse =
                scrapingService.scrapFullStatement(source, code);

        Problem problemToUpdate = problemRepository
                .findByProblemCodeAndOnlineJudge(
                        code,
                        OJudgeType.valueOf(source.toUpperCase())
                )
                .orElseThrow(() ->
                        new ProblemNotFoundException(
                                "Metadata not found for problem " + source + "-" + code
                        )
                );

        // ================= PROPERTIES =================
        List<ProblemProperty> newProperties =
                propertyMapper.toListEntity(scrapedResponse.getProperties());

        problemToUpdate.getProperties().clear();
        for (ProblemProperty property : newProperties) {
            property.setProblem(problemToUpdate);
            property.setContentType(FormatType.PLAIN_TEXT);
            problemToUpdate.getProperties().add(property);
        }

        // ================= SECTIONS =================
        List<ProblemSection> newSections =
                sectionMapper.toListEntity(scrapedResponse.getSections());

        problemToUpdate.getSections().clear();

        for (ProblemSection section : newSections) {
            section.setProblem(problemToUpdate);

            section.getContents().forEach(content ->
                    content.setSection(section)
            );

            problemToUpdate.getSections().add(section);
        }

        problemToUpdate.setFetchedAt(Instant.now());

        Problem savedProblem = problemRepository.save(problemToUpdate);
        return problemMapper.toStatementDTO(savedProblem);
    }


    @Override
    @Transactional
    public void setFavorite(FavoriteRequest request) {
        Long userId = authenticationService.getCurrentUserId();
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
        Long currentUserId = authenticationService.getCurrentUserId();
        ProblemSpecificationsBuilder builder = new ProblemSpecificationsBuilder();

        if (oj != null) builder.with("onlineJudge", ":", OJudgeType.valueOf(oj.toUpperCase()));
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
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndAttemptedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all solved problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getSolved(Pageable pageable) {
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndSolvedTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }

    /// get all favorite problems
    @Override
    @Transactional(readOnly = true)
    public Page<ProblemResponse> getFavorites(Pageable pageable) {
        Long userId = authenticationService.getCurrentUserId();
        Page<ProblemUserRelation> relations = relationRepository.findByUserIdAndFavoriteTrue(userId, pageable);
        return relations.map(rel -> problemMapper.toResponseDTO(rel.getProblem()));
    }
}
