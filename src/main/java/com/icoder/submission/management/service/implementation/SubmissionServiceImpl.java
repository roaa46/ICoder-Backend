package com.icoder.submission.management.service.implementation;

import com.icoder.coding.editor.utils.LanguageMatcher;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.core.utils.SecurityUtils;
import com.icoder.problem.management.entity.Problem;
import com.icoder.problem.management.repository.ProblemRepository;
import com.icoder.submission.management.dto.LanguageOptionResponse;
import com.icoder.submission.management.dto.SubmissionCreateRequest;
import com.icoder.submission.management.dto.SubmissionCreateResponse;
import com.icoder.submission.management.entity.Submission;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.mapper.SubmissionMapper;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.icoder.submission.management.service.interfaces.SubmissionService;
import com.icoder.submission.management.utils.SubmissionUtils;
import com.icoder.user.management.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmissionServiceImpl implements SubmissionService {
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final SubmissionProcessor submissionProcessor;
    private final SubmissionMapper submissionMapper;
    private final SecurityUtils securityUtils;
    private final SubmissionUtils submissionUtils;


    public List<LanguageOptionResponse> getCsesLanguages() {
        List<String> csesOptions = List.of(
                "Assembly", "C++11", "C++17", "C++20",
                "CPython2", "CPython3", "Haskell", "Java",
                "Node.js", "Pascal", "PyPy2", "PyPy3",
                "Ruby", "Rust", "Scala"
        );

        return csesOptions.stream()
                .filter(lang -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(lang)))
                .map(lang -> LanguageOptionResponse.builder()
                        .id(lang)
                        .displayName(lang)
                        .build())
                .toList();
    }

    public List<LanguageOptionResponse> getCodeforcesLanguages() {
        Map<Integer, String> cfRawData = Map.ofEntries(
                Map.entry(79, "C# 10, .NET SDK 6.0"), Map.entry(96, "C# 13, .NET SDK 9"),
                Map.entry(65, "C# 8, .NET Core 3.1"), Map.entry(9, "C# Mono 6.8"),
                Map.entry(54, "GNU G++17 7.3.0"), Map.entry(89, "GNU G++20 13.2 (64 bit, winlibs)"),
                Map.entry(91, "GNU G++23 14.2 (64 bit, msys2)"), Map.entry(43, "GNU GCC C11 5.1.0"),
                Map.entry(87, "Java 21 64bit"), Map.entry(36, "Java 8 32bit"),
                Map.entry(83, "Kotlin 1.7.20"), Map.entry(88, "Kotlin 1.9.21"),
                Map.entry(99, "Kotlin 2.2.0"), Map.entry(20, "Scala 2.12.8"),
                Map.entry(7, "Python 2.7.18"), Map.entry(31, "Python 3.13.2"),
                Map.entry(40, "PyPy 2.7.13 (7.3.0)"), Map.entry(41, "PyPy 3.6.9 (7.3.0)"),
                Map.entry(70, "PyPy 3.10 (7.3.15, 64bit)"), Map.entry(34, "JavaScript V8 4.8.0"),
                Map.entry(55, "Node.js 15.8.0 (64bit)"), Map.entry(75, "Rust 1.89.0 (2021)"),
                Map.entry(98, "Rust 1.89.0 (2024)"), Map.entry(32, "Go 1.22.2"),
                Map.entry(6, "PHP 8.1.7"), Map.entry(67, "Ruby 3.2.2"),
                Map.entry(4, "Free Pascal 3.2.2"), Map.entry(51, "PascalABC.NET 3.8.3"),
                Map.entry(3, "Delphi 7"), Map.entry(12, "Haskell GHC 8.10.1"),
                Map.entry(28, "D DMD32 v2.105.0"), Map.entry(97, "F# 9, .NET SDK 9"),
                Map.entry(19, "OCaml 4.02.1"), Map.entry(13, "Perl 5.20.1")
        );

        return cfRawData.entrySet().stream()
                .filter(entry -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(entry.getValue())))
                .sorted(Map.Entry.comparingByValue())
                .map(entry -> LanguageOptionResponse.builder()
                        .id(entry.getKey().toString())
                        .displayName(entry.getValue())
                        .build())
                .toList();
    }

    public List<LanguageOptionResponse> getAtCoderLanguages() {
        Map<Integer, String> atCoderRawData = Map.ofEntries(
                Map.entry(6017, "C++23 (GCC 15.2.0)"), Map.entry(6116, "C++23 (Clang 21.1.0)"),
                Map.entry(6014, "C23 (GCC 14.2.0)"), Map.entry(6054, "C++ IOI-Style (GCC 14.2.0)"),
                Map.entry(6056, "Java 24 (OpenJDK 24.0.2)"), Map.entry(6062, "Kotlin (Kotlin/JVM 2.2.10)"),
                Map.entry(6090, "Scala (Dotty 3.7.2)"), Map.entry(6082, "Python (CPython 3.13.7)"),
                Map.entry(6083, "Python (PyPy 3.11-v7.3.20)"), Map.entry(6059, "JavaScript (Node.js 22.19.0)"),
                Map.entry(6102, "TypeScript 5.9 (Node.js 22.19.0)"), Map.entry(6015, "C# 13.0 (.NET 9.0.8)"),
                Map.entry(6088, "Rust (rustc 1.89.0)"), Map.entry(6051, "Go (go 1.25.1)"),
                Map.entry(6077, "PHP (PHP 8.4.12)"), Map.entry(6087, "Ruby 3.4 (ruby 3.4.5)"),
                Map.entry(6095, "Swift 6.2")
        );

        return atCoderRawData.entrySet().stream()
                .filter(entry -> !"plaintext".equals(LanguageMatcher.resolveMonacoName(entry.getValue())))
                .map(entry -> LanguageOptionResponse.builder()
                        .id(entry.getKey().toString())
                        .displayName(entry.getValue())
                        .build())
                .toList();
    }

    @Override
    @Transactional
    public SubmissionCreateResponse submit(SubmissionCreateRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        log.info("Received submission request for problem: {} by user: {}",
                request.getProblemCode(), currentUser.getHandle());

        Problem problem = problemRepository.findByProblemCodeAndOnlineJudge(request.getProblemCode(), request.getOnlineJudge())
                .orElseThrow(() -> new ResourceNotFoundException("Problem not found"));

        Submission submission = Submission.builder()
                .submissionCode(request.getCode())
                .language(request.getLanguage())
                .onlineJudge(problem.getOnlineJudge())
                .problem(problem)
                .user(currentUser)
                .status(SubmissionStatus.CREATED)
                .verdict(SubmissionVerdict.PENDING)
                .build();

        submission = submissionRepository.save(submission);
        submissionUtils.updateUserProblemRelation(currentUser, problem);

        final Long submissionId = submission.getId();
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                log.info("Transaction committed successfully. Starting processor for ID: {}", submissionId);
                submissionProcessor.process(submissionId);
            }
        });

        return submissionMapper.toDTO(submission);
    }

    @Override
    public SubmissionCreateResponse getSubmission(Long id) {
        Submission submission = submissionRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Submission not found"));
        return submissionMapper.toDTO(submission);
    }
}