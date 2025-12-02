package com.icoder.coding.editor.service.implementation;

import com.icoder.coding.editor.dto.LanguageResponse;
import com.icoder.coding.editor.dto.SubmissionRequest;
import com.icoder.coding.editor.dto.SubmissionResult;
import com.icoder.coding.editor.dto.TokenResponse;
import com.icoder.coding.editor.service.interfaces.Judge0Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;

import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class Judge0ServiceImpl implements Judge0Service {
    private WebClient webClient;

    public Judge0ServiceImpl(
            WebClient.Builder webClientBuilder,
            @Value("${judge0.api-url}") String apiUrl,
            @Value("${judge0.auth-key}") String authKey,
            @Value("${judge0.host}") String host) {

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-RapidAPI-Key", authKey);
        headers.set("X-RapidAPI-Host", host);
        headers.set("Content-Type", "application/json");

        this.webClient = webClientBuilder
                .baseUrl(apiUrl)
                .defaultHeaders(h -> h.addAll(headers))
                .build();
    }

    private String encodeToBase64(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return Base64.getEncoder().encodeToString(input.getBytes());
    }

    public String decodeFromBase64(String input) {
        if (input == null || input.isEmpty()) {
            return "";
        }
        return new String(Base64.getDecoder().decode(input));
    }

    @Override
    public LanguageResponse getLanguage(int id) {
        List<LanguageResponse> allLanguages = getLanguages();

        return allLanguages.stream()
                .filter(lang -> lang.getId() != null && lang.getId() == id)
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Language with ID " + id + " not found.")
                );
    }

    @Override
    public List<LanguageResponse> getLanguages() {
        ParameterizedTypeReference<List<LanguageResponse>> typeRef =
                new ParameterizedTypeReference<>() {
                };
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/languages")
                        .build())
                .retrieve()
                .bodyToMono(typeRef)
                .block();
    }

    @Override
    public TokenResponse submitCode(SubmissionRequest request) {

        String encodedSource = encodeToBase64(request.getSourceCode());
        String encodedStdin = encodeToBase64(request.getStdin());

        Map<String, Object> requestBody = Map.of(
                "source_code", encodedSource,
                "language_id", request.getLanguageId(),
                "stdin", encodedStdin
        );

        return webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/submissions")
                        .queryParam("base64_encoded", "true")
                        .queryParam("wait", "false")
                        .build())
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(TokenResponse.class)
                .block();
    }

    @Override
    public SubmissionResult getRawResult(String token) {
        return webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/submissions/{token}")
                        .queryParam("base64_encoded", "true")
                        .queryParam("fields", "status,stdout,stderr,compile_output,time,memory")
                        .build(token))
                .retrieve()
                .bodyToMono(SubmissionResult.class)
                .block();
    }

    @Override
    public SubmissionResult processAndGetResult(String token) {

        SubmissionResult result = getRawResult(token);

        if (result == null || result.getStatus() == null) {
            return new SubmissionResult();
        }

        if (result.getStdout() != null) {
            result.setStdout(decodeFromBase64(result.getStdout()));
        }
        if (result.getStderr() != null) {
            result.setStderr(decodeFromBase64(result.getStderr()));
        }
        if (result.getCompile_output() != null) {
            result.setCompile_output(decodeFromBase64(result.getCompile_output()));
        }

        return result;
    }
}
