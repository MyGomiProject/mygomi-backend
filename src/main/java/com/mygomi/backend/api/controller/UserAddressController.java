package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.service.AddressService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "User Address", description = "사용자 동네(주소) 관리 API")
@RestController
@RequestMapping("/api/user-addresses")
@RequiredArgsConstructor
public class UserAddressController {

    private final AddressService addressService;
    private final UserRepository userRepository; // [추가] 유저 조회를 위해 필요

    @Operation(summary = "내 동네 설정 (등록/수정)", description = "최초 등록이면 생성, 이미 있으면 수정합니다.")
    @PostMapping
    public ResponseEntity<AddressResponseDto> setAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody AddressRequestDto request) {

        // [수정 완료] 토큰에서 이메일 추출 -> DB 조회 -> ID 획득
        Long userId = getUserIdFromToken(userDetails);

        // 토큰 올바르게 받아서 로그인 했는지 확인용
        // System.out.println("🔥 현재 로그인한 유저 ID: " + userId);

        AddressResponseDto response = addressService.saveOrUpdateAddress(userId, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "내 동네 조회", description = "등록된 주소 목록을 가져옵니다.")
    @GetMapping
    public ResponseEntity<List<AddressResponseDto>> getAddresses(
            @AuthenticationPrincipal UserDetails userDetails) {

        // [수정 완료] 토큰 기반 ID 조회
        Long userId = getUserIdFromToken(userDetails);

        List<AddressResponseDto> response = addressService.getMyAddresses(userId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "대표 주소 변경", description = "선택한 주소를 대표 주소로 설정합니다 (배민 방식)")
    @PatchMapping("/{addressId}/primary")
    public ResponseEntity<AddressResponseDto> changePrimaryAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        AddressResponseDto response = addressService.changePrimaryAddress(userId, addressId);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "주소 삭제", description = "등록된 주소를 삭제합니다 (대표 주소가 1개만 남은 경우 삭제 불가)")
    @DeleteMapping("/{addressId}")
    public ResponseEntity<String> deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long addressId) {
        
        Long userId = getUserIdFromToken(userDetails);
        
        addressService.deleteAddress(userId, addressId);
        return ResponseEntity.ok("주소가 삭제되었습니다.");
    }

    // 🕵️‍♂️ 편의 메서드: 토큰 정보(UserDetails)로 실제 유저 ID 찾기
    private Long getUserIdFromToken(UserDetails userDetails) {
        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }

        // 토큰에 들어있는 'Subject'(이메일)을 가져옴
        String email = userDetails.getUsername();

        // 이메일로 DB에서 유저를 찾음 (없으면 에러)
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 사용자입니다. email=" + email));

        return user.getId();
    }
}