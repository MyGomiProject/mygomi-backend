package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.ItemResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.domain.user.UserRepository;
import com.mygomi.backend.service.ItemService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Item Search", description = "품목(쓰레기) 검색 API")
@RestController
@RequestMapping("/api/items")
@RequiredArgsConstructor
public class ItemController {

    private final ItemService itemService;
    private final UserRepository userRepository;

    @Operation(summary = "쓰레기 분류 검색", description = "로그인한 유저의 지역(구)을 기준으로 쓰레기 배출 방법을 검색합니다.")
    @GetMapping("/search")
    public ResponseEntity<List<ItemResponseDto>> search(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam String keyword
    ) {
        // 1. 로그인 유저 확인
        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 서비스 호출 (유저 ID 전달)
        return ResponseEntity.ok(itemService.searchItems(user.getId(), keyword));
    }
}