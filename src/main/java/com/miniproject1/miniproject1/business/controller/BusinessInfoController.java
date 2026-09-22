package com.miniproject1.miniproject1.business.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.miniproject1.miniproject1.business.dto.request.BusinessInfoRequestDTO;
import com.miniproject1.miniproject1.business.dto.response.BusinessInfoResponseDTO;
import com.miniproject1.miniproject1.business.service.BusinessInfoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "03. Business", description = "사업 정보")
@RestController
@RequestMapping("/api/users/me/business-info")
@RequiredArgsConstructor
@Validated
public class BusinessInfoController {

    private final BusinessInfoService businessInfoService;

    @Operation(summary = "사업정보 등록")
    @PostMapping
    public ResponseEntity<BusinessInfoResponseDTO> registerBusinessInfo(
            Authentication authentication,
            @Valid @RequestBody BusinessInfoRequestDTO request) {
        String email = (String) authentication.getPrincipal();
        BusinessInfoResponseDTO response = businessInfoService.registerBusinessInfo(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}
