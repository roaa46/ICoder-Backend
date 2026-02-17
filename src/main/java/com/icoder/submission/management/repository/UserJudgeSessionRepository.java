package com.icoder.submission.management.repository;

import com.icoder.problem.management.enums.OJudgeType;
import com.icoder.submission.management.entity.UserJudgeSession;
import com.icoder.user.management.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJudgeSessionRepository extends JpaRepository<UserJudgeSession, Long> {
    Optional<UserJudgeSession> findByUserAndJudgeType(User user, OJudgeType judge);
}
