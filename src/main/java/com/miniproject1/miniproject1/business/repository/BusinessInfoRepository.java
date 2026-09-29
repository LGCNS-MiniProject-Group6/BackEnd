package com.miniproject1.miniproject1.business.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;

public interface BusinessInfoRepository extends JpaRepository<BusinessInfo, Long>{
    boolean existsByEmail(String email);
    Optional<BusinessInfo> findByEmail(String email);

    /** 대표 사업정보가 있으면 우선 선택하고, 없으면 가장 먼저 등록한 정보를 사용합니다. */
    Optional<BusinessInfo> findFirstByEmailOrderByIsDefaultDescBusinessIdAsc(String email);
}
