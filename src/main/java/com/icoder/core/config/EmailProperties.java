package com.icoder.core.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for email settings.
 * Centralizes all email-related configurations and provides validation at startup.
 *
 * Note: Sensitive values should be provided via environment variables, not hardcoded in properties files.
 */
@Configuration
@ConfigurationProperties(prefix = "app.email")
@Component
@Data
@Validated
public class EmailProperties {

    /**
     * Frontend URL for email links (e.g., invitation links)
     */
    @NotBlank(message = "Frontend URL must be configured")
    private String frontendUrl;

    /**
     * Default sender email address (usually from spring.mail.username)
     * Should be provided via environment variable for security
     */
    @NotBlank(message = "Default sender email must be configured")
    private String senderEmail;

    /**
     * Default sender name for emails
     */
    private String senderName = "ICoder";

    /**
     * Invitation email expiration time in hours
     */
    private Integer invitationExpirationHours = 24;
}

