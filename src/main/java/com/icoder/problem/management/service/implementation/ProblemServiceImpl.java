package com.icoder.problem.management.service.implementation;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.core.exception.ProblemNotFoundException;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.service.interfaces.ProblemService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProblemServiceImpl implements ProblemService {
    private final ProblemRepository problemRepository;
    private final ProblemMapper problemMapper;

    /// find a specific problem. if not found, then scrap it
    public ProblemStatementResponse getProblem(OJudgeType judgeSource, String ProblemCode) {

        Optional<Problem> problem = problemRepository.findByProblemCodeAndOnlineJudge(ProblemCode, judgeSource);
                if (problem.isPresent())
            return problemMapper.toStatementDTO(problem.get());

        // this is where you call the scraping service if the problem is NOT found.
        // return scrapAndSaveProblem(ProblemCode, judgeSource);

        // temporary fix until scrap is implemented:
        throw new ProblemNotFoundException("Problem not found in DB or external judge");
    }

    /// view all problems
    public Page<ProblemResponse> getAllProblems(Pageable pageable) {
        Page<Problem> problemEntities = problemRepository.findAll(pageable);
        return problemEntities.map(problemMapper::toResponseDTO);
    }
}
