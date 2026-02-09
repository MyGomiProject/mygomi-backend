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
    private final GeocodingService geocodingService; // 📍 지오코딩 서비스

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
        Area mappedArea = findBestMatchingArea(candidateAreas, request.getBanchi());

        // 4. 📍 [추가됨] 지오코딩: 주소를 좌표로 변환
        // 검색 정확도를 위해 "丁目"를 붙여서 검색합니다.
        String searchAddress = String.format("%s %s %s %s %s",
                request.getPrefecture(),
                request.getWard(),
                request.getTown(),
                (cleanChome != null && !cleanChome.isEmpty()) ? cleanChome + "丁目" : "",
                request.getBanchi() != null ? request.getBanchi() : ""
        ).trim();

        GeocodingService.GeoCoordinate coordinate = geocodingService.getCoordinate(searchAddress);
        log.info("지오코딩 변환: {} -> lat={}, lng={}", searchAddress, coordinate.lat(), coordinate.lng());

        // 5. 대표 주소 설정 시 기존 대표 주소 해제
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            UserAddress oldPrimary = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
            if (oldPrimary != null) {
                oldPrimary.updatePrimary(false);
            }
        }

        // 6. 주소 저장
        // 주의: UserAddress 엔티티는 보통 User 객체를 받습니다. (.user(user))
        UserAddress address = UserAddress.builder()
                .user(user)
                .area(mappedArea)
                .prefecture(request.getPrefecture())
                .ward(request.getWard())
                .town(request.getTown())
                .chome(cleanChome)
                .banchiText(request.getBanchi())
                .isPrimary(request.getIsPrimary())
                .lat(coordinate.lat()) // 📍 지오코딩된 위도
                .lng(coordinate.lng()) // 📍 지오코딩된 경도
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

    /**
     * 사용자의 주 주소(대표 주소) 가져오기
     */
    @Transactional(readOnly = true)
    public UserAddress getPrimaryAddress(Long userId) {
        UserAddress primaryAddress = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (primaryAddress == null) {
            throw new IllegalArgumentException("등록된 대표 주소가 없습니다. 주소를 먼저 등록해주세요.");
        }
        return primaryAddress;
    }

    /**
     * 대표 주소 변경 (배민 방식)
     */
    @Transactional
    public AddressResponseDto changePrimaryAddress(Long userId, Long addressId) {
        // 1. 해당 주소가 존재하고 본인 것인지 확인
        UserAddress targetAddress = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다."));
        
        if (!targetAddress.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 주소만 변경할 수 있습니다.");
        }
        
        // 2. 기존 대표 주소 해제
        UserAddress oldPrimary = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (oldPrimary != null) {
            oldPrimary.updatePrimary(false);
        }
        
        // 3. 새로운 대표 주소 설정
        targetAddress.updatePrimary(true);
        
        log.info("대표 주소 변경 완료 - userId: {}, addressId: {}", userId, addressId);
        
        return AddressResponseDto.from(targetAddress);
    }

    /**
     * 주소 삭제
     */
    @Transactional
    public void deleteAddress(Long userId, Long addressId) {
        // 1. 해당 주소가 존재하고 본인 것인지 확인
        UserAddress address = userAddressRepository.findById(addressId)
                .orElseThrow(() -> new IllegalArgumentException("주소를 찾을 수 없습니다."));
        
        if (!address.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("본인의 주소만 삭제할 수 있습니다.");
        }
        
        // 2. 대표 주소인 경우 삭제 불가 (최소 1개는 있어야 함)
        if (Boolean.TRUE.equals(address.getIsPrimary())) {
            long addressCount = userAddressRepository.findByUserId(userId).size();
            if (addressCount <= 1) {
                throw new IllegalArgumentException("대표 주소는 삭제할 수 없습니다. 다른 주소를 대표로 설정한 후 삭제해주세요.");
            }
        }
        
        // 3. 삭제
        userAddressRepository.delete(address);
        
        log.info("주소 삭제 완료 - userId: {}, addressId: {}", userId, addressId);
    }

    // ==========================================
    // 🕵️‍♂️ 번지수 매칭 로직 (기존 유지)
    // ==========================================
    private Area findBestMatchingArea(List<Area> areas, String userBanchi) {
        if (areas == null || areas.isEmpty()) return null; // null safe 처리 추가
        if (areas.size() == 1) return areas.get(0);
        if (userBanchi == null || userBanchi.isBlank()) return areas.get(0);

        int targetNumber;
        try {
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