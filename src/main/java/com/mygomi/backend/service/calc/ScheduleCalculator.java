package com.mygomi.backend.service.calc;

import com.mygomi.backend.domain.enums.RuleType;
import com.mygomi.backend.domain.enums.WeekDay;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ScheduleCalculator {

    /**
     * 주어진 기간 동안의 수거 날짜를 계산합니다.
     *
     * @param startDate   조회 시작일 (예: 2026-02-01)
     * @param endDate     조회 종료일 (예: 2026-02-28)
     * @param ruleType    규칙 타입 (WEEKDAY, NTH_WEEKDAY 등)
     * @param weekdaysStr 요일 문자열 (예: "MON, THU")
     * @param nthWeeksStr 주차 문자열 (예: "1,3" -> 1째주, 3째주)
     * @return 수거 날짜 리스트
     */
    public List<LocalDate> calculateDates(LocalDate startDate, LocalDate endDate, RuleType ruleType, String weekdaysStr, String nthWeeksStr) {
        if (weekdaysStr == null || weekdaysStr.isBlank()) {
            return Collections.emptyList();
        }

        // 1. DB의 요일 문자열("MON, THU")을 자바 DayOfWeek 리스트로 변환
        List<DayOfWeek> targetDays = parseWeekDays(weekdaysStr);
        List<LocalDate> resultDates = new ArrayList<>();

        // 2. 규칙 타입에 따라 계산 분기
        if (ruleType == RuleType.WEEKDAY) {
            // 매주 수거 (쉬움)
            resultDates = calculateWeekly(startDate, endDate, targetDays);
        } else if (ruleType == RuleType.NTH_WEEKDAY) {
            // 특정 주차 수거 (어려움: "1,3주 금요일")
            List<Integer> targetWeeks = parseNthWeeks(nthWeeksStr);
            resultDates = calculateNthWeekday(startDate, endDate, targetDays, targetWeeks);
        }

        Collections.sort(resultDates); // 날짜순 정렬
        return resultDates;
    }

    // ========================================================
    // 🕵️‍♂️ 내부 계산 로직 (Private Methods)
    // ========================================================

    /**
     * 매주(WEEKDAY) 계산 로직
     * - 시작일부터 하루씩 넘기면서 해당 요일인지 확인
     */
    private List<LocalDate> calculateWeekly(LocalDate start, LocalDate end, List<DayOfWeek> targetDays) {
        List<LocalDate> dates = new ArrayList<>();
        LocalDate current = start;

        // 시작일~종료일 반복
        while (!current.isAfter(end)) {
            if (targetDays.contains(current.getDayOfWeek())) {
                dates.add(current);
            }
            current = current.plusDays(1);
        }
        return dates;
    }

    /**
     * 격주/특정 주(NTH_WEEKDAY) 계산 로직
     * - 해당 월의 N번째 요일을 찾아서 범위 내에 있는지 확인
     */
    private List<LocalDate> calculateNthWeekday(LocalDate start, LocalDate end, List<DayOfWeek> targetDays, List<Integer> targetWeeks) {
        List<LocalDate> dates = new ArrayList<>();

        // 조회 기간에 포함된 모든 "월(Month)"을 순회해야 함
        // 예: 1월 30일 ~ 3월 2일 조회면 -> 1월, 2월, 3월 다 체크
        LocalDate monthCursor = start.withDayOfMonth(1); // 시작일이 속한 달의 1일
        LocalDate endMonth = end.withDayOfMonth(1);     // 종료일이 속한 달의 1일

        while (!monthCursor.isAfter(endMonth)) {
            // 이번 달(monthCursor)에서 타겟 요일과 주차를 찾음
            for (DayOfWeek dayOfWeek : targetDays) {
                for (int nth : targetWeeks) {
                    // "이번 달의 nth번째 dayOfWeek" 날짜 계산 (핵심!)
                    LocalDate calculatedDate = monthCursor.with(TemporalAdjusters.dayOfWeekInMonth(nth, dayOfWeek));

                    // 계산된 날짜가 이번 달이 맞는지 확인 (5주차 없는 달 방지)
                    if (calculatedDate.getMonth() == monthCursor.getMonth()) {
                        // 조회 범위(start ~ end) 안에 들어오는지 확인
                        if (!calculatedDate.isBefore(start) && !calculatedDate.isAfter(end)) {
                            dates.add(calculatedDate);
                        }
                    }
                }
            }
            monthCursor = monthCursor.plusMonths(1); // 다음 달로 이동
        }
        return dates;
    }

    // "MON, THU" -> [MONDAY, THURSDAY] 파싱
    private List<DayOfWeek> parseWeekDays(String weekdaysStr) {
        return Arrays.stream(weekdaysStr.split(","))
                .map(String::trim)
                .map(WeekDay::toJavaDay) // 아까 만든 WeekDay Enum 활용!
                .collect(Collectors.toList());
    }

    // "1,3" -> [1, 3] 파싱
    private List<Integer> parseNthWeeks(String nthWeeksStr) {
        if (nthWeeksStr == null || nthWeeksStr.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return Arrays.stream(nthWeeksStr.split(","))
                    .map(String::trim)
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());
        } catch (NumberFormatException e) {
            log.warn("주차 파싱 실패: {}", nthWeeksStr);
            return Collections.emptyList();
        }
    }
}