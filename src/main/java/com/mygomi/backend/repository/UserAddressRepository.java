package com.mygomi.backend.repository;

import com.mygomi.backend.domain.address.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    List<UserAddress> findByUserId(Long userId);

    // 해당 유저의 대표 주소 찾기
    UserAddress findByUserIdAndIsPrimaryTrue(Long userId);
}