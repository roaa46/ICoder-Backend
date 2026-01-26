package com.icoder.coding.editor.repository;

import com.icoder.coding.editor.entity.CodeTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {
    Optional<CodeTemplate> findByIdAndUserId(Long id, Long userId);

    Page<CodeTemplate> findAllByUserId(Long userId, Pageable pageable);

    boolean existsByUserIdAndLanguageIdAndEnabledTrue(Long userId, Integer languageId);

    @Modifying
    @Query("UPDATE CodeTemplate t SET t.enabled = false " +
            "WHERE t.user.id = :userId AND t.languageId = :langId AND t.enabled = true")
    void disableTemplatesByLanguage(Long userId, Integer langId);
}
