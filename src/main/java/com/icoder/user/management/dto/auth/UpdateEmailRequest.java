package com.icoder.user.management.dto.auth;


import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UpdateEmailRequest {
    @NotBlank
    private String newEmail;

    @NotBlank
    private String currentPassword;
}
