package com.unduck.paletteduck.controller;

import com.unduck.paletteduck.dto.RoomInfo;
import com.unduck.paletteduck.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketRoomController {

    private final RoomService roomService;

    @MessageMapping("/room/{roomId}/update")
    @SendTo("/topic/room/{roomId}")
    public RoomInfo updateRoom(@DestinationVariable String roomId) {
        return roomService.getRoomInfo(roomId);
    }
}