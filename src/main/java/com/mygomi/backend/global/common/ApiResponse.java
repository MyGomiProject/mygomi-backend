package com.mygomi.backend.global.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ApiResponse<T> {
    private T data;
    private Meta meta; // 페이지네이션 정보 등을 담기 위해 확장 가능
    private ErrorResponse error;

    // 성공 시
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(data, null, null);
    }

    // 실패 시
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(null, null, new ErrorResponse(code, message));
    }

    @Getter
    @AllArgsConstructor
    public static class ErrorResponse {
        private String code;
        private String message;
    }

    @Getter
    @AllArgsConstructor
    public static class Meta {
        private int count;
    }
}