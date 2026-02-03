package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.CalendarResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.collection.CollectionRule;
import com.mygomi.backend.domain.enums.RuleType;
import com.mygomi.backend.repository.CollectionRuleRepository;
import com.mygomi.backend.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CalendarService {

    private final CollectionRuleRepository collectionRuleRepository;
    private final UserAddressRepository userAddressRepository;

    @Transactional(readOnly = true)
    public List<CalendarResponseDto> getMyCollectionCalendar(Long userId, int year, int month) {
        UserAddress myAddress = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (myAddress == null || myAddress.getArea() == null) return new ArrayList<>();

        List<CollectionRule> rules = collectionRuleRepository.findByAreaId(myAddress.getArea().getId());
        List<CalendarResponseDto> events = new ArrayList<>();
        YearMonth targetMonth = YearMonth.of(year, month);

        for (CollectionRule rule : rules) {
            // 1. 요일이 "MON,THU" 처럼 여러 개일 수 있음 -> 콤마로 쪼개기
            String[] days = rule.getDayOfWeek().split(",");

            for (String dayStr : days) {
                DayOfWeek day = parseDay(dayStr.trim());
                if (day == null) continue;

                List<LocalDate> dates = calculateDates(rule.getRuleType(), day, rule.getWeekNumbers(), targetMonth);

                for (LocalDate date : dates) {
                    events.add(CalendarResponseDto.builder()
                            .title(rule.getWasteType().getLabel())
                            .start(date)
                            .color(rule.getWasteType().getColor())
                            .allDay(true)
                            .build());
                }
            }
        }
        return events;
    }

    private DayOfWeek parseDay(String dayStr) {
        try {
            // CSV에는 "MON", "TUE" 등으로 저장됨
            return DayOfWeek.valueOf(dayStr.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

    private List<LocalDate> calculateDates(RuleType type, DayOfWeek day, String weekNums, YearMonth yearMonth) {
        List<LocalDate> resultDates = new ArrayList<>();
        LocalDate firstDay = yearMonth.atDay(1);

        if (type == RuleType.WEEKDAY) { // 매주
            LocalDate date = firstDay.with(TemporalAdjusters.firstInMonth(day));
            while (date.getMonth().equals(yearMonth.getMonth())) {
                resultDates.add(date);
                date = date.plusWeeks(1);
            }
        } else if (type == RuleType.NTH_WEEKDAY && weekNums != null) { // 특정 주 (예: "1,3")
            String[] weeks = weekNums.split(",");
            for (String w : weeks) {
                try {
                    int weekOrder = Integer.parseInt(w.trim());
                    LocalDate date = firstDay.with(TemporalAdjusters.dayOfWeekInMonth(weekOrder, day));
                    if (date.getMonth().equals(yearMonth.getMonth())) {
                        resultDates.add(date);
                    }
                } catch (Exception ignored) {}
            }
        }
        return resultDates;
    }
}