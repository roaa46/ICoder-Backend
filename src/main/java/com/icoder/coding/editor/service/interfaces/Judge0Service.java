package com.icoder.coding.editor.service.interfaces;

import com.icoder.coding.editor.dto.*;

import java.util.List;

public interface Judge0Service {
    TokenResponse submitCode(SubmissionRequest request);

    SubmissionResult getRawResult(String token);

    LanguageResponse getLanguage(int id);

    List<LanguageResponse> getLanguages();

    SubmissionResult processAndGetResult(String token);
}
