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
import com.miniproject1.miniproject1.commons.exception.ErrorResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;

@Tag(name = "03. Business", description = "사업 정보")
@RestController
@RequestMapping("/api/users/me/business-info")
@RequiredArgsConstructor
@Validated
public class BusinessInfoController {

    private final BusinessInfoService businessInfoService;

    @Operation(summary = "사업정보 등록")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "등록 성공",
                    content = @Content(schema = @Schema(implementation = BusinessInfoResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "businessId": 1,
                                      "companyName": null,
                                      "industry": "카페",
                                      "region": "서울",
                                      "openingDate": "2024-03-15",
                                      "employeeCount": 0,
                                      "annualRevenue": 80000000,
                                      "isDefault": true,
                                      "createdAt": "2026-09-22T16:00:00",
                                      "updatedAt": "2026-09-22T16:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "INVALID_INPUT_VALUE",
                                      "message": "입력값 또는 요청 형식이 올바르지 않습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": [
                                        { "field": "industry", "value": "", "reason": "업종은 필수입니다." }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "409", description = "이미 등록된 사업 정보입니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BUSINESS_INFO_ALREADY_EXISTS",
                                      "message": "이미 등록된 사업 정보입니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": []
                                    }
                                    """)))
    })
    @PostMapping
    public ResponseEntity<BusinessInfoResponseDTO> registerBusinessInfo(
            Authentication authentication,
            @Valid @RequestBody BusinessInfoRequestDTO request) {
        String email = (String) authentication.getPrincipal();
        BusinessInfoResponseDTO response = businessInfoService.registerBusinessInfo(email, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation (summary = "사업정보 조회")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "조회 성공",
                    content = @Content(schema = @Schema(implementation = BusinessInfoResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "businessId": 1,
                                      "companyName": null,
                                      "industry": "카페",
                                      "region": "서울",
                                      "openingDate": "2024-03-15",
                                      "employeeCount": 0,
                                      "annualRevenue": 80000000,
                                      "isDefault": true,
                                      "createdAt": "2026-09-22T16:00:00",
                                      "updatedAt": "2026-09-22T16:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "등록된 사업 정보가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BUSINESS_INFO_NOT_FOUND",
                                      "message": "등록된 사업 정보가 없습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": []
                                    }
                                    """)))
    })
    @GetMapping
    public ResponseEntity<BusinessInfoResponseDTO> getBusinessInfo(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        BusinessInfoResponseDTO response = businessInfoService.getBusinessInfo(email);
        return ResponseEntity.ok(response);
    }
    
    @Operation (summary = "사업정보 수정")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "수정 성공",
                    content = @Content(schema = @Schema(implementation = BusinessInfoResponseDTO.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "businessId": 1,
                                      "companyName": null,
                                      "industry": "카페",
                                      "region": "서울",
                                      "openingDate": "2024-03-15",
                                      "employeeCount": 0,
                                      "annualRevenue": 80000000,
                                      "isDefault": true,
                                      "createdAt": "2026-09-22T16:00:00",
                                      "updatedAt": "2026-09-22T16:00:00"
                                    }
                                    """))),
            @ApiResponse(responseCode = "400", description = "요청 값 검증 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "INVALID_INPUT_VALUE",
                                      "message": "입력값 또는 요청 형식이 올바르지 않습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": [
                                        { "field": "employeeCount", "value": "-1", "reason": "직원 수는 0 이상이어야 합니다." }
                                      ]
                                    }
                                    """))),
            @ApiResponse(responseCode = "404", description = "등록된 사업 정보가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BUSINESS_INFO_NOT_FOUND",
                                      "message": "등록된 사업 정보가 없습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": []
                                    }
                                    """)))
    })
    @PutMapping
    public ResponseEntity<BusinessInfoResponseDTO> updateBusinessInfo(
            Authentication authentication,
            @Valid @RequestBody BusinessInfoRequestDTO request) {
        String email = (String) authentication.getPrincipal();
        BusinessInfoResponseDTO response = businessInfoService.updateBusinessInfo(email, request);
        return ResponseEntity.ok(response);
    }

    @Operation (summary = "사업정보 삭제")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "삭제 성공"),
            @ApiResponse(responseCode = "404", description = "등록된 사업 정보가 없습니다.",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class),
                            examples = @ExampleObject(value = """
                                    {
                                      "code": "BUSINESS_INFO_NOT_FOUND",
                                      "message": "등록된 사업 정보가 없습니다.",
                                      "timestamp": "2026-09-22T16:00:00+09:00",
                                      "path": "/api/users/me/business-info",
                                      "details": []
                                    }
                                    """)))
    })
    @DeleteMapping
    public ResponseEntity<Void> deleteBusinessInfo(Authentication authentication) {
        String email = (String) authentication.getPrincipal();
        businessInfoService.deleteBusinessInfo(email);
        return ResponseEntity.noContent().build();
    }
}
