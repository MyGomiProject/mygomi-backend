package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.UserResponseDto;
import com.mygomi.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
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

    @Operation(summary = "닉네임 수정", description = "사용자의 닉네임을 변경합니다.")
    @PatchMapping("/me/nickname")
    public ResponseEntity<UserResponseDto> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody java.util.Map<String, String> request) {

        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }

        String email = userDetails.getUsername();
        String newNickname = request.get("nickname");

        if (newNickname == null || newNickname.isBlank()) {
            throw new IllegalArgumentException("닉네임을 입력해주세요.");
        }

        UserResponseDto response = userService.updateNickname(email, newNickname);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "비밀번호 변경", description = "현재 비밀번호 확인 후 새 비밀번호로 변경합니다.")
    @PatchMapping("/me/password")
    public ResponseEntity<String> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody java.util.Map<String, String> request) {

        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }

        String email = userDetails.getUsername();
        String currentPassword = request.get("currentPassword");
        String newPassword = request.get("newPassword");

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("현재 비밀번호를 입력해주세요.");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("새 비밀번호를 입력해주세요.");
        }

        userService.updatePassword(email, currentPassword, newPassword);
        return ResponseEntity.ok("비밀번호가 변경되었습니다.");
    }
}

