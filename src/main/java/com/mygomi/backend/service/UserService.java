package com.mygomi.backend.service;

import com.mygomi.backend.api.dto.response.AddressResponseDto;
import com.mygomi.backend.api.dto.response.UserResponseDto;
import com.mygomi.backend.domain.user.User;
import com.mygomi.backend.repository.UserRepository;
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
    private final org.springframework.security.crypto.password.PasswordEncoder passwordEncoder; // [추가]

    public UserResponseDto getCurrentUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("가입되지 않은 사용자입니다. email=" + email));
        
        // 사용자의 주소 정보도 함께 조회
        List<AddressResponseDto> addresses = addressService.getMyAddresses(user.getId());
        
        return UserResponseDto.from(user, addresses);
    }

    /**
     * 닉네임 수정
     */
    @Transactional
    public UserResponseDto updateNickname(String email, String newNickname) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        user.updateNickname(newNickname);
        
        List<AddressResponseDto> addresses = addressService.getMyAddresses(user.getId());
        return UserResponseDto.from(user, addresses);
    }

    /**
     * 비밀번호 수정
     */
    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
        
        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        
        // 새 비밀번호 암호화 후 저장
        String encodedPassword = passwordEncoder.encode(newPassword);
        user.updatePassword(encodedPassword);
    }
}
