package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.domain.address.Area;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.domain.user.UserRepository;
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
    private final UserRepository userRepository;

    @Transactional
    public AddressResponseDto saveOrUpdateAddress(Long userId, AddressRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        // 1. 쵸메 정제 ('1丁目' -> '1')
        String cleanChome = request.getChome();
        if (cleanChome != null) {
            cleanChome = cleanChome.replace("丁目", "").trim();
        }

        // 2. DB에서 후보군(List) 조회 (develop 로직 차용)
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

        // 3. 🕵️‍♂️ 번지수(Banchi)로 정확한 구역 찾기 (핵심 로직)
        Area mappedArea = findBestMatchingArea(candidateAreas, request.getBanchiText());

        // 4. 대표 주소 설정 시 기존 대표 주소 해제 (auth 로직)
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            UserAddress oldPrimary = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
            if (oldPrimary != null) {
                oldPrimary.updatePrimary(false);
            }
        }

        // 5. 주소 저장 (User 엔티티 사용하는 auth 방식 유지)
        UserAddress address = UserAddress.builder()
                .user(user)
                .area(mappedArea)
                .prefecture(request.getPrefecture())
                .ward(request.getWard())
                .town(request.getTown())
                .chome(cleanChome)
                .banchiText(request.getBanchiText())
                .isPrimary(request.getIsPrimary())
                .lat(request.getLat())
                .lng(request.getLng())
                .build();

        UserAddress saved = userAddressRepository.save(address);
        return AddressResponseDto.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AddressResponseDto> getMyAddresses(Long userId) {
        return userAddressRepository.findByUserId(userId).stream()
                .map(AddressResponseDto::from)
                .toList();
    }

    // ==========================================
    // 🕵️‍♂️ 번지수 매칭 로직 (develop에서 가져옴)
    // ==========================================
    private Area findBestMatchingArea(List<Area> areas, String userBanchi) {
        if (areas.isEmpty()) return null;
        if (areas.size() == 1) return areas.get(0);
        if (userBanchi == null || userBanchi.isBlank()) return areas.get(0);

        int targetNumber;
        try {
            // "23-5" -> 23 추출
            String mainNumber = userBanchi.split("-")[0].replaceAll("[^0-9]", "");
            targetNumber = Integer.parseInt(mainNumber);
        } catch (NumberFormatException e) {
            log.warn("번지수 파싱 실패: {}", userBanchi);
            return areas.get(0);
        }

        for (Area area : areas) {
            String ruleText = area.getBanchiText();
            if (ruleText == null || ruleText.equals("전역")) return area;

            String[] rules = ruleText.split(",");
            for (String rule : rules) {
                rule = rule.trim();
                if (rule.contains("-")) {
                    try {
                        String[] range = rule.split("-");
                        int start = Integer.parseInt(range[0]);
                        int end = Integer.parseInt(range[1]);
                        if (targetNumber >= start && targetNumber <= end) return area;
                    } catch (Exception ignored) {}
                } else {
                    try {
                        int single = Integer.parseInt(rule);
                        if (targetNumber == single) return area;
                    } catch (Exception ignored) {}
                }
            }
        }
        return areas.get(0);
    }
}