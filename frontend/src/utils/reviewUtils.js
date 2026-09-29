import { REVIEW_STATUS } from '../constants/reviewStatus'

const STATUS_KEY_MAP = {
  MATCHED: REVIEW_STATUS.MATCHED,
  UNMATCHED: REVIEW_STATUS.UNMATCHED,
  NEED_CHECK: REVIEW_STATUS.NEED_CHECK,
}

const STATUS_SUMMARY_TEXT = {
  [REVIEW_STATUS.MATCHED]: '모든 조건을 충족하고 있어요',
  [REVIEW_STATUS.NEED_CHECK]: '일부 조건 확인이 필요해요',
  [REVIEW_STATUS.UNMATCHED]: '충족하지 못하는 조건이 있어요',
}

export function toReviewStatusKey(status) {
  return STATUS_KEY_MAP[status] ?? REVIEW_STATUS.NEED_CHECK
}

// Backend의 AiReviewDetailResponse를 화면 컴포넌트가 쓰는 형태로 변환합니다.
export function normalizeReview(apiReview) {
  const conditions = (apiReview?.conditions ?? []).map((condition, index) => ({
    id: `${condition.condition || 'condition'}-${index}`,
    item: condition.condition || '공고 요건 확인',
    status: toReviewStatusKey(condition.status),
    myInfo: condition.userValue || '정보 없음',
    requirement: condition.requirement || '공고 원문 참고',
    reason: condition.reason || '',
    evidence: condition.evidence || '',
  }))

  const countByStatus = (status) => conditions.filter((item) => item.status === status).length
  const matchedCount = countByStatus(REVIEW_STATUS.MATCHED)
  const needCheckCount = countByStatus(REVIEW_STATUS.NEED_CHECK)
  const unmatchedCount = countByStatus(REVIEW_STATUS.UNMATCHED)

  const status = toReviewStatusKey(apiReview?.status)

  return {
    reviewId: apiReview?.reviewId,
    pblancId: apiReview?.pblancId,
    status,
    summary: STATUS_SUMMARY_TEXT[status] ?? STATUS_SUMMARY_TEXT[REVIEW_STATUS.NEED_CHECK],
    description:
      conditions.length === 0
        ? '공고문에서 판정 가능한 자격 요건을 찾지 못했습니다.'
        : `전체 ${conditions.length}개 조건 중 충족 ${matchedCount}개, 추가 확인 ${needCheckCount}개, 미충족 가능 ${unmatchedCount}개로 분석됐습니다.`,
    conditions,
    matchedCount,
    needCheckCount,
    unmatchedCount,
    warnings: (apiReview?.warnings ?? []).filter((item) => item?.text),
    documents: (apiReview?.documents ?? []).filter((item) => item?.text),
    createdAt: apiReview?.createdAt,
  }
}
