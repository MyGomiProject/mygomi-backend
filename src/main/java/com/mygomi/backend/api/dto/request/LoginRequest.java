package com.mygomi.backend.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "로그인 요청 DTO")
public class LoginRequest {

    @Schema(description = "이메일", example = "user@example.com")
    private String email;

    @Schema(description = "비밀번호", example = "password123!")
    private String password;
}