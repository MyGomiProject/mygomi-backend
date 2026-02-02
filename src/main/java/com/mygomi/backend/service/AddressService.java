package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.area.Area;
import com.mygomi.backend.repository.AreaRepository;
import com.mygomi.backend.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository userAddressRepository;
    private final AreaRepository areaRepository;

    @Transactional
    public AddressResponseDto saveOrUpdateAddress(Long userId, AddressRequestDto request) {

        // 1. 쵸메 정제 ('1丁目' -> '1')
        String cleanChome = request.getChome();
        if (cleanChome != null) {
            cleanChome = cleanChome.replace("丁目", "").trim();
        }

        // 2. DB에서 일단 후보군(List)을 다 조회함
        List<Area> candidateAreas;
        if (cleanChome != null && !cleanChome.isEmpty()) {
            candidateAreas = areaRepository.findByPrefectureAndWardAndTownAndChome(
                    request.getPrefecture(), request.getWard(), request.getTown(), cleanChome
            );
        } else {
            candidateAreas = areaRepository.findByPrefectureAndWardAndTownAndChomeIsNull(
                    request.getPrefecture(), request.getWard(), request.getTown()
            );
        }

        // 3. 🕵️‍♂️ 번지수(Banchi)로 정확한 구역 찾기 (여기가 핵심!)
        Area mappedArea = findBestMatchingArea(candidateAreas, request.getBanchi());

        // 4. 저장/수정 (기존과 동일)
        UserAddress userAddress = userAddressRepository.findByUserId(userId).orElse(null);

        if (userAddress == null) {
            userAddress = UserAddress.builder()
                    .userId(userId)
                    .area(mappedArea)
                    .prefecture(request.getPrefecture())
                    .ward(request.getWard())
                    .town(request.getTown())
                    .chome(cleanChome)
                    .banchiText(request.getBanchi())
                    .isPrimary(true)
                    .lat(request.getLat())
                    .lng(request.getLng())
                    .build();
            userAddressRepository.save(userAddress);
        } else {
            userAddress.updateAddress(
                    mappedArea,
                    request.getPrefecture(), request.getWard(), request.getTown(),
                    cleanChome, request.getBanchi(),
                    request.getLat(), request.getLng()
            );
        }

        return new AddressResponseDto(userAddress);
    }

    @Transactional(readOnly = true)
    public AddressResponseDto getAddress(Long userId) {
        UserAddress userAddress = userAddressRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("설정된 주소가 없습니다."));
        return new AddressResponseDto(userAddress);
    }

    // ==========================================
    // 🕵️‍♂️ 번지수 매칭 로직 (Private Helper)
    // ==========================================
    private Area findBestMatchingArea(List<Area> areas, String userBanchi) {
        if (areas.isEmpty()) return null;
        if (areas.size() == 1) return areas.get(0); // 하나밖에 없으면 고민 없이 리턴
        if (userBanchi == null || userBanchi.isBlank()) return areas.get(0); // 사용자 번지 없으면 첫 번째 거 줌

        // 1. 사용자 입력에서 '번지' 숫자만 추출 (예: "23-5" -> 23)
        int targetNumber;
        try {
            String mainNumber = userBanchi.split("-")[0].replaceAll("[^0-9]", "");
            targetNumber = Integer.parseInt(mainNumber);
        } catch (NumberFormatException e) {
            log.warn("번지수 파싱 실패: {}", userBanchi);
            return areas.get(0); // 숫자 아니면 그냥 첫 번째 거 반환
        }

        // 2. 후보군을 하나씩 돌면서 확인
        for (Area area : areas) {
            String ruleText = area.getBanchiText(); // 예: "1-21, 41-47, 53"
            if (ruleText == null || ruleText.equals("전역")) return area;

            // 콤마(,)로 구역 나눔
            String[] rules = ruleText.split(",");

            for (String rule : rules) {
                rule = rule.trim();
                if (rule.contains("-")) {
                    // 범위인 경우 (예: "1-21")
                    try {
                        String[] range = rule.split("-");
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        if (targetNumber >= start && targetNumber <= end) {
                            return area; // 🎯 찾았다!
                        }
                    } catch (Exception ignored) {}
                } else {
                    // 단일 숫자인 경우 (예: "53")
                    try {
                        int single = Integer.parseInt(rule);
                        if (targetNumber == single) {
                            return area; // 🎯 찾았다!
                        }
                    } catch (Exception ignored) {}
                }
            }
        }

        // 못 찾았으면 아쉽지만 첫 번째 구역으로 설정
        return areas.get(0);
    }
}