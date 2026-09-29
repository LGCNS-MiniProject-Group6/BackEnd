package com.miniproject1.miniproject1.review.service;

import com.miniproject1.miniproject1.business.entity.BusinessInfo;
import com.miniproject1.miniproject1.program.entity.Program;
import com.miniproject1.miniproject1.review.entity.ReviewConditionStatus;

import java.util.List;

public interface ReviewAnalyzer {

    ReviewAnalysis analyze(Program program, BusinessInfo businessInfo);

    record ReviewAnalysis(String overallStatus, String model, String promptVersion,
                          String documentHash, List<ConditionAnalysis> conditions) {
    }

    record ConditionAnalysis(String conditionType, ReviewConditionStatus status,
                             String reason, Integer page, String evidenceText) {
    }
}
