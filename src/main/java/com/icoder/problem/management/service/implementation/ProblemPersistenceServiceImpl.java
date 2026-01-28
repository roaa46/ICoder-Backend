package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.dto.PropertyScrapeDTO;
import com.icoder.problem.management.dto.SectionScrapeDTO;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemProperty;
import com.icoder.problem.management.entity.ProblemSection;
import com.icoder.problem.management.enums.FormatType;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.mapper.PropertyMapper;
import com.icoder.problem.management.mapper.SectionMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.service.interfaces.ProblemPersistenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProblemPersistenceServiceImpl implements ProblemPersistenceService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;
    private final PropertyMapper propertyMapper;
    private final SectionMapper sectionMapper;

    @Override
    @Transactional
    public ProblemResponse saveScrapedMetadata(ProblemResponse response, OJudgeType judgeType) {
        Problem problemToSave = problemRepository.findByProblemCodeAndOnlineJudge(
                response.getProblemCode(), judgeType).orElse(new Problem());

        problemToSave.setProblemCode(response.getProblemCode());
        problemToSave.setOnlineJudge(judgeType);
        problemToSave.setContestTitle(response.getContestTitle());
        problemToSave.setContestLink(response.getContestLink());
        problemToSave.setProblemTitle(response.getProblemTitle());
        problemToSave.setProblemLink(response.getProblemLink());

        Problem savedProblem = problemRepository.save(problemToSave);

        return problemMapper.toResponseDTO(savedProblem);
    }

    @Override
    @Transactional
    public ProblemStatementResponse saveFullStatement(ProblemStatementResponse scrapedResponse, OJudgeType judgeType) {
        log.info("Saving full statement for: {} - {}", scrapedResponse.getProblemCode(), judgeType);
        log.info("response data: {}", scrapedResponse);
        Problem problem = problemRepository
                .findByProblemCodeAndOnlineJudge(scrapedResponse.getProblemCode(), judgeType)
                .orElseThrow(() -> new ResourceNotFoundException("Metadata not found"));
        log.info("Problem found: {}", problem);
        updateProperties(problem, scrapedResponse.getProperties());
        updateSections(problem, scrapedResponse.getSections());
        problem.setFetchedAt(Instant.now());

        return problemMapper.toStatementDTO(problemRepository.save(problem));
    }

    private void updateProperties(Problem problem, List<PropertyScrapeDTO> dtos) {
        List<ProblemProperty> newProperties = propertyMapper.toListEntity(dtos);

        problem.getProperties().clear();
        if (newProperties != null) {
            newProperties.forEach(prop -> {
                prop.setProblem(problem);
                prop.setContentType(FormatType.PLAIN_TEXT);
                problem.getProperties().add(prop);
            });
        }
    }

    private void updateSections(Problem problem, List<SectionScrapeDTO> dtos) {
        List<ProblemSection> newSections = sectionMapper.toListEntity(dtos);

        problem.getSections().clear();
        if (newSections != null) {
            newSections.forEach(section -> {
                section.setProblem(problem);
                if (section.getContents() != null) {
                    section.getContents().forEach(content -> content.setSection(section));
                }
                problem.getSections().add(section);
            });
        }
    }
}
