package com.miniproject1.miniproject1.business.repository;

import com.miniproject1.miniproject1.business.entity.BusinessInterestCategory;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusinessInterestCategoryRepository extends JpaRepository<BusinessInterestCategory, Long> {

    List<BusinessInterestCategory> findByBusinessIdOrderByCategory(Long businessId);
}
