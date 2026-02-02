package com.icoder.invitation.management.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.core.utils.UppercaseEnumDeserializer;
import com.icoder.invitation.management.enums.InvitationResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class RespondToInvitationRequest {
    @NotNull
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private InvitationResponse response;
    @NotBlank
    private String Token;
}
