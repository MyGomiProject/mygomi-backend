package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.request.AddressRequestDto;
import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.domain.area.Area;
import com.mygomi.backend.repository.AreaRepository;
import com.mygomi.backend.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AddressService {

    private final UserAddressRepository userAddressRepository;
    private final AreaRepository areaRepository;

    @Transactional
    public AddressResponseDto saveOrUpdateAddress(Long userId, AddressRequestDto request) {
        // 1. '1丁目' -> '1' 정제
        String cleanChome = request.getChome();
        if (cleanChome != null) {
            cleanChome = cleanChome.replace("丁目", "").trim();
        }

        // 2. Area 매핑 (DB 조회)
        Area mappedArea = null;
        if (cleanChome != null && !cleanChome.isEmpty()) {
            mappedArea = areaRepository.findByPrefectureAndWardAndTownAndChome(
                    request.getPrefecture(), request.getWard(), request.getTown(), cleanChome
            ).orElse(null);
        } else {
            mappedArea = areaRepository.findByPrefectureAndWardAndTownAndChomeIsNull(
                    request.getPrefecture(), request.getWard(), request.getTown()
            ).orElse(null);
        }

        // 3. 저장/수정
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
}