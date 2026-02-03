package com.mygomi.backend.api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CommonResponse<T> {
    private T data;
    private Meta meta;

    @Getter
    @AllArgsConstructor
    public static class Meta {
        private String timestamp;
    }

    public static <T> CommonResponse<T> success(T data) {
        return new CommonResponse<>(data, new Meta(LocalDateTime.now().toString()));
    }
}