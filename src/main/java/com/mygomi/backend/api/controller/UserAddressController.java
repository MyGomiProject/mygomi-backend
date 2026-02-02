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

@Tag(name = "User Address", description = "사용자 동네(주소) 관리 API")
@RestController
@RequestMapping("/api/user-addresses") // back.md의 user_addresses CRUD에 맞춘 URL
@RequiredArgsConstructor
public class UserAddressController {

    private final AddressService addressService;

    @Operation(summary = "내 동네 설정 (등록/수정)", description = "최초 등록이면 생성, 이미 있으면 수정합니다.")
    @PostMapping
    public ResponseEntity<AddressResponseDto> setAddress(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody AddressRequestDto request) {

        // TODO: Security 설정에 따라 ID 가져오는 방식 확인 필요 (여기선 임시로 1L)
        Long userId = 1L;

        AddressResponseDto response = addressService.saveOrUpdateAddress(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 동네 조회", description = "현재 설정된 주소를 가져옵니다.")
    @GetMapping
    public ResponseEntity<AddressResponseDto> getAddress(
            @AuthenticationPrincipal UserDetails user) {

        Long userId = 1L;
        AddressResponseDto response = addressService.getAddress(userId);
        return ResponseEntity.ok(response);
    }
}