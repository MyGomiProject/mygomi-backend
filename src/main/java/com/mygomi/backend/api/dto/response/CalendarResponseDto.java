package com.mygomi.backend.api.dto.response;

import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
public class CalendarResponseDto {
    private String title;   // 예: "타는 쓰레기"
    private LocalDate start; // 예: "2026-02-02"
    private String color;    // 예: "#FF5733"
    private boolean allDay;  // true
}
