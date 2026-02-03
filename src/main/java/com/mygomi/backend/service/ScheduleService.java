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

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final UserAddressRepository userAddressRepository;
    private final CollectionRuleRepository collectionRuleRepository;
    private final ScheduleCalculator scheduleCalculator;

    @Transactional(readOnly = true)
    public List<ScheduleResponseDto> getMonthlySchedule(Long userId, int year, int month) {
        // 1. 유저의 대표 주소 가져오기
        UserAddress userAddress = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (userAddress == null) {
            // 주소가 없으면 빈 리스트 반환 (또는 에러 처리)
            return new ArrayList<>();
        }

        // 2. 조회할 기간 설정 (해당 월의 1일 ~ 말일)
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // 3. 해당 지역(Area)의 수거 규칙 모두 가져오기
        List<CollectionRule> rules = collectionRuleRepository.findByAreaId(userAddress.getArea().getId());

        // 4. 규칙을 하나씩 돌면서 날짜 계산기로 날짜 뽑아내기
        List<ScheduleResponseDto> schedules = new ArrayList<>();

        for (CollectionRule rule : rules) {
            // 계산기 작동! (이미 만들어둔 Calculator 사용)
            List<LocalDate> dates = scheduleCalculator.calculateDates(
                    startDate,
                    endDate,
                    rule.getRuleType(),
                    rule.getWeekdays(),
                    rule.getNthWeeks()
            );

            // 결과 DTO로 변환
            for (LocalDate date : dates) {
                schedules.add(new ScheduleResponseDto(
                        date,
                        rule.getWasteType().getDescription(), // "타는 쓰레기" (한글)
                        rule.getWasteType().name(),           // "BURNABLE" (코드)
                        rule.getNote()
                ));
            }
        }

        // 5. 날짜순으로 정렬해서 반환 (빠른 날짜가 위로 오게)
        schedules.sort(Comparator.comparing(ScheduleResponseDto::getDate));

        return schedules;
    }
}