package com.mygomi.backend.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class ScheduleResponseDto {
    private String id;              // "1", "2" 와 같은 고유 ID
    private String title;           // "가연성 쓰레기", "플라스틱"
    private LocalDate start;        // "2026-02-02" (FullCalendar는 이 필드를 날짜로 인식합니다)
    private boolean allDay;         // true
    private ExtendedProps extendedProps;

    @Getter
    @AllArgsConstructor
    public static class ExtendedProps {
        private String wasteType;   // "BURNABLE", "PLASTIC"
    }
}