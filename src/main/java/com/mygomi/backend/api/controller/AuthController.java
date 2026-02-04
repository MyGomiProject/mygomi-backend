package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.*;
import com.mygomi.backend.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @Operation(summary = "회원가입", description = "새로운 사용자를 등록합니다.")
    @PostMapping("/signup")
    public CommonResponse<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return CommonResponse.success("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인하여 JWT 토큰을 발급받습니다.")
    @PostMapping("/login")
    public CommonResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request);
        return CommonResponse.success(token);
    }
}