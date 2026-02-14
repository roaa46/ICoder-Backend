package com.icoder.submission.management.dto;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.core.utils.UppercaseEnumDeserializer;
import com.icoder.problem.management.enums.OJudgeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class SubmissionCreateRequest {
        @NotBlank
        private String problemCode;
        @NotBlank
        private String code;
        @NotBlank
        private String language;
        @NotNull
        @JsonDeserialize(using = UppercaseEnumDeserializer.class)
        private OJudgeType onlineJudge;

        private Long contestId; // optional
}
