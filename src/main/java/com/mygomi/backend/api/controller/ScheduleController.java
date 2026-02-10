package com.mygomi.backend.api.controller;

import com.mygomi.backend.api.dto.response.CommonResponse;
import com.mygomi.backend.api.dto.response.ScheduleResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
import com.mygomi.backend.service.ScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Schedule", description = "쓰레기 수거 일정 API")
@RestController
@RequestMapping("/api/schedules")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final UserRepository userRepository;

    @Operation(summary = "월간 수거 일정 조회", description = "년/월을 입력하지 않으면 '이번 달' 일정을 보여줍니다.")
    @GetMapping
    public ResponseEntity<CommonResponse<List<ScheduleResponseDto>>> getSchedules(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month
    ) {
        // 1. 유저 조회
        if (userDetails == null) {
            throw new UsernameNotFoundException("로그인 정보가 없습니다.");
        }
        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 2. 📅 날짜 기본값 설정 (입력 없으면 '오늘' 기준)
        if (year == null) year = LocalDate.now().getYear();
        if (month == null) month = LocalDate.now().getMonthValue();

        // 3. 서비스 호출
        List<ScheduleResponseDto> schedules = scheduleService.getMonthlySchedule(user.getId(), year, month);
        return ResponseEntity.ok(CommonResponse.success(schedules)); // "data": [...] 형태로 나감
    }
}