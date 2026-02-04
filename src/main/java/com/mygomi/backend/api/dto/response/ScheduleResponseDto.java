package com.mygomi.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ScheduleResponseDto {
    private LocalDate date;        // 수거 날짜 (2026-02-05)
    private String wasteName;      // 쓰레기 이름 ("타는 쓰레기")
    private String wasteType;      // 쓰레기 타입 코드 ("BURNABLE") -> 프론트 아이콘용
    private String note;           // 비고 ("연말연시 휴무" 등)
}