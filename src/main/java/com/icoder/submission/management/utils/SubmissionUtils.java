package com.icoder.submission.management.utils;

import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubmissionUtils {
    private final SubmissionRepository submissionRepository;

    public void handleFailure(Long id) {
        submissionRepository.findById(id).ifPresent(s -> {
            s.setStatus(SubmissionStatus.FAILED);
            s.setVerdict(SubmissionVerdict.FAILED);
            submissionRepository.save(s);
        });
    }
}
