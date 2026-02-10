package com.icoder.submission.management.utils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.icoder.submission.management.entity.BotAccount;
import com.icoder.submission.management.enums.SubmissionStatus;
import com.icoder.submission.management.enums.SubmissionVerdict;
import com.icoder.submission.management.repository.BotAccountRepository;
import com.icoder.submission.management.repository.SubmissionRepository;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.options.Cookie;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubmissionUtils {
    private final SubmissionRepository submissionRepository;
    private final BotAccountRepository accountRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public void handleFailure(Long id) {
        submissionRepository.findById(id).ifPresent(s -> {
            s.setStatus(SubmissionStatus.FAILED);
            s.setVerdict(SubmissionVerdict.FAILED);
            submissionRepository.save(s);
        });
    }

    public void loadCookies(BrowserContext context, BotAccount account) {
        if (account.getCookies() != null && !account.getCookies().isBlank()) {
            try {
                List<Map<String, Object>> cookieMaps = mapper.readValue(account.getCookies(), new TypeReference<>() {
                });

                for (Map<String, Object> map : cookieMaps) {
                    context.addCookies(List.of(new Cookie((String) map.get("name"), (String) map.get("value"))
                            .setDomain((String) map.get("domain"))
                            .setPath((String) map.get("path"))
                            .setExpires(((Number) map.get("expires")).doubleValue())
                            .setHttpOnly((Boolean) map.get("httpOnly"))
                            .setSecure((Boolean) map.get("secure"))
                            .setSameSite(com.microsoft.playwright.options.SameSiteAttribute.valueOf((String) map.get("sameSite")))
                    ));
                }
                log.info("Cookies loaded successfully for: {}", account.getUsername());
            } catch (Exception e) {
                log.error("Detailed Cookie Loading Error: {}", e.getMessage());
            }
        }
    }

    public void saveCookies(BrowserContext context, BotAccount account) {
        try {
            List<Cookie> cookies = context.cookies();
            String cookiesJson = mapper.writeValueAsString(cookies);
            account.setCookies(cookiesJson);
            accountRepository.save(account);
        } catch (Exception e) {
            log.error("Failed to extract cookies: {}", e.getMessage());
        }
    }
}
