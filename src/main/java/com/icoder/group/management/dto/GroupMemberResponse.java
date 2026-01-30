package com.icoder.group.management.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.icoder.group.management.enums.GroupRole;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class GroupMemberResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Long userId;
    private String handle;
    private String nickname;
    private String pictureUrl;
    private boolean verified;
    private GroupRole role;
}
