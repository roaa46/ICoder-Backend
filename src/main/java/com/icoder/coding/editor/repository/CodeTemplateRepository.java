package com.icoder.coding.editor.repository;

import com.icoder.coding.editor.entity.CodeTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {
    Optional<CodeTemplate> findByIdAndUserId(Long id, Long userId);

    Page<CodeTemplate> findAllByUserId(Long userId, Pageable pageable);
}
