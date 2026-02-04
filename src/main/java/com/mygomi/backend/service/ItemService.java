package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.ItemResponseDto;
import com.mygomi.backend.domain.address.UserAddress;
import com.mygomi.backend.repository.ItemRepository;
import com.mygomi.backend.repository.UserAddressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItemService {

    private final ItemRepository itemRepository;
    private final UserAddressRepository userAddressRepository;

    @Transactional(readOnly = true)
    public List<ItemResponseDto> searchItems(Long userId, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return List.of();
        }

        // 1. 유저 주소 찾기
        UserAddress userAddress = userAddressRepository.findByUserIdAndIsPrimaryTrue(userId);
        if (userAddress == null) {
            return List.of();
        }

        // 2. 검색어 전처리: 공백 제거 ("헌 종이" -> "헌종이")
        // 이렇게 하면 "CD"와 "cd" 처리는 DB 쿼리(LOWER)에서 하고, 띄어쓰기는 여기서 잡습니다.
        String cleanedKeyword = keyword.replaceAll("\\s+", "");

        // 3. 검색 실행
        return itemRepository.searchByKeywordAndWard(cleanedKeyword, userAddress.getWard())
                .stream()
                .map(ItemResponseDto::new)
                .toList();
    }
}