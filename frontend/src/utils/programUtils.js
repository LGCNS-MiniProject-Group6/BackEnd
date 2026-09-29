export function normalizeProgram(program) {
  if (!program) return null

  // 순수 JavaScript: aiSummary 중첩 객체 또는 루트 필드에서 데이터 추출
  const rawAi =
    program.aiSummary ||
    program.ai_summary ||
    (program.bizSummary || program.supportContent || program.applyMethod
      ? program
      : null)

  const aiSummary = rawAi
    ? {
        bizSummary: rawAi.bizSummary || rawAi.biz_summary || '',
        targetDescription: rawAi.targetDescription || rawAi.target_description || program.targetDescription || '',
        supportContent: rawAi.supportContent || rawAi.support_content || program.description || '',
        applyMethod: rawAi.applyMethod || rawAi.apply_method || '',
        requiredDocuments: rawAi.requiredDocuments || rawAi.required_documents || '',
        contactInfo: rawAi.contactInfo || rawAi.contact_info || '',
        updatedAt: rawAi.updatedAt || rawAi.updated_at || '',
      }
    : null

  return {
    ...program,
    pblancId: program.pblancId,
    title: program.title,
    category: program.category,
    organization: program.organization,
    target: program.targetDescription,
    summary: program.description,
    applicationStartAt: program.applyStartDate,
    applicationEndAt: program.applyEndDate,
    rawApplyPeriod: program.rawApplyPeriod,
    apiUpdatedAt: program.apiUpdatedAt,
    pblancUrl: program.pblancUrl || program.pblanc_url || null,
    aiSummary,
  }
}

export function normalizeProgramPage(responseData) {
  const content = Array.isArray(responseData)
    ? responseData
    : responseData?.content ?? []

  return content.map(normalizeProgram).filter(Boolean)
}
