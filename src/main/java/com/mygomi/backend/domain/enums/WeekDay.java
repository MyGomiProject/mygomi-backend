package com.mygomi.backend.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.DayOfWeek;
import java.util.Arrays;

@Getter
@RequiredArgsConstructor
public enum WeekDay {
    MON("MON", DayOfWeek.MONDAY),
    TUE("TUE", DayOfWeek.TUESDAY),
    WED("WED", DayOfWeek.WEDNESDAY),
    THU("THU", DayOfWeek.THURSDAY),
    FRI("FRI", DayOfWeek.FRIDAY),
    SAT("SAT", DayOfWeek.SATURDAY),
    SUN("SUN", DayOfWeek.SUNDAY);

    private final String dbValue;       // DB에 저장된 값 ("MON")
    private final DayOfWeek javaDay;    // 자바 날짜 계산용 값 (DayOfWeek.MONDAY)

    // "MON" -> WeekDay.MON 으로 찾아주는 메소드
    public static DayOfWeek toJavaDay(String dbValue) {
        return Arrays.stream(values())
                .filter(d -> d.dbValue.equalsIgnoreCase(dbValue.trim()))
                .findFirst()
                .map(WeekDay::getJavaDay)
                .orElseThrow(() -> new IllegalArgumentException("알 수 없는 요일입니다: " + dbValue));
    }
}