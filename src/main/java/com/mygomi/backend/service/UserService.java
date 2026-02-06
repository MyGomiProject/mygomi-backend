package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.api.dto.response.UserResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final AddressService addressService;

    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 사용자입니다. email=" + email));
        
        // 사용자의 주소 정보도 함께 조회
        List<AddressResponseDto> addresses = addressService.getMyAddresses(user.getId());
        
        return UserResponseDto.from(user, addresses);
    }
}
