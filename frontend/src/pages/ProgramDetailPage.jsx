import { useEffect, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import { favoriteApi } from '../api/favoriteApi'
import { programApi } from '../api/programApi'
import AppLayout from '../components/common/AppLayout'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import ErrorMessage from '../components/common/ErrorMessage'
import Loading from '../components/common/Loading'
import StatusBadge from '../components/common/StatusBadge'
import { reviewLoadingPath, ROUTES } from '../constants/routes'
import { useBusinessProfileStatus } from '../hooks/useBusinessProfileStatus'
import { formatPeriod, getDday } from '../utils/dateUtils'
import { normalizeProgram } from '../utils/programUtils'

function ProgramDetailPage() {
  const { pblancId } = useParams()
  const navigate = useNavigate()
  const [program, setProgram] = useState(null)
  const [programState, setProgramState] = useState('loading')
  const [favorite, setFavorite] = useState(false)
  const [favoriteLoading, setFavoriteLoading] = useState(false)
  const [requestVersion, setRequestVersion] = useState(0)
  const {
    profileState,
    profileComplete,
    refresh: refreshBusinessProfile,
  } = useBusinessProfileStatus()

  useEffect(() => {
    let active = true

    Promise.all([
      programApi.getProgramDetail(pblancId),
      favoriteApi.getFavorites().catch(() => ({ data: [] })),
    ]).then(([{ data }, { data: favorites }]) => {
      if (!active) return
      console.log('[ProgramDetailPage] program API raw data:', data)
      const normalized = normalizeProgram(data)
      console.log('[ProgramDetailPage] normalized program:', normalized)
      setProgram(normalized)
      setFavorite((Array.isArray(favorites) ? favorites : []).some((item) => item.pblancId === pblancId))
      setProgramState('success')
    }).catch((err) => {
      console.error('[ProgramDetailPage] API error:', err)
      if (active) setProgramState('error')
    })

    return () => {
      active = false
    }
  }, [pblancId, requestVersion])

  // AI 서버가 공고 원문을 파싱해 aiSummary를 만들어 둔 공고만 검수(review)에 필요한
  // 원문 텍스트(program_documents.original_text)가 있습니다. 아직 파싱되지 않은 공고는
  // 검수 API를 호출해도 항상 실패하므로 버튼 단계에서 미리 막습니다.
  const documentAvailable = Boolean(program?.aiSummary)

  const moveToReview = () => {
    if (!profileComplete || !documentAvailable) return
    navigate(reviewLoadingPath(pblancId))
  }

  const toggleFavorite = async () => {
    if (favoriteLoading) return
    setFavoriteLoading(true)
    try {
      if (favorite) await favoriteApi.removeFavorite(pblancId)
      else await favoriteApi.addFavorite(pblancId)
      setFavorite((current) => !current)
    } catch (error) {
      if (!favorite && error.response?.status === 409) setFavorite(true)
      if (favorite && error.response?.status === 404) setFavorite(false)
    } finally {
      setFavoriteLoading(false)
    }
  }

  if (programState === 'loading') {
    return <AppLayout><div className="program-list-state"><Loading label="지원사업 상세정보를 불러오고 있어요." /></div></AppLayout>
  }

  if (programState === 'error' || !program) {
    return (
      <AppLayout>
        <Link className="back-link" to={ROUTES.PROGRAMS}>← 지원사업 찾기</Link>
        <ErrorMessage
          title="지원사업 상세정보를 불러오지 못했습니다."
          description="공고가 삭제되었거나 Backend 연결에 문제가 있을 수 있습니다."
          onRetry={() => {
            setProgramState('loading')
            setRequestVersion((current) => current + 1)
          }}
        />
      </AppLayout>
    )
  }

  const details = program?.aiSummary
    ? [
      ['사업개요', program.aiSummary?.bizSummary || '사업개요 정보가 없습니다.'],
      ['지원대상', program.aiSummary?.targetDescription || program.target || '지원대상 정보가 없습니다.'],
      ['지원내용', program.aiSummary?.supportContent || program.summary || '지원내용 정보가 없습니다.'],
      ['신청방법', program.aiSummary?.applyMethod || '신청방법 정보가 없습니다.'],
      ['준비서류', program.aiSummary?.requiredDocuments || '준비서류 정보가 없습니다.'],
      ['문의처', program.aiSummary?.contactInfo || '문의처 정보가 없습니다.'],
    ]
    : [
      ['지원대상', program?.target || '지원대상 정보가 없습니다.'],
      ['지원내용', program?.summary || '지원내용 정보가 없습니다.'],
      ['공고 정보 갱신일', program?.apiUpdatedAt || '갱신일 정보가 없습니다.'],
    ]

  return (
    <AppLayout>
      <Link className="back-link" to={ROUTES.PROGRAMS}>← 지원사업 찾기</Link>

      <header className="detail-heading">
        <div>
          <span className="eyebrow">{program.category || '분야 정보 없음'}</span>
          <h1>{program.title || '지원사업명 미정'}</h1>
          <p>{program.organization || '기관 정보 없음'}</p>
          <div className="badge-row">
            <StatusBadge tone="info">{program.category || '분야 정보 없음'}</StatusBadge>
            <StatusBadge tone={getDday(program.applicationEndAt) === '마감' ? 'neutral' : 'success'}>{getDday(program.applicationEndAt)}</StatusBadge>
          </div>
        </div>
        <Button variant="secondary" onClick={toggleFavorite} disabled={favoriteLoading} className={favorite ? 'is-favorite' : ''}>
          {favorite ? '♥' : '♡'} {favoriteLoading ? '처리 중...' : '관심공고'}
        </Button>
      </header>

      <Card className="program-overview">
        <h2>공고 요약</h2>
        <dl>
          <div><dt>신청기간</dt><dd>{formatPeriod(program.applicationStartAt, program.applicationEndAt, program.rawApplyPeriod)}</dd></div>
          <div><dt>지원대상</dt><dd>{program.target || '지원대상 정보가 없습니다.'}</dd></div>
          <div><dt>지원내용</dt><dd>{program.summary || '지원내용 정보가 없습니다.'}</dd></div>
        </dl>
      </Card>

      <section className="review-cta">
        <div>
          <span className="review-cta__icon" aria-hidden="true">AI</span>
          <div>
            <h2>AI로 신청 조건을 먼저 확인해보세요</h2>
            <p>
              {profileState === 'loading' && '등록한 사업정보를 확인하고 있습니다.'}
              {profileState === 'incomplete' && 'AI 분석을 이용하려면 먼저 내 정보를 입력해주세요.'}
              {profileState === 'error' && '사업정보를 확인하지 못해 AI 분석을 실행할 수 없습니다.'}
              {profileState === 'complete' && !documentAvailable && '이 공고는 아직 원문 분석이 준비되지 않아 AI 검수를 이용할 수 없습니다.'}
              {profileState === 'complete' && documentAvailable && 'AI가 공고 원문과 내 사업정보를 비교해 신청 적합성을 검수합니다.'}
            </p>
          </div>
        </div>
        <span className="review-cta__actions">
          {profileState === 'incomplete' && (
            <Link className="button button--light button--large" to={ROUTES.PROFILE_EDIT}>내 정보 입력하기</Link>
          )}
          {profileState === 'error' && (
            <Button variant="light" size="large" onClick={refreshBusinessProfile}>다시 확인</Button>
          )}
          <Button size="large" onClick={moveToReview} disabled={!profileComplete || !documentAvailable}>AI 지원 적합성 검수하기</Button>
        </span>
      </section>

      <div className="detail-layout">
        <div className="detail-list">
          {details.map(([label, content]) => (
            <Card key={label} className="detail-row"><h3>{label}</h3><p>{content}</p></Card>
          ))}
        </div>
        <Card className="detail-helper">
          <span className="eyebrow">공고 원문 및 신청</span>
          <h2>공고 원문 안내</h2>
          <p>
            {program.pblancUrl
              ? '주관기관 공식 사이트에서 원문 공고 확인 및 사업 신청이 가능합니다.'
              : '현재 공식 공고 링크가 등록되어 있지 않습니다.'}
          </p>
          {program.pblancUrl && (
            <a
              href={program.pblancUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="button button--primary"
            >
              공고 원문 보러가기 / 신청하기 <span aria-hidden="true">↗</span>
            </a>
          )}
        </Card>
      </div>
    </AppLayout>
  )
}

export default ProgramDetailPage
