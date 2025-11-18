package com.unduck.paletteduck.domain.game.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DrawData {
    private String playerId;
    private String tool;        // "pen" | "eraser"
    private String color;       // hex color
    private int width;          // 1-3
    private List<Point> points; // 경로 좌표들

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {
        private double x;
        private double y;
    }
}
