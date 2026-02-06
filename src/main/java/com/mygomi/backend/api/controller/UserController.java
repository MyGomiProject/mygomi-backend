package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.UserResponseDto;
import com.mygomi.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User", description = "사용자 정보 API")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "현재 로그인한 사용자 정보 조회", description = "JWT 토큰을 통해 현재 로그인한 사용자의 정보를 반환합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }

        String email = userDetails.getUsername();
        UserResponseDto response = userService.getCurrentUser(email);

        return ResponseEntity.ok(response);
    }
}

