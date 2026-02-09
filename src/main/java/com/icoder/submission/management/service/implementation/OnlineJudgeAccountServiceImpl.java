package com.icoder.submission.management.service.implementation;

import com.icoder.core.exception.OnlineJudgeException;
import com.icoder.core.exception.ResourceNotFoundException;
import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.OnlineJudgeAccount;
import com.icoder.submission.management.repository.OnlineJudgeAccountRepository;
import com.icoder.submission.management.service.interfaces.OnlineJudgeAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class OnlineJudgeAccountServiceImpl implements OnlineJudgeAccountService {
    private final OnlineJudgeAccountRepository repository;

    @Override
    public OnlineJudgeAccount getAvailableAccount(OJudgeType judgeType) {
        OnlineJudgeAccount account = repository.findFirstByJudgeTypeAndIsActiveTrueAndInUseFalseOrderByLastUsedAtAsc(judgeType)
                .orElseThrow(() -> new ResourceNotFoundException("No available bot accounts for " + judgeType));

        account.setInUse(true);
        return repository.save(account);
    }

    @Override
    @Transactional
    public void releaseAccount(Long accountId) {
        repository.findById(accountId).ifPresent(account -> {
            account.setInUse(false);
            account.setLastUsedAt(Instant.now());
            repository.save(account);
        });
    }

    @Override
    @Transactional
    public void addAccount(OnlineJudgeAccount account) {
        boolean exists = repository.existsByUsernameAndJudgeType(
                account.getUsername(), account.getJudgeType());

        if (exists) {
            throw new OnlineJudgeException("Account already exists for this Judge");
        }

        account.setInUse(false);
        account.setActive(true);
        account.setLastUsedAt(Instant.now());

        repository.save(account);
    }

    @Override
    @Transactional
    public void deleteAccount(Long id) {
        OnlineJudgeAccount account = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found"));

        if (account.isInUse()) {
            throw new OnlineJudgeException("Cannot delete account while it is in use");
        }

        repository.delete(account);
    }
}
