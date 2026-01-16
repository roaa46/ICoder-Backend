package com.icoder.user.management.repository;

import com.icoder.user.management.entity.Token;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    @Query("""
            select t from Token t
            inner join User u
            on t.user.id = u.id
            where u.id = :userId
            and (t.isExpired = false or t.isRevoked = false )
            """)
    List<Token> findAllValidTokens(Long userId);

    Optional<Token> findByToken(String token);

    @Transactional
    int deleteAllByCreatedAtBefore(LocalDateTime dateTime);
}
