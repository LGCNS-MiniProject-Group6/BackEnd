import Reveal from '../common/Reveal'

const steps = [
  ['01', '내 사업정보를 한 번만 입력해요', '업종, 지역, 업력처럼 공고마다 반복해서 확인하던 정보를 안전하게 저장합니다.'],
  ['02', '필요한 공고를 빠르게 찾아요', '수많은 공고 중 내 상황과 관련성이 높은 지원사업부터 차분히 보여드립니다.'],
  ['03', 'AI가 조건과 근거를 비교해요', '공고문과 내 사업정보를 항목별로 대조해 충족·미충족·확인 필요를 구분합니다.'],
  ['04', '준비할 것만 확인하고 신청해요', '놓친 조건과 필요한 서류를 정리한 뒤 원문 신청 페이지로 이동합니다.'],
]

function ProcessSection() {
  return (
    <section className="process-section" id="how-it-works" aria-labelledby="process-title">
      <Reveal className="process-section__intro">
        <span className="eyebrow">HOW IT WORKS</span>
        <h2 id="process-title">복잡한 공고를 읽는 시간은 줄이고,<br />결정에 필요한 근거는 남깁니다.</h2>
      </Reveal>
      <div className="process-list">
        {steps.map(([number, title, description], index) => (
          <Reveal className="process-step" delay={Math.min(index, 3)} key={number}>
            <span>{number}</span>
            <h3>{title}</h3>
            <p>{description}</p>
          </Reveal>
        ))}
      </div>
    </section>
  )
}

export default ProcessSection
