package com.icoder.problem.management.service.interfaces;

import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProblemService {
    @Transactional
    ProblemResponse getProblemMetadata(String source, String code);

    @Transactional
    ProblemStatementResponse getProblemStatement(String source, String code);

    @Transactional
    ProblemStatementResponse scrapFullStatement(String source, String code);

    @Transactional
    void setFavorite(FavoriteRequest request);

    @Transactional
    Page<ProblemResponse> getAllProblems(String oj, String code, String title, Pageable pageable);

    @Transactional
    Page<ProblemResponse> getAttempted(Pageable pageable);

    @Transactional
    Page<ProblemResponse> getSolved(Pageable pageable);

    @Transactional
    Page<ProblemResponse> getFavorites(Pageable pageable);
}
