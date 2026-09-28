package com.miniproject1.miniproject1;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.miniproject1.miniproject1.program.dto.api.ProgramApiDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.FileWriter;
import java.net.URI;
import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApiNullDateExtractorRunner implements CommandLineRunner {

    private final ObjectMapper objectMapper;

    @Value("${bizinfo.api.url:https://www.bizinfo.go.kr/uss/rss/bizinfoApi.do}")
    private String apiUrl;

    @Value("${bizinfo.api.key:}")
    private String apiKey;

    @Override
    public void run(String... args) throws Exception {
        log.info(">>>> [NULL 날짜 데이터 전용 JSON 추출 시작] <<<<");

        RestTemplate restTemplate = new RestTemplate();

        URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                .queryParam("crtfcKey", apiKey)
                .queryParam("dataType", "json")
                .build()
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", MediaType.APPLICATION_JSON_VALUE);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            String responseStr = response.getBody();

            if (responseStr == null || responseStr.isBlank()) {
                log.warn("API 응답 데이터가 비어있습니다.");
                return;
            }

            JsonNode rootNode = objectMapper.readTree(responseStr);
            JsonNode targetArrayNode = rootNode.path("jsonArray");
            if (targetArrayNode.has("item")) {
                targetArrayNode = targetArrayNode.path("item");
            }

            if (targetArrayNode.isArray()) {
                // NULL 건만 담을 새로운 JSON Array 생성
                ArrayNode nullDateArray = objectMapper.createArrayNode();

                int totalCount = 0;
                int nullStartCount = 0;
                int nullEndCount = 0;

                for (JsonNode node : targetArrayNode) {
                    totalCount++;
                    // DTO 매핑 및 날짜 파싱 실행
                    ProgramApiDTO dto = objectMapper.treeToValue(node, ProgramApiDTO.class);

                    boolean isStartNull = (dto.getApplyStartDate() == null);
                    boolean isEndNull = (dto.getApplyEndDate() == null);

                    // 시작일이나 종료일 중 하나라도 NULL인 경우 따로 추출
                    if (isStartNull || isEndNull) {
                        if (isStartNull)
                            nullStartCount++;
                        if (isEndNull)
                            nullEndCount++;

                        // 분석 용이성을 위해 원본 JSON Node에 파싱 결과 플래그를 추가하여 저장
                        ObjectNode copyNode = node.deepCopy();
                        copyNode.put("_parsed_applyStartDate",
                                isStartNull ? "NULL" : dto.getApplyStartDate().toString());
                        copyNode.put("_parsed_applyEndDate", isEndNull ? "NULL" : dto.getApplyEndDate().toString());

                        nullDateArray.add(copyNode);
                    }
                }

                // 프로젝트 루트에 null_date_programs.json으로 예쁘게(PrettyPrint) 저장
                try (FileWriter writer = new FileWriter("null_date_programs.json", StandardCharsets.UTF_8)) {
                    objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, nullDateArray);
                }

                log.info(">>>> [추출 완료] 전체: {}건 중 NULL 포함 건수: {}건 (시작일 NULL: {}건 / 종료일 NULL: {}건) <<<<",
                        totalCount, nullDateArray.size(), nullStartCount, nullEndCount);
                log.info(">>>> 결과 파일 저장 위치: null_date_programs.json <<<<");
            }

        } catch (Exception e) {
            log.error("NULL 데이터 추출 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}