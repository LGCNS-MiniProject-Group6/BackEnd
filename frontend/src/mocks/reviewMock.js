import { REVIEW_STATUS } from '../constants/reviewStatus'

export const reviewMock = {
  reviewId: 1,
  pblancId: 'online-2026',
  status: REVIEW_STATUS.NEED_CHECK,
  summary: '일부 조건 확인이 필요해요',
  description:
    '현재 입력된 사업정보 기준으로 주요 조건 대부분과 일치하지만 2개의 추가 확인 항목이 있습니다.',
  conditions: [
    {
      id: 'region',
      item: '지역',
      status: REVIEW_STATUS.MATCHED,
      myInfo: '서울특별시',
      requirement: '서울 소재 사업자',
      reason: '등록된 사업장 주소와 공고 대상 지역이 동일해 조건을 충족합니다.',
    },
    {
      id: 'period',
      item: '업력',
      status: REVIEW_STATUS.MATCHED,
      myInfo: '약 3년',
      requirement: '업력 1년 이상',
      reason: '등록된 개업일 기준으로 공고의 최소 업력 조건을 넘습니다.',
    },
    {
      id: 'duplicate',
      item: '중복지원',
      status: REVIEW_STATUS.NEED_CHECK,
      myInfo: '정보 없음',
      requirement: '최근 3년 동일·유사 사업 수혜자 제외',
      reason: '과거 지원 이력이 입력되지 않아 직접 확인이 필요합니다.',
    },
    {
      id: 'tax',
      item: '세금 체납',
      status: REVIEW_STATUS.NEED_CHECK,
      myInfo: '정보 없음',
      requirement: '국세·지방세 체납 사업자 제외 가능',
      reason: '체납 여부는 현재 사업정보만으로 확인할 수 없습니다.',
    },
    {
      id: 'revenue',
      item: '매출 세부기준',
      status: REVIEW_STATUS.UNMATCHED,
      myInfo: '연 매출 1억 원',
      requirement: '공고 원문의 세부 기준 별도 확인',
      reason: '매출 조건이 공고문에서 모호해 원문 확인이 필요합니다.',
    },
  ],
  warnings: ['최근 동일·유사 사업 참여 여부', '국세·지방세 체납 여부', '세부 매출 기준'],
  documents: ['사업자등록증', '매출 증빙자료', '지원 신청서', '기타 공고 지정 서류'],
  applicationStartAt: '2026-09-01',
  applicationEndAt: '2026-09-30',
}
