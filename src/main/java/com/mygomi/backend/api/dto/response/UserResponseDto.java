package com.mygomi.backend.api.dto.response;

import com.mygomi.backend.domain.user.Role;
import com.mygomi.backend.domain.user.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class UserResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private Role role;
    private LocalDateTime createdAt;
    private List<AddressResponseDto> addresses; // 사용자 주소 목록

    public static UserResponseDto from(User user, List<AddressResponseDto> addresses) {
        return UserResponseDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .role(user.getRole())
                .createdAt(user.getCreatedAt())
                .addresses(addresses)
                .build();
    }
}

