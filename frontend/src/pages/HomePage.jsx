import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { favoriteApi } from '../api/favoriteApi'
import { programApi } from '../api/programApi'
import AppLayout from '../components/common/AppLayout'
import ErrorMessage from '../components/common/ErrorMessage'
import Loading from '../components/common/Loading'
import ProgramFilter from '../components/program/ProgramFilter'
import ProgramSearchBar from '../components/program/ProgramSearchBar'
import ProgramCard from '../components/program/ProgramCard'
import Reveal from '../components/common/Reveal'
import AnalysisPreview from '../components/home/AnalysisPreview'
import ProcessSection from '../components/home/ProcessSection'
import { ROUTES } from '../constants/routes'
import { useAuth } from '../hooks/useAuth'
import { normalizeProgramPage } from '../utils/programUtils'

function HomePage() {
  const [query, setQuery] = useState('')
  const [category, setCategory] = useState('전체')
  const [latestPrograms, setLatestPrograms] = useState([])
  const [favoriteIds, setFavoriteIds] = useState(() => new Set())
  const [programState, setProgramState] = useState('loading')
  const [requestVersion, setRequestVersion] = useState(0)
  const navigate = useNavigate()
  const { user, business, refreshSession } = useAuth()

  useEffect(() => {
    refreshSession().catch(() => undefined)
  }, [refreshSession])

  useEffect(() => {
    let active = true
    Promise.all([
      programApi.getPrograms({ page: 0, size: 3, sort: 'applyStartDate,desc' }),
      favoriteApi.getFavorites().catch(() => ({ data: [] })),
    ]).then(([{ data: programs }, { data: favorites }]) => {
      if (!active) return
      setLatestPrograms(normalizeProgramPage(programs))
      setFavoriteIds(new Set((Array.isArray(favorites) ? favorites : []).map((favorite) => favorite.pblancId)))
      setProgramState('success')
    }).catch(() => {
      if (active) setProgramState('error')
    })
    return () => {
      active = false
    }
  }, [requestVersion])

  const updateFavorite = (pblancId, isFavorite) => {
    setFavoriteIds((current) => {
      const next = new Set(current)
      if (isFavorite) next.add(pblancId)
      else next.delete(pblancId)
      return next
    })
  }

  const search = (event) => {
    event.preventDefault()
    navigate(`${ROUTES.PROGRAMS}?q=${encodeURIComponent(query)}&category=${encodeURIComponent(category)}`)
  }

  return (
    <AppLayout className="home-page">
      <section className="home-hero">
        <Reveal className="home-hero__content">
          <span className="home-hero__signal"><i /> 공고를 읽기 전에, 가능성부터 확인하세요</span>
          <h1><span>내 사업에 맞는 지원금,</span><br /><em>놓치지 않도록.</em></h1>
          <p>흩어진 정부지원사업을 찾고, 복잡한 신청 조건을 AI가 비교해요. 이제 공고문을 처음부터 끝까지 읽기 전에 신청 가능성을 확인하세요.</p>
          <div className="home-hero__actions">
            <Link className="button button--primary button--large" to={ROUTES.PROGRAMS}>내 지원사업 찾기 <span aria-hidden="true">→</span></Link>
            <a className="home-hero__text-link" href="#how-it-works">어떻게 작동하나요?</a>
          </div>
          <div className="home-hero__proof" aria-label="서비스 핵심 기능">
            <span><strong>01</strong>맞춤 공고 탐색</span>
            <span><strong>02</strong>AI 조건 비교</span>
            <span><strong>03</strong>서류 체크</span>
          </div>
        </Reveal>
        <Reveal className="home-hero__visual" variant="scale" delay={1}>
          <AnalysisPreview />
          <span className="home-hero__float home-hero__float--match"><b>12개</b> 조건 분석 완료</span>
          <span className="home-hero__float home-hero__float--document"><b>1개</b> 서류 확인 필요</span>
        </Reveal>
      </section>

      <Reveal as="section" className="discovery-panel" variant="up">
        <div className="discovery-panel__intro">
          <span className="eyebrow">DISCOVER</span>
          <h2>어떤 지원이 필요하세요?</h2>
          <p>관심 분야를 선택하거나 키워드로 바로 찾아보세요.</p>
        </div>
        <div className="discovery-panel__search">
          <ProgramSearchBar value={query} onChange={setQuery} onSubmit={search} placeholder="온라인 마케팅, 시설개선, 교육, 판로지원 검색" />
          <ProgramFilter selected={category} onSelect={setCategory} compact />
        </div>
        <aside className="business-snapshot">
          <div><span>내 사업 프로필</span><strong>{business?.industry || '사업정보 미입력'}</strong></div>
          <dl>
            <div><dt>지역</dt><dd>{business?.region || '미입력'}</dd></div>
            <div><dt>개업일</dt><dd>{business?.openingDate || '미입력'}</dd></div>
          </dl>
          <Link to={ROUTES.MY_PAGE} aria-label="사업정보 확인하기">→</Link>
        </aside>
      </Reveal>

      <section className="problem-section" aria-labelledby="problem-title">
        <Reveal className="problem-section__statement">
          <span className="eyebrow">THE PROBLEM</span>
          <h2 id="problem-title">지원사업이 없는 게 아니라,<br /><em>찾고 판단하기가 어려웠습니다.</em></h2>
        </Reveal>
        <div className="problem-list">
          <Reveal className="problem-item" delay={1}><span>01</span><h3>공고는 너무 많고</h3><p>여러 기관에 흩어진 공고 중 내 사업에 맞는 것을 찾기 어렵습니다.</p></Reveal>
          <Reveal className="problem-item" delay={2}><span>02</span><h3>조건은 모호하고</h3><p>긴 공고문 속 자격 조건을 하나씩 대조하다 보면 중요한 기준을 놓칩니다.</p></Reveal>
          <Reveal className="problem-item" delay={3}><span>03</span><h3>준비물은 늦게 보여요</h3><p>막상 신청하려 할 때 필요한 서류나 제외 조건을 뒤늦게 발견합니다.</p></Reveal>
        </div>
      </section>

      <ProcessSection />

      <section className="analysis-story" id="analysis" aria-labelledby="analysis-story-title">
        <Reveal className="analysis-story__copy">
          <span className="eyebrow eyebrow--light">AI REVIEW</span>
          <h2 id="analysis-story-title">“가능해요”에서 끝나지 않고,<br />왜 가능한지 보여드려요.</h2>
          <p>지원UP은 내 사업정보와 실제 공고문을 항목별로 비교합니다. 판단 근거와 확인할 부분이 함께 남기 때문에 다음 행동이 선명해집니다.</p>
          <Link to={ROUTES.PROGRAMS}>AI 검수할 공고 찾기 <span aria-hidden="true">→</span></Link>
        </Reveal>
        <Reveal className="analysis-story__product" variant="scale" delay={1}>
          <div className="result-summary">
            <span>신청 가능성</span>
            <strong>높음 <em>87%</em></strong>
            <p>핵심 자격 4개 중 3개를 충족했어요.</p>
          </div>
          <div className="result-evidence">
            <div><span className="is-pass">충족</span><p><strong>지역 조건</strong> 서울 소재 사업장</p></div>
            <div><span className="is-pass">충족</span><p><strong>업력 조건</strong> 공고일 기준 3년 이상</p></div>
            <div><span className="is-check">확인</span><p><strong>매출 증빙</strong> 최근 과세표준증명원 필요</p></div>
          </div>
          <div className="result-next"><span>다음 할 일</span><strong>매출 증빙자료 준비하기</strong><em>01</em></div>
        </Reveal>
      </section>

      <section className="recommendation-section">
        <Reveal className="section-heading section-heading--editorial">
          <div><span className="eyebrow">LATEST PROGRAMS</span><h2>{user?.name ?? '사용자'}님이<br />확인할 수 있는 최신 공고</h2></div>
          <div><p>현재 Backend에 등록된 지원사업 중 최근 공고를 보여드려요.</p><Link to={ROUTES.PROGRAMS}>전체 공고 보기 →</Link></div>
        </Reveal>
        <div className="program-grid program-grid--home">
          {programState === 'loading' ? (
            <Loading label="최신 지원사업을 불러오고 있어요." />
          ) : programState === 'error' ? (
            <ErrorMessage title="최신 지원사업을 불러오지 못했습니다." onRetry={() => {
              setProgramState('loading')
              setRequestVersion((current) => current + 1)
            }} />
          ) : latestPrograms.map((program, index) => (
            <Reveal variant="up" delay={index} key={program.pblancId}>
              <ProgramCard
                program={{ ...program, isFavorite: favoriteIds.has(program.pblancId) }}
                featured
                onFavoriteChange={updateFavorite}
              />
            </Reveal>
          ))}
        </div>
      </section>

      <section className="benefit-section" aria-labelledby="benefit-title">
        <Reveal className="benefit-section__headline">
          <span className="eyebrow">BUILT FOR CLARITY</span>
          <h2 id="benefit-title">지원금 신청의<br />불확실성을 줄이는 방법</h2>
        </Reveal>
        <div className="benefit-list">
          <Reveal className="benefit-item"><span>맞춤 탐색</span><h3>내 조건에서 시작</h3><p>인기순이 아니라 업종·지역·업력을 기준으로 관련 공고를 먼저 봅니다.</p></Reveal>
          <Reveal className="benefit-item" delay={1}><span>근거 있는 분석</span><h3>판단 과정까지 투명하게</h3><p>충족 여부뿐 아니라 어떤 정보와 조건을 비교했는지 함께 확인합니다.</p></Reveal>
          <Reveal className="benefit-item" delay={2}><span>실행 가능한 안내</span><h3>다음 행동을 구체적으로</h3><p>더 확인할 조건과 준비할 서류를 목록으로 정리해 바로 신청을 준비합니다.</p></Reveal>
        </div>
      </section>

      <Reveal as="section" className="home-cta" variant="scale">
        <span>READY WHEN YOU ARE</span>
        <h2>받을 수 있는 지원,<br />이제 놓치지 마세요.</h2>
        <p>사업정보를 등록하면 나에게 맞는 공고와 신청 가능성을 더 정확하게 확인할 수 있어요.</p>
        <div>
          <Link className="button button--light button--large" to={ROUTES.MY_PAGE}>내 사업정보 확인하기</Link>
          <Link className="home-cta__link" to={ROUTES.PROGRAMS}>공고부터 둘러보기 →</Link>
        </div>
      </Reveal>

    </AppLayout>
  )
}

export default HomePage
