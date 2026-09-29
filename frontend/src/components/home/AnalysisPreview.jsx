function AnalysisPreview() {
  const checks = [
    ['사업자 업력', '3년 이상', '충족'],
    ['사업장 소재지', '서울특별시', '충족'],
    ['지원 업종', '생활 서비스업', '충족'],
    ['필수 증빙', '매출 증빙자료', '확인 필요'],
  ]

  return (
    <div className="analysis-preview" aria-label="AI 신청 가능성 분석 예시">
      <div className="analysis-preview__topbar">
        <div className="analysis-preview__window" aria-hidden="true"><i /><i /><i /></div>
        <span><i className="analysis-preview__live" /> AI 분석 중</span>
      </div>

      <div className="analysis-preview__content">
        <div className="analysis-preview__heading">
          <div>
            <span>공고 조건 12개 항목 비교</span>
            <h2>경영환경 개선 지원사업</h2>
          </div>
          <strong>87<small>%</small></strong>
        </div>

        <div className="analysis-preview__meter" aria-hidden="true">
          <span />
        </div>

        <div className="analysis-preview__checks">
          {checks.map(([label, value, status], index) => (
            <div className={`analysis-check analysis-check--${index + 1}`} key={label}>
              <span className={status === '충족' ? 'analysis-check__icon is-done' : 'analysis-check__icon'}>
                {status === '충족' ? '✓' : '!'}
              </span>
              <div><small>{label}</small><strong>{value}</strong></div>
              <em>{status}</em>
            </div>
          ))}
        </div>

        <div className="analysis-preview__result">
          <span>AI 검수 결과</span>
          <strong>신청 가능성이 높아요</strong>
          <p>필수 서류 한 가지만 더 준비하면 신청할 수 있어요.</p>
        </div>
      </div>
    </div>
  )
}

export default AnalysisPreview
