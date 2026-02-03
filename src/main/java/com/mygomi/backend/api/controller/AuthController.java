package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.*;
import com.mygomi.backend.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/signup")
    public CommonResponse<String> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return CommonResponse.success("회원가입이 완료되었습니다.");
    }

    @PostMapping("/login")
    public CommonResponse<TokenResponse> login(@RequestBody LoginRequest request) {
        TokenResponse token = authService.login(request);
        return CommonResponse.success(token);
    }
}