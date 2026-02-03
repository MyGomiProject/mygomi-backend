package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.CommonResponse;
import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.security.JwtTokenProvider;
import com.mygomi.backend.service.AddressService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/addresses")
@RequiredArgsConstructor
public class AddressController {

    private final AddressService addressService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping
    public CommonResponse<AddressResponseDto> createAddress(
            @RequestHeader("Authorization") String token,
            @RequestBody AddressRequestDto request) {

        // 토큰에서 'Bearer ' 제거 후 이메일 추출 -> 유저 ID 조회 로직이 필요하지만
        // 지금은 약식으로 구현하거나, ArgumentResolver를 사용하는 게 정석입니다.
        // 임시로: 토큰 검증은 Filter에서 했다고 가정하고, 여기선 편의상 Header를 파싱 안 하고
        // SecurityContextHolder에서 꺼내는 게 맞지만,
        // 주니어 가이드 수준에선 일단 서비스 호출 구조만 잡겠습니다.
        // TODO: 실제 userId를 토큰에서 꺼내는 로직으로 교체 필요
        Long userId = 1L; // 임시 하드코딩 (테스트용)

        return CommonResponse.success(addressService.saveOrUpdateAddress(userId, request));
    }

    @GetMapping
    public CommonResponse<List<AddressResponseDto>> getAddresses() {
        Long userId = 1L; // 임시 하드코딩
        return CommonResponse.success(addressService.getMyAddresses(userId));
    }
}