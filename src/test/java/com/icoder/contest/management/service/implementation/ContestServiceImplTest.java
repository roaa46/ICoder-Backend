package com.icoder.contest.management.service.implementation;

import com.icoder.contest.management.dto.ContestDetailsResponse;
import com.icoder.contest.management.dto.ContestResponse;
import com.icoder.contest.management.dto.CreateContestRequest;
import com.icoder.contest.management.dto.ProblemSetResponse;
import com.icoder.contest.management.entity.Contest;
import com.icoder.contest.management.entity.ContestProblemRelation;
import com.icoder.contest.management.entity.ContestUserRelation;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestRole;
import com.icoder.contest.management.enums.ContestStatus;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.contest.management.mapper.ContestMapper;
import com.icoder.contest.management.repository.ContestRepository;
import com.icoder.contest.management.repository.ContestUserRelationRepository;
import com.icoder.contest.management.util.ContestUtils;
import com.icoder.core.dto.MessageResponse;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.group.management.entity.Group;
import com.icoder.group.management.repository.GroupRepository;
import com.icoder.problem.management.entity.Problem;
import com.icoder.submission.management.repository.SubmissionRepository;
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
import org.springframework.security.access.AccessDeniedException;

import java.time.Duration;
import java.time.Instant;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContestServiceImplTest {

    @Mock
    private ContestRepository contestRepository;

    @Mock
    private ContestUtils contestUtils;

    @Mock
    private SecurityUtils securityUtils;

    @Mock
    private GroupRepository groupRepository;

    @Mock
    private ContestMapper contestMapper;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ContestUserRelationRepository contestUserRelationRepository;

    @Mock
    private SubmissionRepository submissionRepository;

    @InjectMocks
    private ContestServiceImpl contestService;

    private CreateContestRequest request;
    private Group group;
    private Contest contest;
    private User user;
    private ContestUserRelation ownerRelation;
    private ContestDetailsResponse contestDetailsResponse;
    private ContestResponse contestResponse;
    private ContestProblemRelation relation1;
    private ContestProblemRelation relation2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        request = mock(CreateContestRequest.class);

        group = new Group();
        group.setId(1L);

        contest = Contest.builder()
                .title("Weekly Contest")
                .description("Contest Description")
                .beginTime(Instant.parse("2026-04-04T10:00:00Z"))
                .endTime(Instant.parse("2026-04-04T12:00:00Z"))
                .length(Duration.ofHours(2))
                .contestType(ContestType.CLASSICAL)
                .contestOpenness(ContestOpenness.PUBLIC)
                .build();
        contest.setId(100L);
        contest.setGroup(group);
        contest.setProblemRelation(new LinkedHashSet<>());

        user = new User();
        user.setId(10L);
        user.setHandle("roaa");

        ownerRelation = ContestUserRelation.builder()
                .contest(contest)
                .user(user)
                .role(ContestRole.OWNER)
                .build();

        contestDetailsResponse = new ContestDetailsResponse();
        contestDetailsResponse.setTitle("Weekly Contest");

        contestResponse = new ContestResponse();
        contestResponse.setTitle("Weekly Contest");

        relation1 = new ContestProblemRelation();
        relation2 = new ContestProblemRelation();

        pageable = PageRequest.of(0, 10);
    }

    @Nested
    @DisplayName("createContest()")
    class CreateContestTests {

        @Test
        @DisplayName("should create contest successfully")
        void createContest_shouldCreateContestSuccessfully() {
            Set problemSet = Set.of(new Object(), new Object());
            Set<ContestProblemRelation> relations = new LinkedHashSet<>(Set.of(relation1, relation2));
            Duration duration = Duration.ofHours(2);
            Instant beginTime = Instant.parse("2026-04-04T10:00:00Z");

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(request.getGroupId()).thenReturn(1L);
            when(request.getProblemSet()).thenReturn(problemSet);
            when(request.getLength()).thenReturn("02:00:00");
            when(request.getTitle()).thenReturn("Weekly Contest");
            when(request.getDescription()).thenReturn("Contest Description");
            when(request.getBeginTime()).thenReturn(beginTime);
            when(request.getContestOpenness()).thenReturn(ContestOpenness.PUBLIC);
            when(request.getContestType()).thenReturn(ContestType.CLASSICAL);
            when(request.getHistoryRank()).thenReturn(true);
            when(request.getPassword()).thenReturn(null);

            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(true);
            when(contestUtils.parseDuration("02:00:00")).thenReturn(duration);
            when(contestUtils.calculateStatus(beginTime, duration)).thenReturn(ContestStatus.SCHEDULED);
            when(contestUtils.mapProblemSetToRelations(eq(problemSet), any(Contest.class))).thenReturn(relations);
            when(userRepository.findById(10L)).thenReturn(Optional.of(user));

            MessageResponse result = contestService.createContest(request);

            assertNotNull(result);
            assertEquals("Contest created successfully with 2 problems.", result.getMessage());

            verify(contestUtils).validateContestRules(request, group);
            verify(contestUtils).checkGroupVisibility(group, request);
            verify(contestUtils).applyContestRulesBasedOnGroupVisibility(any(Contest.class), eq(group), isNull());
            verify(contestRepository).save(any(Contest.class));
        }

        @Test
        @DisplayName("should throw exception when problem set is empty")
        void createContest_shouldThrowException_whenProblemSetEmpty() {
            when(request.getProblemSet()).thenReturn(Set.of());

            IllegalArgumentException ex = assertThrows(
                    IllegalArgumentException.class,
                    () -> contestService.createContest(request)
            );

            assertEquals("A contest must have at least one problem.", ex.getMessage());

            verifyNoInteractions(groupRepository, contestRepository, userRepository);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when group does not exist")
        void createContest_shouldThrowException_whenGroupNotFound() {
            Set problemSet = Set.of(new Object());

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(request.getGroupId()).thenReturn(1L);
            when(request.getProblemSet()).thenReturn(problemSet);
            when(groupRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> contestService.createContest(request)
            );

            assertEquals("Group not found with id: 1", ex.getMessage());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not contest coordinator")
        void createContest_shouldThrowException_whenUserNotCoordinator() {
            Set problemSet = Set.of(new Object());

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(request.getGroupId()).thenReturn(1L);
            when(request.getProblemSet()).thenReturn(problemSet);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(false);

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> contestService.createContest(request)
            );

            assertEquals("User is not a contest coordinator", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("updateContest()")
    class UpdateContestTests {

        @Test
        @DisplayName("should update contest successfully")
        void updateContest_shouldUpdateContestSuccessfully() {
            Set problemSet = Set.of(new Object(), new Object());
            Set<ContestProblemRelation> newRelations = new LinkedHashSet<>(Set.of(relation1, relation2));
            Duration duration = Duration.ofHours(3);
            Instant beginTime = Instant.parse("2026-04-05T10:00:00Z");

            contest.getProblemRelation().add(new ContestProblemRelation());

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
            when(request.getGroupId()).thenReturn(1L);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(true);
            when(request.getProblemSet()).thenReturn(problemSet);
            when(request.getLength()).thenReturn("03:00:00");
            when(request.getBeginTime()).thenReturn(beginTime);
            when(request.getHistoryRank()).thenReturn(false);
            when(request.getPassword()).thenReturn("1234");
            when(contestUtils.parseDuration("03:00:00")).thenReturn(duration);
            when(contestUtils.calculateStatus(beginTime, duration)).thenReturn(ContestStatus.SCHEDULED);
            when(contestUtils.mapProblemSetToRelations(problemSet, contest)).thenReturn(newRelations);

            MessageResponse result = contestService.updateContest(100L, request);

            assertNotNull(result);
            assertEquals("Contest updated successfully.", result.getMessage());
            assertEquals(beginTime, contest.getBeginTime());
            assertEquals(beginTime.plus(duration), contest.getEndTime());
            assertEquals(duration, contest.getLength());
            assertFalse(contest.isHistoryRank());
            assertEquals(2, contest.getProblemRelation().size());

            verify(contestMapper).updateContestFromDto(request, contest);
            verify(contestUtils).checkGroupVisibility(group, request);
            verify(contestUtils).applyContestRulesBasedOnGroupVisibility(contest, group, "1234");
            verify(contestRepository).save(contest);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when contest does not exist")
        void updateContest_shouldThrowException_whenContestNotFound() {
            when(contestRepository.findById(100L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> contestService.updateContest(100L, request)
            );

            assertEquals("Contest not found with id: 100", ex.getMessage());
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not coordinator")
        void updateContest_shouldThrowException_whenUserNotCoordinator() {

            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
            when(request.getGroupId()).thenReturn(1L);
            when(groupRepository.findById(1L)).thenReturn(Optional.of(group));
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(false);

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> contestService.updateContest(100L, request)
            );

            assertEquals("User is not a contest coordinator", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteContest()")
    class DeleteContestTests {

        @Test
        @DisplayName("should delete contest successfully")
        void deleteContest_shouldDeleteContestSuccessfully() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(contestUtils.getGroup(100L)).thenReturn(group);
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(true);

            contestService.deleteContest(100L);

            verify(contestUtils).isContestInGroup(100L, 1L);
            verify(contestRepository).deleteById(100L);
        }

        @Test
        @DisplayName("should throw AccessDeniedException when user is not coordinator")
        void deleteContest_shouldThrowException_whenUserNotCoordinator() {
            when(securityUtils.getCurrentUserId()).thenReturn(10L);
            when(contestUtils.getGroup(100L)).thenReturn(group);
            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(false);

            AccessDeniedException ex = assertThrows(
                    AccessDeniedException.class,
                    () -> contestService.deleteContest(100L)
            );

            assertEquals("User is not a contest coordinator", ex.getMessage());

            verify(contestRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    @DisplayName("viewContestDetails()")
    class ViewContestDetailsTests {

        @Test
        @DisplayName("should return contest details with owner info")
        void viewContestDetails_shouldReturnContestDetailsWithOwnerInfo() {
            when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
            when(contestUserRelationRepository.findByContestIdAndRole(100L, ContestRole.OWNER))
                    .thenReturn(Optional.of(ownerRelation));
            when(contestMapper.toContestDetailsDto(contest)).thenReturn(contestDetailsResponse);

            ContestDetailsResponse result = contestService.viewContestDetails(100L);

            assertNotNull(result);
            assertEquals("Weekly Contest", result.getTitle());
            assertEquals(10L, result.getOwnerId());
            assertEquals("roaa", result.getOwnerHandle());

            verify(contestMapper).toContestDetailsDto(contest);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when owner relation does not exist")
        void viewContestDetails_shouldThrowException_whenOwnerNotFound() {
            when(contestRepository.findById(100L)).thenReturn(Optional.of(contest));
            when(contestUserRelationRepository.findByContestIdAndRole(100L, ContestRole.OWNER))
                    .thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(
                    ResourceNotFoundException.class,
                    () -> contestService.viewContestDetails(100L)
            );

            assertEquals("Contest owner not found for contest with id: 100", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("viewProblemSet()")
    class ViewProblemSetTests {

        @Test
        @DisplayName("should return full problem set for coordinator and set solved status")
        void viewProblemSet_shouldReturnFullProblemSet_forCoordinator() {
            Problem problemA = new Problem();
            problemA.setId(1L);
            problemA.setProblemTitle("Problem A");

            Problem problemB = new Problem();
            problemB.setId(2L);
            problemB.setProblemTitle("Problem B");

            relation1.setProblem(problemA);
            relation2.setProblem(problemB);

            ProblemSetResponse response1 = new ProblemSetResponse();
            response1.setTitle("Problem A");
            response1.setOrigin("Codeforces");

            ProblemSetResponse response2 = new ProblemSetResponse();
            response2.setTitle("Problem B");
            response2.setOrigin("AtCoder");

            contest.setProblemRelation(new LinkedHashSet<>(Set.of(relation1, relation2)));

            // 2. Update the repository method mock
            when(contestRepository.findByIdWithGroupAndProblems(100L)).thenReturn(Optional.of(contest));
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            // 3. Mock the new submission repository call (Let's simulate that Problem A is solved)
            when(submissionRepository.findSolvedProblemIdsByUserIdAndContestId(10L, 100L)).thenReturn(Set.of(1L));

            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(true);
            when(contestUtils.checkIfContestRunning(contest)).thenReturn(true);
            when(contestMapper.toProblemSetResponse(relation1)).thenReturn(response1);
            when(contestMapper.toProblemSetResponse(relation2)).thenReturn(response2);

            Set<ProblemSetResponse> result = contestService.viewProblemSet(100L);

            assertEquals(2, result.size());

            // Assertions include the solved status
            assertTrue(result.stream().anyMatch(r -> "Problem A".equals(r.getTitle()) && r.isSolved()));
            assertTrue(result.stream().anyMatch(r -> "Problem B".equals(r.getTitle()) && !r.isSolved()));
        }

        @Test
        @DisplayName("should hide origin when contest is running and user is not coordinator")
        void viewProblemSet_shouldHideFields_whenRunningAndNotCoordinator() {
            Problem problemA = new Problem();
            problemA.setId(1L);
            problemA.setProblemTitle("Problem A");
            relation1.setProblem(problemA);

            ProblemSetResponse response = new ProblemSetResponse();
            response.setTitle("Problem A");
            response.setOrigin("Codeforces");

            contest.setProblemRelation(new LinkedHashSet<>(Set.of(relation1)));

            when(contestRepository.findByIdWithGroupAndProblems(100L)).thenReturn(Optional.of(contest));
            when(securityUtils.getCurrentUserId()).thenReturn(10L);

            // Mock submission repository (User hasn't solved anything)
            when(submissionRepository.findSolvedProblemIdsByUserIdAndContestId(10L, 100L)).thenReturn(Collections.emptySet());

            when(contestUtils.isUserContestCoordinator(10L, group)).thenReturn(false);
            when(contestUtils.checkIfContestRunning(contest)).thenReturn(true);
            when(contestMapper.toProblemSetResponse(relation1)).thenReturn(response);

            Set<ProblemSetResponse> result = contestService.viewProblemSet(100L);

            assertEquals(1, result.size());

            ProblemSetResponse item = result.iterator().next();

            assertNotNull(item.getTitle());
            assertNull(item.getOrigin());

            verify(contestUtils).validateAccessWithRole(contest, false);
        }
    }

    @Nested
    @DisplayName("viewAllContests()")
    class ViewAllContestsTests {

        @Test
        @DisplayName("should return mapped page of contests")
        void viewAllContests_shouldReturnMappedPage() {
            Page<Contest> contestPage = new PageImpl<>(List.of(contest), pageable, 1);

            when(contestRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable)))
                    .thenReturn(contestPage);
            when(contestMapper.toContestResponse(contest)).thenReturn(contestResponse);

            Page<ContestResponse> result = contestService.viewAllContests(
                    "Weekly",
                    "Group 1",
                    ContestStatus.SCHEDULED,
                    ContestType.CLASSICAL,
                    pageable
            );

            assertNotNull(result);
            assertEquals(1, result.getTotalElements());
            assertEquals("Weekly Contest", result.getContent().get(0).getTitle());

            verify(contestRepository).findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(pageable));
            verify(contestMapper).toContestResponse(contest);
        }
    }
}