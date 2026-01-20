package com.icoder.coding.editor.service.implementation;

import com.icoder.coding.editor.dto.*;
import com.icoder.coding.editor.entity.CodeTemplate;
import com.icoder.coding.editor.mapper.TemplateMapper;
import com.icoder.coding.editor.repository.CodeTemplateRepository;
import com.icoder.coding.editor.service.interfaces.CodingEditorService;
import com.icoder.core.exception.TemplateException;
import com.icoder.user.management.entity.User;
import com.icoder.user.management.repository.UserRepository;
import com.icoder.user.management.service.interfaces.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class CodingEditorServiceImpl implements CodingEditorService {
    private final WebClient webClient;
    private final CodeTemplateRepository templateRepository;
    private final UserRepository userRepository;
    private final AuthenticationService service;
    private final TemplateMapper mapper;

    public CodingEditorServiceImpl(
            WebClient.Builder webClientBuilder,
            CodeTemplateRepository templateRepository,
            AuthenticationService service,
            UserRepository userRepository,
            TemplateMapper mapper,
            @Value("${judge0.api-url}") String apiUrl,
            @Value("${judge0.auth-key}") String authKey,
            @Value("${judge0.host}") String host) {

        this.templateRepository = templateRepository;
        this.service = service;
        this.userRepository = userRepository;
        this.mapper = mapper;

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

    private String decodeFromBase64(String input) {
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
        List<LanguageResponse> languages = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/languages").build())
                .retrieve()
                .bodyToMono(typeRef)
                .block();
        if (languages != null) {
            languages.forEach(lang -> {
                String monacoLang = JUDGE0_TO_MONACO_MAP.getOrDefault(lang.getId(), "plaintext");
                lang.setMonacoName(monacoLang);
            });
        }
        return languages;
    }

    private static final Map<Integer, String> JUDGE0_TO_MONACO_MAP = Map.ofEntries(
            // Python
            Map.entry(70, "python"),
            Map.entry(71, "python"),

            // C / C++
            Map.entry(48, "c"), Map.entry(49, "c"), Map.entry(50, "c"),
            Map.entry(52, "cpp"), Map.entry(53, "cpp"), Map.entry(54, "cpp"),

            // Java & C#
            Map.entry(62, "java"),
            Map.entry(51, "csharp"),

            // JavaScript & TypeScript
            Map.entry(63, "javascript"),
            Map.entry(74, "typescript"),

            // Others
            Map.entry(46, "shell"),
            Map.entry(60, "go"),
            Map.entry(73, "rust"),
            Map.entry(68, "php"),
            Map.entry(72, "ruby"),
            Map.entry(64, "lua"),
            Map.entry(67, "pascal"),
            Map.entry(57, "elixir"),
            Map.entry(58, "erlang"),
            Map.entry(61, "haskell"),
            Map.entry(69, "prolog")
    );

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

    private SubmissionResult getRawResult(String token) {
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

        getResultStatus(result);

        return result;
    }

    @Override
    public List<TokenResponse> submitBatchCode(BatchRunRequest request) {
        if (request.getTestInputs() == null || request.getTestInputs().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Test cases list cannot be empty for batch submission.");
        }

        List<BatchSubmissionRequestItem> batchItems = request.getTestInputs().stream()
                .map(test -> BatchSubmissionRequestItem.builder()
                        .languageId(request.getLanguageId())
                        .sourceCode(encodeToBase64(request.getSourceCode()))
                        .stdin(encodeToBase64(test.getInput()))
                        .expectedOutput(encodeToBase64(test.getExpectedOutput()))
                        .build())
                .collect(Collectors.toList());

        BatchSubmissionWrapper wrapper = BatchSubmissionWrapper.builder()
                .submissions(batchItems)
                .build();

        ParameterizedTypeReference<List<TokenResponse>> typeRef =
                new ParameterizedTypeReference<List<TokenResponse>>() {};

        List<TokenResponse> response = webClient.post()
                .uri(uriBuilder -> uriBuilder.path("/submissions/batch")
                        .queryParam("base64_encoded", "true")
                        .queryParam("wait", "false")
                        .build())
                .bodyValue(wrapper)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> clientResponse.bodyToMono(String.class)
                        .flatMap(error -> Mono.error(new ResponseStatusException(
                                clientResponse.statusCode(), "Judge0 Batch Submission Error: " + error))))
                .bodyToMono(typeRef)
                .block();

        if (response == null || response.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to get tokens from Judge0.");
        }

        return response;
    }

    @Override
    public BatchSubmissionResult processAndGetBatchResults(List<String> tokens) {

        String tokensString = String.join(",", tokens);

        BatchSubmissionResult responseWrapper = webClient.get()
                .uri(uriBuilder -> uriBuilder.path("/submissions/batch")
                        .queryParam("tokens", tokensString)
                        .queryParam("base64_encoded", "true")
                        .build())
                .retrieve()
                .bodyToMono(BatchSubmissionResult.class)
                .block();

        assert responseWrapper != null;
        List<SubmissionResult> rawResults = responseWrapper.getSubmissions();

        if (rawResults == null || rawResults.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No results found for provided tokens.");
        }

        for (SubmissionResult result : rawResults) {
            if (result != null)
                getResultStatus(result);
        }
        return new BatchSubmissionResult(rawResults);
    }

    @Transactional
    @Override
    public CodeTemplateResponse addTemplate(CodeTemplateRequest request) {
        Long userId = service.getCurrentUserId();
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));

        CodeTemplate template = new CodeTemplate();
        template.setTemplateName(request.getTemplateName());
        template.setLanguageId(request.getLanguageId());
        template.setCode(request.getCode());
        template.setEnabled(request.isEnabled());
        template.setUser(user);
        template.setCreatedAndUpdatedAt(Instant.now());

        templateRepository.save(template);

        CodeTemplateResponse response = mapper.toDTO(template);
        enhanceTemplateResponse(response, template.getLanguageId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public CodeTemplateResponse getTemplate(String  templateId) {
        Long userId = service.getCurrentUserId();
        Long id = Long.parseLong(templateId);

        CodeTemplate template = templateRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TemplateException("Template not found"));
        CodeTemplateResponse response = mapper.toDTO(template);

        response.setLanguageId(template.getLanguageId());
        enhanceTemplateResponse(response, template.getLanguageId());

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CodeTemplateResponse> getTemplates(int page) {
        Long userId = service.getCurrentUserId();
        Pageable pageable = PageRequest.of(page, 5, Sort.by("createdAndUpdatedAt").descending()); // 5 templates per page

        return templateRepository.findAllByUserId(userId, pageable)
                .map(template -> {
                    CodeTemplateResponse dto = mapper.toDTO(template);
                    enhanceTemplateResponse(dto, template.getLanguageId());
                    return dto;
                });
    }

    @Override
    @Transactional
    public CodeTemplateResponse editTemplate(String templateId, CodeTemplateRequest request) {
        Long userId = service.getCurrentUserId();
        Long id = Long.parseLong(templateId);

        CodeTemplate template = templateRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TemplateException("Template not found"));

        template.setTemplateName(request.getTemplateName());
        template.setCode(request.getCode());
        template.setLanguageId(request.getLanguageId());
        template.setEnabled(request.isEnabled());
        template.setCreatedAndUpdatedAt(Instant.now());

        templateRepository.save(template);

        CodeTemplateResponse response = mapper.toDTO(template);
        enhanceTemplateResponse(response, template.getLanguageId());

        return response;
    }

    @Override
    @Transactional
    public void deleteTemplate(String templateId) {
        Long userId = service.getCurrentUserId();

        Long id = Long.parseLong(templateId);
        CodeTemplate template = templateRepository
                .findByIdAndUserId(id, userId)
                .orElseThrow(() -> new TemplateException("Template not found"));

        templateRepository.delete(template);
    }

    private void getResultStatus(SubmissionResult result) {
        if (result.getStdout() != null) {
            result.setStdout(decodeFromBase64(result.getStdout()));
        }
        if (result.getStderr() != null) {
            result.setStderr(decodeFromBase64(result.getStderr()));
        }
        if (result.getCompile_output() != null) {
            result.setCompile_output(decodeFromBase64(result.getCompile_output()));
        }
    }

    private void enhanceTemplateResponse(CodeTemplateResponse response, Integer languageId) {
        if (languageId != null) {
            String monacoName = JUDGE0_TO_MONACO_MAP.getOrDefault(languageId, "plaintext");
            response.setMonacoName(monacoName);
        } else {
            response.setMonacoName("plaintext");
        }
    }
}
