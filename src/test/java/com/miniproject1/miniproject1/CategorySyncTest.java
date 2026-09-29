// package com.miniproject1.miniproject1;

// import com.fasterxml.jackson.databind.JsonNode;
// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.miniproject1.miniproject1.program.entity.Program;
// import com.miniproject1.miniproject1.program.repository.ProgramRepository;
// import jakarta.persistence.EntityManager;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.test.annotation.Rollback;
// import org.springframework.transaction.annotation.Transactional;

// import java.io.File;

// @SpringBootTest
// public class CategorySyncTest {

// @Autowired
// private ProgramRepository programRepository;

// @Autowired
// private ObjectMapper objectMapper;

// @Autowired
// private EntityManager entityManager;

// @Test
// @Transactional
// @Rollback(false) // 💡 실제 DB 반영 (Commit)
// void updateCategoriesFromExtractedRawFile() {
// System.out.println("\n==========================================");
// System.out.println("🚀 [최종 카테고리 동기화] bizinfo_raw_data.json 읽기 시작...");
// System.out.println("==========================================");

// try {
// File rawFile = new File("bizinfo_raw_data.json");

// if (!rawFile.exists()) {
// System.err.println("❌ bizinfo_raw_data.json 파일이 존재하지 않습니다.");
// return;
// }

// JsonNode rootNode = objectMapper.readTree(rawFile);
// JsonNode targetArrayNode = rootNode.path("jsonArray");

// if (!targetArrayNode.isArray() && targetArrayNode.has("item")) {
// targetArrayNode = targetArrayNode.path("item");
// } else if (!targetArrayNode.isArray() && rootNode.has("item")) {
// targetArrayNode = rootNode.path("item");
// } else if (rootNode.isArray()) {
// targetArrayNode = rootNode;
// }

// if (!targetArrayNode.isArray() || targetArrayNode.isEmpty()) {
// System.err.println("❌ 파일 내 유효한 공고 데이터 배열이 없습니다.");
// return;
// }

// System.out.println("🟢 총 " + targetArrayNode.size() + "건의 공고 데이터를 파일에서
// 읽어왔습니다.");

// int totalMatched = 0;
// int totalUpdated = 0;
// int batchCount = 0;

// for (JsonNode node : targetArrayNode) {
// String pblancId = node.path("pblancId").asText(null);
// if (pblancId == null || pblancId.isBlank()) {
// pblancId = node.path("pblanc_id").asText(null);
// }

// // 대분류명 추출
// String lcategory = node.path("pldirSportRealmLclasCodeNm").asText(null);

// if (pblancId == null || pblancId.isBlank()) {
// continue;
// }

// Program program = programRepository.findById(pblancId).orElse(null);

// if (program != null) {
// totalMatched++;
// if (lcategory != null && !lcategory.isBlank()) {
// program.updateCategory(lcategory);
// totalUpdated++;
// batchCount++;

// if (batchCount % 100 == 0) {
// entityManager.flush();
// entityManager.clear();
// }
// }
// }
// }

// entityManager.flush();
// entityManager.clear();

// System.out.println("\n==========================================");
// System.out.println("🎉 [동기화 성공!]");
// System.out.println(" - DB 매칭 성공 건수: " + totalMatched + "건");
// System.out.println(" - 실제 수정된 category 건수: " + totalUpdated + "건");
// System.out.println("==========================================\n");

// } catch (Exception e) {
// System.err.println("❌ 동기화 진행 중 오류 발생: " + e.getMessage());
// e.printStackTrace();
// }
// }
// }