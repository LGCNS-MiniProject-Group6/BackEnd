package com.miniproject1.miniproject1.business.repository;

import com.miniproject1.miniproject1.business.entity.BusinessInterestCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessInterestCategoryRepository extends JpaRepository<BusinessInterestCategory, Long> {

    List<BusinessInterestCategory> findByBusinessIdOrderByCategory(Long businessId);

    // [추가된 부분] 사업자 정보 수정/삭제 시 기존 관심 카테고리 일괄 삭제
    void deleteByBusinessId(Long businessId);
}
