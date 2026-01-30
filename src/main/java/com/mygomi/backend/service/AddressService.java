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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

        // 1. Area 매칭 로직 (W2에서 고도화 예정, 지금은 단순 일치 검색)
        Area area = areaRepository.findByPrefectureAndWardAndTownAndChome(
                request.getPrefecture(), request.getWard(), request.getTown(), request.getChome()
        ).orElse(null); // 매칭 실패 시 null (수거 규칙 조회 불가)

        // 2. 대표 주소 설정 시 기존 대표 주소 해제
        if (Boolean.TRUE.equals(request.getIsPrimary())) {
            UserAddress oldPrimary = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
            if (oldPrimary != null) {
                oldPrimary.updatePrimary(false);
            }
        }

        // 3. 주소 저장
        UserAddress address = UserAddress.builder()
                .user(user)
                .area(area)
                .prefecture(request.getPrefecture())
                .ward(request.getWard())
                .town(request.getTown())
                .chome(request.getChome())
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
}