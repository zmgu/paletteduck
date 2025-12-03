package com.unduck.paletteduck.domain.room.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.unduck.paletteduck.domain.game.dto.GameSettings;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RoomInfo {
    private String roomId;
    private String inviteCode;
    private List<RoomPlayer> players;
    private GameSettings settings;
    private RoomStatus status = RoomStatus.WAITING;

    @JsonProperty("isPublic")
    @Getter(AccessLevel.NONE)  // Lombok이 getter를 생성하지 않도록
    @Setter(AccessLevel.NONE)  // Lombok이 setter를 생성하지 않도록
    private boolean isPublic = true; // 기본값: 공개방

    // 명시적으로 getter/setter 작성
    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}