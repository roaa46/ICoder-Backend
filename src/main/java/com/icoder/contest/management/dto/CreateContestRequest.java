package com.icoder.contest.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.icoder.contest.management.enums.ContestOpenness;
import com.icoder.contest.management.enums.ContestType;
import com.icoder.core.utils.LowercaseEnumSerializer;
import com.icoder.core.utils.UppercaseEnumDeserializer;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateContestRequest {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long groupId;

    @NotBlank
    private String title;

    private String description;

    private Instant beginTime;

    @NotBlank
    @Pattern(
            regexp = "^\\d{1,3}:\\d{2}:\\d{2}$",
            message = "length must be in h:mm:ss format"
    )
    private String length;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private ContestType contestType;

    @JsonSerialize(using = LowercaseEnumSerializer.class)
    @JsonDeserialize(using = UppercaseEnumDeserializer.class)
    private ContestOpenness contestOpenness;

    private String password;

    private Boolean historyRank;

    private Set<ProblemSetRequest> problemSet;
}
