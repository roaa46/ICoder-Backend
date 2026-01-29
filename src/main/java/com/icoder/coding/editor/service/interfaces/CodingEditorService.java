package com.icoder.coding.editor.service.interfaces;

import com.icoder.coding.editor.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

public interface CodingEditorService {
    TokenResponse submitCode(SubmissionRequest request);

    LanguageResponse getLanguage(int id);

    List<LanguageResponse> getLanguages();

    SubmissionResult processAndGetResult(String token);

    List<TokenResponse> submitBatchCode(BatchRunRequest request);

    BatchSubmissionResult processAndGetBatchResults(List<String> tokens);

    CodeTemplateResponse addTemplate(CodeTemplateRequest request);

    CodeTemplateResponse toggleTemplate(Long templateId, boolean force);

    CodeTemplateResponse getTemplate(String  templateId);

    Page<CodeTemplateResponse> getTemplates(int page);

    CodeTemplateResponse editTemplate(String templateId, CodeTemplateRequest request);

    void deleteTemplate(String templateId);
}
