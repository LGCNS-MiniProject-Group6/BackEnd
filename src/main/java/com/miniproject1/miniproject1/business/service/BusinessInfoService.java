package com.miniproject1.miniproject1.business.service;

import org.springframework.stereotype.Service;

import com.miniproject1.miniproject1.business.dto.request.BusinessInfoRequestDTO;
import com.miniproject1.miniproject1.business.dto.response.BusinessInfoResponseDTO;
import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.business.repository.BusinessInfoRepository;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.commons.exception.ErrorCode;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class BusinessInfoService {
    
    private final BusinessInfoRepository businessInfoRepository;

    @Transactional 
    public BusinessInfoResponseDTO registerBusinessInfo (String email, BusinessInfoRequestDTO request) {
        if(businessInfoRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_RESOURCE, "이미 등록된 사업 정보입니다.");
        }

        
        BusinessInfo businessInfo = BusinessInfo.builder()
                                        .email(email)
                                        .industry(request.getIndustry())
                                        .region(request.getRegion())
                                        .openingDate(request.getOpeningDate())
                                        .employeeCount(request.getEmployeeCount())
                                        .annualRevenue(request.getAnnualRevenue())
                                        .build();

        BusinessInfo saveBusinessInfo = businessInfoRepository.save(businessInfo);
        return BusinessInfoResponseDTO.from(saveBusinessInfo);
    }

    public BusinessInfoResponseDTO getBusinessInfo(String email) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND,"등록된 사업 정보가 없습니다."));     
        return BusinessInfoResponseDTO.from(businessInfo);

    }

    @Transactional 
    public BusinessInfoResponseDTO updateBusinessInfo(String email, BusinessInfoRequestDTO request) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "등록된 사업 정보가 없습니다."));
        businessInfo.update(request);

        return BusinessInfoResponseDTO.from (businessInfo);
    }

    @Transactional 
    public void deleteBusinessInfo(String email) {
        BusinessInfo businessInfo = businessInfoRepository.findByEmail(email)
            .orElseThrow(() -> new BusinessException(ErrorCode.DATA_NOT_FOUND, "등록된 사업 정보가 없습니다."));
        businessInfoRepository.delete(businessInfo);
    }

}
