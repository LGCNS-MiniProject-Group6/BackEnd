package com.miniproject1.miniproject1.program.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.miniproject1.miniproject1.commons.exception.BusinessException;
import com.miniproject1.miniproject1.program.dto.api.ProgramApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExternalProgramApiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper;

    // ★ 오타 수정: 닫는 중괄호 '}' 추가
    @Value("${biziinfo.api.url}")
    private String apiUrl;

    @Value("${biziinfo.api.key}")
    private String apiKey;

    public List<ProgramApiDTO> fetchProgramsFromOpenApi() {
        log.info("[OpenAPI Client] 기업마당 API 임시 수집 시작 (Target URL: {})", apiUrl);
        List<ProgramApiDTO> resultList = new ArrayList<>();

        try {
            URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("crtfcKey", apiKey)
                    .queryParam("dataType", "json")
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();

            // ★ Null type safety 경고 해결: MediaType.APPLICATION_JSON 정적 객체 직접 주입
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);

            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            String responseStr = response.getBody();

            if (responseStr == null || responseStr.isBlank()) {
                log.warn("[OpenAPI Client] API 응답 데이터가 비어있습니다.");
                return resultList;
            }

            JsonNode rootNode = objectMapper.readTree(responseStr);

            JsonNode targetArrayNode = rootNode.path("jsonArray");
            if (targetArrayNode.has("item")) {
                targetArrayNode = targetArrayNode.path("item");
            }

            if (targetArrayNode.isArray()) {
                for (JsonNode node : targetArrayNode) {
                    try {
                        ProgramApiDTO dto = objectMapper.treeToValue(node, ProgramApiDTO.class);
                        resultList.add(dto);
                    } catch (BusinessException e) {
                        log.warn("[OpenAPI Client] 공고 데이터 검증/날짜 파싱 실패로 스킵 - pblancId: {}, 사유: {}",
                                node.path("pblancId").asText(), e.getMessage());
                    } catch (Exception e) {
                        log.warn("[OpenAPI Client] 개별 공고 데이터 매핑 실패 - pblancId: {}, 사유: {}",
                                node.path("pblancId").asText(), e.getMessage());
                    }
                }
            }

            log.info("[OpenAPI Client] 총 {}건의 공고 파싱 완료", resultList.size());

        } catch (Exception e) {
            log.error("[OpenAPI Client] 외부 API 수집 중 네트워크/파싱 오류 발생: {}", e.getMessage(), e);
        }

        return resultList;
    }
}