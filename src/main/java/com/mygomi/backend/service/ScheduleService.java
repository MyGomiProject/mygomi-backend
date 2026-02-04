package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.ScheduleResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.collection.CollectionRule;
import com.mygomi.backend.repository.CollectionRuleRepository;
import com.mygomi.backend.repository.UserAddressRepository;
import com.mygomi.backend.service.calc.ScheduleCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final UserAddressRepository userAddressRepository;
    private final CollectionRuleRepository collectionRuleRepository;
    private final ScheduleCalculator scheduleCalculator;

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getMonthlySchedule(Long userId, int year, int month) {
        UserAddress userAddress = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (userAddress == null) return new ArrayList<>();

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<CollectionRule> rules = collectionRuleRepository.findByAreaId(userAddress.getArea().getId());
        List<ScheduleResponseDto> schedules = new ArrayList<>();

        for (CollectionRule rule : rules) {
            List<LocalDate> dates = scheduleCalculator.calculateDates(
                    startDate, endDate, rule.getRuleType(), rule.getWeekdays(), rule.getNthWeeks());

            for (LocalDate date : dates) {
                // 팀원 요청 포맷에 맞게 빌더로 생성
                schedules.add(ScheduleResponseDto.builder()
                        .id(UUID.randomUUID().toString()) // 고유 ID 부여
                        .title(rule.getWasteType().getDescription()) // "가연성 쓰레기" 등
                        .start(date)
                        .allDay(true)
                        .extendedProps(new ScheduleResponseDto.ExtendedProps(rule.getWasteType().name()))
                        .build());
            }
        }

        schedules.sort(Comparator.comparing(ScheduleResponseDto::getStart));
        return schedules;
    }
}