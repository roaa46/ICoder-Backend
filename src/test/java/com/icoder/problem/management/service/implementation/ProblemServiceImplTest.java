package com.icoder.problem.management.service.implementation;

import com.icoder.core.exception.ScrapingException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.problem.management.dto.FavoriteRequest;
import com.icoder.problem.management.dto.ProblemResponse;
import com.icoder.problem.management.dto.ProblemStatementResponse;
import com.icoder.problem.management.dto.SectionScrapeDTO;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.entity.ProblemUserRelation;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.problem.management.mapper.ProblemMapper;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.problem.management.repository.ProblemUserRelationRepository;
import com.icoder.problem.management.scraping.service.ScrapingServiceImpl;
import com.icoder.problem.management.service.interfaces.ProblemPersistenceService;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemServiceImplTest {

    @Mock
    private ProblemRepository problemRepository;

    @Mock
    private ProblemMapper problemMapper;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private ScrapingServiceImpl scrapingService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProblemUserRelationRepository relationRepository;

    @Mock
    private ProblemPersistenceService persistenceService;

    @InjectMocks
    private ProblemServiceImpl problemService;

    private Problem problem;
    private Problem anotherProblem;
    private ProblemResponse problemResponse;
    private ProblemResponse anotherProblemResponse;
    private ProblemStatementResponse statementResponse;
    private ProblemUserRelation relation;
    private FavoriteRequest favoriteRequest;
    private Pageable pageable;
    private User user;

    @BeforeEach
    void setUp() {
        problem = new Problem();
        problem.setId(1L);
        problem.setProblemCode("100A");
        problem.setProblemTitle("Watermelon");
        problem.setOnlineJudge(OJudgeType.CODEFORCES);

        anotherProblem = new Problem();
        anotherProblem.setId(2L);
        anotherProblem.setProblemCode("200B");
        anotherProblem.setProblemTitle("Drinks");
        anotherProblem.setOnlineJudge(OJudgeType.CODEFORCES);

        problemResponse = new ProblemResponse();
        problemResponse.setProblemCode("100A");
        problemResponse.setProblemTitle("Watermelon");
        problemResponse.setOnlineJudge(OJudgeType.CODEFORCES);

        anotherProblemResponse = new ProblemResponse();
        anotherProblemResponse.setProblemCode("200B");
        anotherProblemResponse.setProblemTitle("Drinks");
        anotherProblemResponse.setOnlineJudge(OJudgeType.CODEFORCES);

        statementResponse = new ProblemStatementResponse();
        statementResponse.setProblemCode("100A");
        statementResponse.setOnlineJudge(OJudgeType.CODEFORCES);
        statementResponse.setSections(List.of());

        relation = new ProblemUserRelation();
        relation.setProblem(problem);

        favoriteRequest = new FavoriteRequest();
        favoriteRequest.setProblemId(1L);
        favoriteRequest.setFavorite(true);

        pageable = PageRequest.of(0, 10);

        user = new User();
        user.setId(10L);
    }

    @Nested
    @DisplayName("getProblemMetadata()")
    class GetProblemMetadataTests {

        @Test
        @DisplayName("should return metadata from database when problem exists with title")
        void getProblemMetadata_shouldReturnFromDatabase_whenProblemExistsWithTitle() {
            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(problem));
            when(problemMapper.toResponseDTO(problem)).thenReturn(problemResponse);

            ProblemResponse result = problemService.getProblemMetadata("codeforces", "100A");

            assertNotNull(result);
            assertEquals("100A", result.getProblemCode());

            verify(problemRepository).findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES);
            verify(problemMapper).toResponseDTO(problem);
            verifyNoInteractions(scrapingService, persistenceService);
        }

        @Test
        @DisplayName("should scrape and save metadata when problem not found")
        void getProblemMetadata_shouldScrapeAndSave_whenProblemNotFound() {
            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.empty());
            when(scrapingService.scrapMetaData("codeforces", "100A")).thenReturn(problemResponse);
            when(persistenceService.saveScrapedMetadata(problemResponse, OJudgeType.CODEFORCES))
                    .thenReturn(problemResponse);

            ProblemResponse result = problemService.getProblemMetadata("codeforces", "100A");

            assertNotNull(result);
            assertEquals("100A", result.getProblemCode());

            verify(scrapingService).scrapMetaData("codeforces", "100A");
            verify(persistenceService).saveScrapedMetadata(problemResponse, OJudgeType.CODEFORCES);
            verify(problemMapper, never()).toResponseDTO(any(Problem.class));
        }

        @Test
        @DisplayName("should scrape and save metadata when problem exists but title is null")
        void getProblemMetadata_shouldScrapeAndSave_whenTitleIsNull() {
            problem.setProblemTitle(null);

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(problem));
            when(scrapingService.scrapMetaData("codeforces", "100A")).thenReturn(problemResponse);
            when(persistenceService.saveScrapedMetadata(problemResponse, OJudgeType.CODEFORCES))
                    .thenReturn(problemResponse);

            ProblemResponse result = problemService.getProblemMetadata("codeforces", "100A");

            assertNotNull(result);

            verify(scrapingService).scrapMetaData("codeforces", "100A");
            verify(persistenceService).saveScrapedMetadata(problemResponse, OJudgeType.CODEFORCES);
        }
    }

    @Nested
    @DisplayName("getProblemStatement()")
    class GetProblemStatementTests {

        @Test
        @DisplayName("should return statement from database when problem fetchedAt exists")
        void getProblemStatement_shouldReturnFromDatabase_whenFetchedAtExists() {
            ProblemStatementResponse dbStatement = new ProblemStatementResponse();
            dbStatement.setProblemCode("100A");
            dbStatement.setOnlineJudge(OJudgeType.CODEFORCES);

            problem.setFetchedAt(Instant.now());

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(problem));
            when(problemMapper.toStatementDTO(problem)).thenReturn(dbStatement);

            ProblemStatementResponse result = problemService.getProblemStatement("codeforces", "100A");

            assertNotNull(result);
            assertEquals("100A", result.getProblemCode());

            verify(problemMapper).toStatementDTO(problem);
            verifyNoInteractions(scrapingService, persistenceService);
        }

        @Test
        @DisplayName("should scrape and save full statement when not fetched before")
        void getProblemStatement_shouldScrapeAndSave_whenNotFetchedBefore() {
            Problem existingProblem = new Problem();
            existingProblem.setId(1L);
            existingProblem.setProblemCode("100A");
            existingProblem.setOnlineJudge(OJudgeType.CODEFORCES);
            existingProblem.setFetchedAt(null);

            ProblemStatementResponse scraped = new ProblemStatementResponse();
            scraped.setSections(List.of(
                    SectionScrapeDTO.builder()
                            .sectionId(1L)
                            .title("Statement")
                            .orderIndex(1)
                            .contents(List.of())
                            .build(),
                    SectionScrapeDTO.builder()
                            .sectionId(2L)
                            .title("Input")
                            .orderIndex(2)
                            .contents(List.of())
                            .build(),
                    SectionScrapeDTO.builder()
                            .sectionId(3L)
                            .title("Output")
                            .orderIndex(3)
                            .contents(List.of())
                            .build()
            ));

            ProblemStatementResponse saved = new ProblemStatementResponse();
            saved.setProblemCode("100A");
            saved.setOnlineJudge(OJudgeType.CODEFORCES);
            saved.setSections(List.of(
                    SectionScrapeDTO.builder()
                            .sectionId(1L)
                            .title("Statement")
                            .orderIndex(1)
                            .contents(List.of())
                            .build(),
                    SectionScrapeDTO.builder()
                            .sectionId(2L)
                            .title("Input")
                            .orderIndex(2)
                            .contents(List.of())
                            .build(),
                    SectionScrapeDTO.builder()
                            .sectionId(3L)
                            .title("Output")
                            .orderIndex(3)
                            .contents(List.of())
                            .build()
            ));

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(existingProblem));
            when(scrapingService.scrapFullStatement("codeforces", "100A")).thenReturn(scraped);
            when(persistenceService.saveFullStatement(scraped, OJudgeType.CODEFORCES)).thenReturn(saved);

            ProblemStatementResponse result = problemService.getProblemStatement("codeforces", "100A");

            assertNotNull(result);
            assertEquals("100A", scraped.getProblemCode());
            assertEquals(OJudgeType.CODEFORCES, scraped.getOnlineJudge());
            assertEquals(saved, result);

            verify(scrapingService).scrapFullStatement("codeforces", "100A");
            verify(persistenceService).saveFullStatement(scraped, OJudgeType.CODEFORCES);
        }

        @Test
        @DisplayName("should throw ScrapingException when scraped sections are null")
        void getProblemStatement_shouldThrowScrapingException_whenSectionsNull() {
            Problem existingProblem = new Problem();
            existingProblem.setProblemCode("100A");
            existingProblem.setOnlineJudge(OJudgeType.CODEFORCES);

            ProblemStatementResponse scraped = new ProblemStatementResponse();
            scraped.setSections(null);

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(existingProblem));
            when(scrapingService.scrapFullStatement("codeforces", "100A")).thenReturn(scraped);

            ScrapingException ex = assertThrows(
                    ScrapingException.class,
                    () -> problemService.getProblemStatement("codeforces", "100A")
            );

            assertEquals("Cannot get full statement of the problem.", ex.getMessage());

            verify(persistenceService, never()).saveFullStatement(any(), any());
        }

        @Test
        @DisplayName("should throw ScrapingException when scraped sections are empty")
        void getProblemStatement_shouldThrowScrapingException_whenSectionsEmpty() {
            Problem existingProblem = new Problem();
            existingProblem.setProblemCode("100A");
            existingProblem.setOnlineJudge(OJudgeType.CODEFORCES);

            ProblemStatementResponse scraped = new ProblemStatementResponse();
            scraped.setSections(List.of());

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(existingProblem));
            when(scrapingService.scrapFullStatement("codeforces", "100A")).thenReturn(scraped);

            ScrapingException ex = assertThrows(
                    ScrapingException.class,
                    () -> problemService.getProblemStatement("codeforces", "100A")
            );

            assertEquals("Cannot get full statement of the problem.", ex.getMessage());

            verify(persistenceService, never()).saveFullStatement(any(), any());
        }

        @Test
        @DisplayName("should rethrow ScrapingException from scraper")
        void getProblemStatement_shouldRethrowScrapingException_whenScraperFails() {
            Problem existingProblem = new Problem();
            existingProblem.setProblemCode("100A");
            existingProblem.setOnlineJudge(OJudgeType.CODEFORCES);

            when(problemRepository.findByProblemCodeAndOnlineJudge("100A", OJudgeType.CODEFORCES))
                    .thenReturn(Optional.of(existingProblem));
            when(scrapingService.scrapFullStatement("codeforces", "100A"))
                    .thenThrow(new ScrapingException("scraping failed"));

            ScrapingException ex = assertThrows(
                    ScrapingException.class,
                    () -> problemService.getProblemStatement("codeforces", "100A")
            );

            assertEquals("scraping failed", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("setFavorite()")
    class SetFavoriteTests {

        @Test
        @DisplayName("should update favorite on existing relation")
        void setFavorite_shouldUpdateExistingRelation() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(relationRepository.findByUserIdAndProblemId(10L, 1L))
                    .thenReturn(Optional.of(relation));

            problemService.setFavorite(favoriteRequest);

            assertTrue(relation.isFavorite());

            verify(relationRepository).findByUserIdAndProblemId(10L, 1L);
            verify(relationRepository).save(relation);
            verifyNoInteractions(userRepository, problemRepository);
        }

        @Test
        @DisplayName("should create relation when it does not exist")
        void setFavorite_shouldCreateRelation_whenNotExists() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(relationRepository.findByUserIdAndProblemId(10L, 1L))
                    .thenReturn(Optional.empty());
            when(userRepository.getReferenceById(10L)).thenReturn(user);
            when(problemRepository.getReferenceById(1L)).thenReturn(problem);

            problemService.setFavorite(favoriteRequest);

            verify(relationRepository).findByUserIdAndProblemId(10L, 1L);
            verify(userRepository).getReferenceById(10L);
            verify(problemRepository).getReferenceById(1L);
            verify(relationRepository).save(any(ProblemUserRelation.class));
        }
    }

    @Nested
    @DisplayName("getAllProblems()")
    class GetAllProblemsTests {

        @Test
        @DisplayName("should return mapped page with user relations")
        void getAllProblems_shouldReturnMappedPage() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            Page<Problem> problemPage = new PageImpl<>(List.of(problem, anotherProblem), pageable, 2);
            ProblemUserRelation relation1 = new ProblemUserRelation();
            relation1.setProblem(problem);

            when(problemRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(problemPage);
            when(relationRepository.findByUserIdAndProblemIn(10L, List.of(problem, anotherProblem)))
                    .thenReturn(List.of(relation1));
            when(problemMapper.toResponseDTO(problem, relation1)).thenReturn(problemResponse);
            when(problemMapper.toResponseDTO(anotherProblem, null)).thenReturn(anotherProblemResponse);

            Page<ProblemResponse> result = problemService.getAllProblems("codeforces", null, null, pageable);

            assertNotNull(result);
            assertEquals(2, result.getTotalElements());
            assertEquals("100A", result.getContent().get(0).getProblemCode());
            assertEquals("200B", result.getContent().get(1).getProblemCode());

            verify(problemRepository).findAll(any(Specification.class), any(Pageable.class));
            verify(relationRepository).findByUserIdAndProblemIn(10L, List.of(problem, anotherProblem));
            verify(problemMapper).toResponseDTO(problem, relation1);
            verify(problemMapper).toResponseDTO(anotherProblem, null);
        }

        @Test
        @DisplayName("should handle null online judge filter")
        void getAllProblems_shouldHandleNullOj() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            Page<Problem> problemPage = new PageImpl<>(List.of(problem), pageable, 1);

            doReturn(problemPage)
                    .when(problemRepository)
                    .findAll(nullable(Specification.class), any(Pageable.class));

            when(relationRepository.findByUserIdAndProblemIn(anyLong(), anyList()))
                    .thenReturn(List.of());

            when(problemMapper.toResponseDTO(any(), nullable(ProblemUserRelation.class)))
                    .thenReturn(problemResponse);

            Page<ProblemResponse> result = problemService.getAllProblems(null, null, null, pageable);

            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getAttempted()")
    class GetAttemptedTests {

        @Test
        @DisplayName("should return attempted problems page")
        void getAttempted_shouldReturnMappedPage() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            ProblemUserRelation attemptedRelation = new ProblemUserRelation();
            attemptedRelation.setProblem(problem);

            Page<ProblemUserRelation> relationPage =
                    new PageImpl<>(List.of(attemptedRelation), pageable, 1);

            when(relationRepository.findByUserIdAndAttemptedTrue(10L, pageable)).thenReturn(relationPage);
            when(problemMapper.toResponseDTO(problem, attemptedRelation)).thenReturn(problemResponse);

            Page<ProblemResponse> result = problemService.getAttempted(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("100A", result.getContent().get(0).getProblemCode());

            verify(relationRepository).findByUserIdAndAttemptedTrue(10L, pageable);
            verify(problemMapper).toResponseDTO(problem, attemptedRelation);
        }
    }

    @Nested
    @DisplayName("getSolved()")
    class GetSolvedTests {

        @Test
        @DisplayName("should return solved problems page")
        void getSolved_shouldReturnMappedPage() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            ProblemUserRelation solvedRelation = new ProblemUserRelation();
            solvedRelation.setProblem(problem);

            Page<ProblemUserRelation> relationPage =
                    new PageImpl<>(List.of(solvedRelation), pageable, 1);

            when(relationRepository.findByUserIdAndSolvedTrue(10L, pageable)).thenReturn(relationPage);
            when(problemMapper.toResponseDTO(problem, solvedRelation)).thenReturn(problemResponse);

            Page<ProblemResponse> result = problemService.getSolved(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("100A", result.getContent().get(0).getProblemCode());

            verify(relationRepository).findByUserIdAndSolvedTrue(10L, pageable);
            verify(problemMapper).toResponseDTO(problem, solvedRelation);
        }
    }

    @Nested
    @DisplayName("getFavorites()")
    class GetFavoritesTests {

        @Test
        @DisplayName("should return favorite problems page")
        void getFavorites_shouldReturnMappedPage() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            ProblemUserRelation favoriteRelation = new ProblemUserRelation();
            favoriteRelation.setProblem(problem);

            Page<ProblemUserRelation> relationPage =
                    new PageImpl<>(List.of(favoriteRelation), pageable, 1);

            when(relationRepository.findByUserIdAndFavoriteTrue(10L, pageable)).thenReturn(relationPage);
            when(problemMapper.toResponseDTO(problem, favoriteRelation)).thenReturn(problemResponse);

            Page<ProblemResponse> result = problemService.getFavorites(pageable);

            assertEquals(1, result.getTotalElements());
            assertEquals("100A", result.getContent().get(0).getProblemCode());

            verify(relationRepository).findByUserIdAndFavoriteTrue(10L, pageable);
            verify(problemMapper).toResponseDTO(problem, favoriteRelation);
        }
    }
}