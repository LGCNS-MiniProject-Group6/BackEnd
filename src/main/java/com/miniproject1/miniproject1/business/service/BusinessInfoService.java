package com.miniproject1.miniproject1.business.service;

import org.springframework.stereotype.Service;

import com.miniproject1.miniproject1.business.dto.request.BusinessInfoRequestDTO;
import com.miniproject1.miniproject1.business.dto.response.BusinessInfoResponseDTO;
import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.entity.BusinessInterestCategory; // [추가된 import]
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.business.repository.BusinessInterestCategoryRepository; // [추가된 import]
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import java.util.Collections; // [추가된 import]
import java.util.List; // [추가된 import]
import java.util.Set; // [추가된 import]

@Service
@RequiredArgsConstructor
public class BusinessInfoService {

    // ================= [추가된 상수: 8가지 허용 카테고리] =================
    private static final Set<String> ALLOWED_CATEGORIES = Set.of(
            "금융", "기술", "인력", "수출", "내수", "창업", "경영", "기타");
    // =================================================================

    private final BusinessInfoRepository businessInfoRepository;
    // ================= [추가된 의존성] =================
    private final BusinessInterestCategoryRepository businessInterestCategoryRepository;
    // =================================================

    @Transactional
    public BusinessInfoResponseDTO registerBusinessInfo(String email, BusinessInfoRequestDTO request) {
        if (businessInfoRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.BUSINESS_INFO_ALREADY_EXISTS);
        }

        BusinessInfo businessInfo = BusinessInfo.builder()
                .email(email)
                .industry(request.getIndustry())
                .region(request.getRegion())
                .openingDate(request.getOpeningDate())
                .businessType(request.getBusinessType())
                .employeeCount(request.getEmployeeCount())
                .annualRevenue(request.getAnnualRevenue())
                .build();

        BusinessInfo saveBusinessInfo = businessInfoRepository.save(businessInfo);

        // ================= [추가된 부분: 카테고리 저장 및 응답 매핑] =================
        List<String> savedCategories = saveInterestCategories(saveBusinessInfo.getBusinessId(),
                request.getInterestCategories());
        return BusinessInfoResponseDTO.from(saveBusinessInfo, savedCategories);
        // =============================================================================
    }

    public BusinessInfoResponseDTO getBusinessInfo(String email) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_INFO_NOT_FOUND));

        // ================= [추가된 부분: 카테고리 목록 조회 후 함께 반환] =================
        List<String> categories = getCategoriesByBusinessId(businessInfo.getBusinessId());
        return BusinessInfoResponseDTO.from(businessInfo, categories);
        // =====================================================================

    }

    @Transactional
    public BusinessInfoResponseDTO updateBusinessInfo(String email, BusinessInfoRequestDTO request) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_INFO_NOT_FOUND));
        businessInfo.update(request);

        // ================= [추가된 부분: 카테고리 갱신 (삭제 후 재저장)] =================
        List<String> updatedCategories;
        if (request.getInterestCategories() != null) {
            businessInterestCategoryRepository.deleteByBusinessId(businessInfo.getBusinessId());
            updatedCategories = saveInterestCategories(businessInfo.getBusinessId(), request.getInterestCategories());
        } else {
            updatedCategories = getCategoriesByBusinessId(businessInfo.getBusinessId());
        }
        return BusinessInfoResponseDTO.from(businessInfo, updatedCategories);
        // =============================================================================
    }

    @Transactional
    public void deleteBusinessInfo(String email) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessException(ErrorCode.BUSINESS_INFO_NOT_FOUND));

        // ================= [추가된 부분: 연관 카테고리 명시적 삭제] =================
        businessInterestCategoryRepository.deleteByBusinessId(businessInfo.getBusinessId());
        // =================================================================

        businessInfoRepository.delete(businessInfo);
    }

    // ================= [추가된 헬퍼 메서드 1: 카테고리 검증 및 저장] =================
    private List<String> saveInterestCategories(Long businessId, List<String> categories) {
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyList();
        }

        // 중복 제거(uk_business_category 준수) 및 8가지 허용 카테고리만 필터링
        List<String> validCategories = categories.stream()
                .distinct()
                .filter(ALLOWED_CATEGORIES::contains)
                .toList();

        List<BusinessInterestCategory> entities = validCategories.stream()
                .map(category -> BusinessInterestCategory.builder()
                        .businessId(businessId)
                        .category(category)
                        .build())
                .toList();

        businessInterestCategoryRepository.saveAll(entities);
        return validCategories;
    }

    // ================= [추가된 헬퍼 메서드 2: 카테고리 목록 조회] =================
    private List<String> getCategoriesByBusinessId(Long businessId) {
        return businessInterestCategoryRepository.findByBusinessIdOrderByCategory(businessId)
                .stream()
                .map(BusinessInterestCategory::getCategory)
                .toList();
    }

}
