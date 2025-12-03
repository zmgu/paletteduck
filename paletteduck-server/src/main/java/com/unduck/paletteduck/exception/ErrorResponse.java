package com.unduck.paletteduck.exception;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 에러 응답 DTO
 */
@Getter
@Builder
@AllArgsConstructor
public class ErrorResponse {
    private final String code;
    private final String message;
    @Builder.Default
    private final LocalDateTime timestamp = LocalDateTime.now();
}
