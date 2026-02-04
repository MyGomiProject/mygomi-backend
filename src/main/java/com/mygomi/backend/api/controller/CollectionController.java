package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.CalendarResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.domain.user.UserRepository;
import com.mygomi.backend.service.CalendarService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Collection Calendar", description = "수거 캘린더 API")
@RestController
@RequestMapping("/api/collection")
@RequiredArgsConstructor
public class CollectionController {

    private final CalendarService calendarService;
    private final UserRepository userRepository;

    @Operation(summary = "월간 수거 일정 조회", description = "내 대표 주소를 기준으로 해당 월의 수거 일정을 반환합니다.")
    @GetMapping("/calendar")
    public ResponseEntity<List<CalendarResponseDto>> getCalendar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month) {

        Long userId = getUserIdFromToken(userDetails);
        return ResponseEntity.ok(calendarService.getMyCollectionCalendar(userId, year, month));
    }

    private Long getUserIdFromToken(UserDetails userDetails) {
        if (userDetails == null) throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        String email = userDetails.getUsername();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("유저 없음: " + email));
        return user.getId();
    }
}