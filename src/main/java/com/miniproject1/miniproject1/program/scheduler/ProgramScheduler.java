package com.miniproject1.miniproject1.program.scheduler;

import com.miniproject1.miniproject1.program.service.ProgramSyncService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProgramScheduler {

    private final ProgramSyncService programSyncService;

    /**
     * 매일 오전 06:00 정기 공고 수집 및 동기화
     * Cron 표현식: 초 분 시 일 월 요일
     */
    @Scheduled(cron = "0 0 6 * * *")
    public void runProgramSync() {
        log.info("[Scheduler] === 정기 기업마당 공고 수집 스케줄러 시작 ===");
        try {
            programSyncService.syncPrograms();
            log.info("[Scheduler] === 정기 기업마당 공고 수집 스케줄러 완료 ===");
        } catch (Exception e) {
            log.error("[Scheduler] 정기 수집 중 오류 발생: {}", e.getMessage(), e);
        }
    }
}