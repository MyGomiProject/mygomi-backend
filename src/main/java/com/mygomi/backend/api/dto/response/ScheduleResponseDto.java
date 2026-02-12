package com.mygomi.backend.api.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
@Schema(description = "수거 일정 응답 DTO")
public class ScheduleResponseDto {

    @Schema(description = "일정 고유 ID", example = "1")
    private String id;              // "1", "2" 와 같은 고유 ID

    @Schema(description = "일정 제목 (쓰레기 이름)", example = "타는 쓰레기")
    private String title;           // "가연성 쓰레기", "플라스틱"

    @Schema(description = "시작 날짜 (YYYY-MM-DD)", example = "2026-02-15")
    private LocalDate start;        // "2026-02-02" (FullCalendar는 이 필드를 날짜로 인식합니다)

    @Schema(description = "하루 종일 여부 (항상 true)", example = "true")
    private boolean allDay;         // true

    @Schema(description = "확장 속성 (커스텀 데이터)")
    private ExtendedProps extendedProps;

    @Getter
    @AllArgsConstructor
    @Schema(description = "일정 확장 속성")
    public static class ExtendedProps {
        @Schema(description = "쓰레기 타입 코드 (BURNABLE, PLASTIC 등)", example = "BURNABLE")
        private String wasteType;   // "BURNABLE", "PLASTIC"
    }
}