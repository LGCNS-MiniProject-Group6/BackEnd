export const REVIEW_STATUS = {
  MATCHED: 'matched',
  NEED_CHECK: 'needCheck',
  UNMATCHED: 'unmatched',
}

export const REVIEW_STATUS_META = {
  [REVIEW_STATUS.MATCHED]: { label: '충족', icon: '✓', tone: 'success' },
  [REVIEW_STATUS.NEED_CHECK]: {
    label: '추가 확인 필요',
    icon: '!',
    tone: 'warning',
  },
  [REVIEW_STATUS.UNMATCHED]: {
    label: '미충족 가능',
    icon: '×',
    tone: 'danger',
  },
}
