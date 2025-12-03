package com.unduck.paletteduck.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class RoomCreateRequest {
    @Builder.Default
    @JsonProperty("isPublic")
    private boolean isPublic = true; // 기본값: 공개방
}
