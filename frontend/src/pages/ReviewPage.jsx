import { useEffect, useState } from 'react'
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom'
import { programApi } from '../api/programApi'
import { reviewApi } from '../api/reviewApi'
import AppLayout from '../components/common/AppLayout'
import Button from '../components/common/Button'
import Card from '../components/common/Card'
import ErrorMessage from '../components/common/ErrorMessage'
import Loading from '../components/common/Loading'
import Modal from '../components/common/Modal'
import ChatPanel from '../components/chat/ChatPanel'
import ReviewConditionCard from '../components/review/ReviewConditionCard'
import ReviewSummary from '../components/review/ReviewSummary'
import { programDetailPath, reviewLoadingPath } from '../constants/routes'
import { normalizeProgram } from '../utils/programUtils'
import { normalizeReview } from '../utils/reviewUtils'

async function findLatestReviewForProgram(pblancId) {
  const { data } = await reviewApi.getReviews({ page: 0, size: 50 })
  const matches = (data?.content ?? []).filter((item) => item.pblancId === pblancId)
  if (matches.length === 0) return null
  const latest = matches.reduce((a, b) => (new Date(a.createdAt) > new Date(b.createdAt) ? a : b))
  return latest.reviewId
}

function ReviewPage() {
  const [saveModalOpen, setSaveModalOpen] = useState(false)
  const [saved, setSaved] = useState(false)
  const [program, setProgram] = useState(null)
  const [review, setReview] = useState(null)
  const [pageState, setPageState] = useState('loading') // loading | success | error | empty
  const [requestVersion, setRequestVersion] = useState(0)
  const { pblancId } = useParams()
  const location = useLocation()
  const navigate = useNavigate()

  useEffect(() => {
    let active = true

    const loadReviewId = async () => {
      const stateReviewId = location.state?.reviewId
      if (stateReviewId) return stateReviewId
      return findLatestReviewForProgram(pblancId)
    }

    Promise.all([programApi.getProgramDetail(pblancId), loadReviewId()])
      .then(async ([{ data: programData }, reviewId]) => {
        if (!active) return
        setProgram(normalizeProgram(programData))

        if (!reviewId) {
          setPageState('empty')
          return
        }

        const { data: reviewData } = await reviewApi.getReviewDetail(reviewId)
        if (!active) return
        setReview(normalizeReview(reviewData))
        setPageState('success')
      })
      .catch(() => {
        if (active) setPageState('error')
      })

    return () => {
      active = false
    }
  }, [pblancId, location.state, requestVersion])

  const saveReview = () => {
    setSaved(true)
    setSaveModalOpen(false)
  }

  if (pageState === 'loading') {
    return (
      <AppLayout>
        <div className="program-list-state"><Loading label="검수 결과를 불러오고 있어요." /></div>
      </AppLayout>
    )
  }

  if (pageState === 'error') {
    return (
      <AppLayout>
        <Link className="back-link" to={programDetailPath(pblancId)}>← 공고 상세로</Link>
        <ErrorMessage
          title="검수 결과를 불러오지 못했습니다."
          description="Backend 연결에 문제가 있을 수 있습니다. 잠시 후 다시 시도해주세요."
          onRetry={() => {
            setPageState('loading')
            setRequestVersion((current) => current + 1)
          }}
        />
      </AppLayout>
    )
  }

  if (pageState === 'empty' || !review) {
    return (
      <AppLayout>
        <Link className="back-link" to={programDetailPath(pblancId)}>← 공고 상세로</Link>
        <ErrorMessage
          title="검수 결과가 없습니다."
          description="공고 상세 화면에서 AI 검수를 먼저 실행해주세요."
          actionLabel="AI 검수 실행하기"
          onRetry={() => navigate(reviewLoadingPath(pblancId))}
        />
      </AppLayout>
    )
  }

  return (
    <AppLayout className="review-page-container">
      <Link className="back-link" to={programDetailPath(pblancId)}>← 공고 상세로</Link>
      <header className="page-heading page-heading--review">
        <span className="eyebrow">AI 검수 완료</span>
        <h1>AI 신청 적합성 검수 결과</h1>
        <p>{program?.title}</p>
      </header>

      <div className="review-layout">
        <div className="review-content">
          <ReviewSummary review={review} />

          <section className="review-section">
            <div className="section-heading section-heading--compact">
              <h2>조건별 검수</h2>
              <p>내 정보 · 공고 조건 · 판단 근거를 함께 확인하세요.</p>
            </div>
            <div className="condition-list">
              {review.conditions.length === 0 ? (
                <Card className="condition-card">공고문에서 판정 가능한 자격 요건을 찾지 못했습니다.</Card>
              ) : (
                review.conditions.map((condition) => (
                  <ReviewConditionCard key={condition.id} condition={condition} />
                ))
              )}
            </div>
          </section>

          <div className="review-notes-grid">
            <Card className="review-note review-note--warning">
              <h2>신청 전에 꼭 확인하세요</h2>
              {review.warnings.length === 0 ? (
                <p>공고문에서 확인된 별도 주의사항이 없습니다.</p>
              ) : (
                <ul>
                  {review.warnings.map((item, index) => (
                    <li key={index} title={item.evidence || undefined}>{item.text}</li>
                  ))}
                </ul>
              )}
            </Card>
            <Card className="review-note review-note--info">
              <h2>공고에서 확인된 준비서류</h2>
              {review.documents.length === 0 ? (
                <p>공고문에서 확인된 준비서류 안내가 없습니다.</p>
              ) : (
                <ul>
                  {review.documents.map((item, index) => (
                    <li key={index} title={item.evidence || undefined}>{item.text}</li>
                  ))}
                </ul>
              )}
            </Card>
          </div>

          <p className="review-disclaimer">
            AI 검수 결과는 신청 전 확인을 돕기 위한 참고 정보이며, 최종 신청 자격은 반드시 원문 공고를 확인해주세요.
          </p>
          <div className="review-actions">
            <Button size="large" onClick={() => setSaveModalOpen(true)} disabled={saved}>
              {saved ? '검수 결과 저장됨' : '검수 결과 저장'}
            </Button>
            {program?.pblancUrl && (
              <a className="button button--secondary button--large" href={program.pblancUrl} target="_blank" rel="noreferrer">
                원문 공고 보기 ↗
              </a>
            )}
          </div>
        </div>

        <ChatPanel pblancId={pblancId} programTitle={program?.title} />
      </div>

      <Modal
        open={saveModalOpen}
        title="검수 결과를 저장할까요?"
        description="마이페이지의 AI 검수기록에서 언제든 다시 확인할 수 있습니다."
        confirmLabel="저장하기"
        onConfirm={saveReview}
        onClose={() => setSaveModalOpen(false)}
      />
    </AppLayout>
  )
}

export default ReviewPage
