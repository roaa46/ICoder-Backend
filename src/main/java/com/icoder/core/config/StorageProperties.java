package com.icoder.core.config;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

/**
 * Configuration properties for storage-related settings.
 * Centralizes all storage folder configurations and provides validation.
 */
@Configuration
@ConfigurationProperties(prefix = "storage")
@Component
@Data
@Validated
public class StorageProperties {

    @NotBlank(message = "Group picture folder must not be blank")
    private String groupPictureFolder;

    @NotBlank(message = "Profile picture folder must not be blank")
    private String profilePictureFolder;
}

