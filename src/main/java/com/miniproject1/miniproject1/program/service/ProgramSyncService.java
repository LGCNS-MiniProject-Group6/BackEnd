package com.miniproject1.miniproject1.program.service;

import com.miniproject1.miniproject1.program.client.ExternalProgramApiClient;
import com.miniproject1.miniproject1.program.dto.api.ProgramApiDTO;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.program.repository.ProgramRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProgramSyncService {

    private final ExternalProgramApiClient externalApiClient;
    private final ProgramRepository programRepository;

    @Transactional
    public void syncPrograms() {
        log.info("=== [PROGRAM-03] Open API 수집 및 DB 저장/동기화 시작 ===");

        List<ProgramApiDTO> apiDataList = externalApiClient.fetchProgramsFromOpenApi();

        int insertedCount = 0;
        int updatedCount = 0;
        int skippedCount = 0;

        for (ProgramApiDTO dto : apiDataList) {
            Optional<Program> existing = programRepository.findByPblancId(dto.getPblancId());

            if (existing.isEmpty()) {
                // 1. 신규 공고 등록 (빌더로 생성된 toEntity 호출)
                Program newProgram = dto.toEntity();
                programRepository.save(newProgram);
                insertedCount++;
            } else {
                Program program = existing.get();

                // 2. 변경 일자 비교 (Null-safe 비교)
                if (!Objects.equals(program.getApiUpdatedAt(), dto.getApiUpdatedAt())) {
                    Program updatedData = dto.toEntity();
                    program.updateFrom(updatedData);

                    updatedCount++;
                } else {
                    skippedCount++;
                }
            }
        }

        log.info("=== [PROGRAM-03 완료] 신규 저장: {}건, 업데이트: {}건, 변경없음: {}건 ===",
                insertedCount, updatedCount, skippedCount);
    }
}