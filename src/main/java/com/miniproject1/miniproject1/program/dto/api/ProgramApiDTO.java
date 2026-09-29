package com.miniproject1.miniproject1.program.dto.api;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.miniproject1.miniproject1.program.entity.Program;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProgramApiDTO {

    @JsonProperty("pblancId")
    private String pblancId;

    @JsonProperty("pblancNm")
    private String title;

    @JsonProperty("category") // 1. 프론트엔드로 응답할 때 JSON 키 이름
    @JsonAlias("pldirSportRealmLclasCodeNm") // 2. 공공 API에서 수신할 때 키 이름 (대분류만 지정)
    private String category;

    @JsonProperty("jrsdInsttNm")
    private String organization;

    @JsonProperty("trgetNm")
    private String targetDescription;

    @JsonProperty("bsnsSumryCn")
    private String description;

    @JsonProperty("reqstBeginEndDe")
    @JsonAlias({ "reqstBeginEndDe", "reqstPurps", "reqstBeginDe", "reqstEndDe", "period", "applyPeriod" })
    private String reqstBeginEndDe;

    @JsonProperty("updtPnttm")
    @JsonAlias({ "creatPnttm" })
    private String apiUpdatedAt;

    // ★ 공고 상세 URL
    @JsonProperty("pblancUrl")
    @JsonAlias({ "pblancUrl", "dtlUrl", "url" })
    private String pblancUrl;

    // ★ 기업마당 실제 첨부파일/서식 경로 필드 (printFlpth 등 모든 변형 매핑)
    // ★ 기업마당 API 실제 첨부파일 경로 필드: printFlpthNm
    @JsonProperty("printFlpthNm")
    @JsonAlias({ "printFlpthNm" })
    private String printFilePathUrl;

    public LocalDate getApplyStartDate() {
        return parseDate(0);
    }

    public LocalDate getApplyEndDate() {
        return parseDate(1);
    }

    private LocalDate parseDate(int index) {
        if (this.reqstBeginEndDe == null || this.reqstBeginEndDe.isBlank()) {
            return null;
        }

        Pattern pattern = Pattern.compile("(20\\d{2})[-./]?(0[1-9]|1[0-2])[-./]?(0[1-9]|[12]\\d|3[01])");
        Matcher matcher = pattern.matcher(this.reqstBeginEndDe);

        List<LocalDate> parsedDates = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd");

        while (matcher.find()) {
            String dateStr = matcher.group().replaceAll("[^0-9]", "");
            if (dateStr.length() == 8) {
                try {
                    parsedDates.add(LocalDate.parse(dateStr, formatter));
                } catch (Exception ignored) {
                }
            }
        }

        if (parsedDates.size() > index) {
            return parsedDates.get(index);
        }

        return null;
    }

    private String cleanHtml(String text) {
        if (text == null || text.isBlank()) {
            return text;
        }
        return Jsoup.parse(text).text().trim();
    }

    public Program toEntity() {
        return Program.builder()
                .pblancId(this.pblancId)
                .title(cleanHtml(this.title))
                .category(this.category)
                .organization(this.organization)
                .targetDescription(cleanHtml(this.targetDescription))
                .description(cleanHtml(this.description))
                .rawApplyPeriod(this.reqstBeginEndDe)
                .applyStartDate(getApplyStartDate())
                .applyEndDate(getApplyEndDate())
                .apiUpdatedAt(this.apiUpdatedAt)
                .pblancUrl(this.pblancUrl)
                .printFilePathUrl(this.printFilePathUrl)
                .build();
    }
}