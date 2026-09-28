package com.miniproject1.miniproject1;

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
public class ApiRawDataExtractorRunner implements CommandLineRunner {

    // .env에서 등록해둔 설정값 그대로 활용
    @Value("${biziinfo.api.url:https://www.bizinfo.go.kr/uss/rss/bizinfoApi.do}")
    private String apiUrl;

    @Value("${biziinfo.api.key}")
    private String apiKey;

    @Override
    public void run(String... args) {
        log.info(">>>> [Spring Boot 구동 시점] 원본 Open API JSON 파일 추출 시작 <<<<");

        RestTemplate restTemplate = new RestTemplate();

        try {
            URI uri = UriComponentsBuilder.fromUriString(apiUrl)
                    .queryParam("crtfcKey", apiKey)
                    .queryParam("dataType", "json")
                    .queryParam("searchCnt", 0) // 필요 시 파라미터 조정
                    .build()
                    .toUri();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE);
            HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(uri, HttpMethod.GET, requestEntity, String.class);
            String responseStr = response.getBody();

            if (responseStr == null || responseStr.isBlank()) {
                log.warn("[RawDataExtractor] API 응답 데이터가 비어있습니다.");
                return;
            }

            // 루트 경로에 bizinfo_raw_data.json으로 저장
            try (FileWriter writer = new FileWriter("bizinfo_raw_data.json", StandardCharsets.UTF_8)) {
                writer.write(responseStr);
            }

            log.info(">>>> [추출 완료] 원본 API 응답 저장 완료 -> 파일명: bizinfo_raw_data.json <<<<");

        } catch (Exception e) {
            log.error("[RawDataExtractor] 원본 데이터 추출 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}