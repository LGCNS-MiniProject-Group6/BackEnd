package com.miniproject1.miniproject1.business.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;

public interface BusinessInfoRepository extends JpaRepository<BusinessInfo, Long>{
    boolean existsByEmail(String email);
    Optional<BusinessInfo> findByEmail(String email);
}
