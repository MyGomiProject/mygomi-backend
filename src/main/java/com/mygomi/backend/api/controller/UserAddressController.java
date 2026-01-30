package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Address", description = "사용자 동네(주소) 관리 API")
@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final AddressService addressService;

    @Operation(summary = "내 동네 설정 (등록/수정)", description = "최초 등록이면 생성, 이미 있으면 수정합니다.")
    @PostMapping
    public ResponseEntity<AddressResponseDto> setAddress(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AddressRequestDto request) {

        // TODO: 실제 토큰에서 userId 추출하는 로직으로 변경 필요 (지금은 하드코딩 1L)
        Long userId = 1L;

        AddressResponseDto response = addressService.saveOrUpdateAddress(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 동네 조회", description = "등록된 주소 목록을 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAddresses(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = 1L; // 임시 하드코딩

        // [수정 포인트] Service의 getMyAddresses(List 반환)와 맞춤
        List<AddressResponseDto> response = addressService.getMyAddresses(userId);

        return ResponseEntity.ok(response);
    }
}