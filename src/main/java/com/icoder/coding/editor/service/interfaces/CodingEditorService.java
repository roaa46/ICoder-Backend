package com.icoder.coding.editor.service.interfaces;

import com.icoder.coding.editor.dto.*;

import java.util.List;

public interface CodingEditorService {
    TokenResponse submitCode(SubmissionRequest request);

    LanguageResponse getLanguage(int id);

    List<LanguageResponse> getLanguages();

    SubmissionResult processAndGetResult(String token);

    List<TokenResponse> submitBatchCode(BatchRunRequest request);

    BatchSubmissionResult processAndGetBatchResults(List<String> tokens);
}
