import { useEffect, useRef, useState } from 'react'
import { Link, useNavigate, useParams } from 'react-router-dom'
import supportUpLogo from '../assets/support-up-logo.png'
import Button from '../components/common/Button'
import { reviewApi } from '../api/reviewApi'
import { programDetailPath, reviewPath } from '../constants/routes'
import { getApiErrorMessage } from '../utils/authUtils'

const loadingSteps = ['사업정보 확인', '공고문 불러오기', '신청조건 분석', '결과 정리']

function ReviewLoadingPage() {
  const [active, setActive] = useState(0)
  // pending | done | error | not-available: not-available은 이 공고에 파싱된 원문이 없어
  // 재시도해도 항상 실패하는 경우입니다(다른 오류와 구분해 재시도 버튼 대신 안내만 보여줍니다).
  const [requestState, setRequestState] = useState('pending')
  const [errorMessage, setErrorMessage] = useState('')
  const { pblancId } = useParams()
  const navigate = useNavigate()
  const requestedRef = useRef(false)

  // 실제 검수는 언제 끝날지 알 수 없으므로, 응답이 올 때까지 단계 표시를 계속 순환시킵니다.
  useEffect(() => {
    if (requestState !== 'pending') return undefined
    const timer = window.setTimeout(
      () => setActive((current) => (current + 1) % loadingSteps.length),
      900,
    )
    return () => window.clearTimeout(timer)
  }, [active, requestState])

  useEffect(() => {
    if (requestedRef.current) return
    requestedRef.current = true

    reviewApi
      .createReview(pblancId)
      .then(({ data }) => {
        setActive(loadingSteps.length - 1)
        setRequestState('done')
        navigate(reviewPath(pblancId), { replace: true, state: { reviewId: data.reviewId } })
      })
      .catch((error) => {
        if (error.response?.data?.code === 'DATA_NOT_FOUND') {
          setRequestState('not-available')
          setErrorMessage('이 공고는 아직 원문 분석이 준비되지 않아 AI 검수를 이용할 수 없습니다.')
          return
        }
        setRequestState('error')
        setErrorMessage(getApiErrorMessage(error, 'AI 검수 중 문제가 발생했습니다.'))
      })
  }, [pblancId, navigate])

  const retry = () => {
    requestedRef.current = false
    setErrorMessage('')
    setActive(0)
    setRequestState('pending')
  }

  return (
    <main className="loading-page">
      <div className="loading-brand"><img src={supportUpLogo} alt="" /> 지원UP</div>
      <section className="review-loading-card">
        <span className="eyebrow">공고와 내 사업정보를 안전하게 비교하고 있어요</span>
        <h1>AI 신청 적합성 검수</h1>
        <p>
          {requestState === 'error' || requestState === 'not-available'
            ? errorMessage
            : '잠시만 기다려주세요. 공고 원문 전체를 검토해 확인이 필요한 조건과 근거를 정리하고 있습니다.'}
        </p>
        <div className="review-loading-card__progress">
          <span style={{ width: `${((active + 1) / loadingSteps.length) * 100}%` }} />
        </div>
        <ol>
          {loadingSteps.map((item, index) => (
            <li key={item} className={index < active ? 'done' : index === active ? 'active' : ''}>
              <span>{index < active ? '✓' : index === active ? '●' : '○'}</span>
              <div>
                <strong>{item}</strong>
                <small>
                  {requestState === 'error' || requestState === 'not-available'
                    ? '중단됨'
                    : index < active
                      ? '확인 완료'
                      : index === active
                        ? '현재 진행 중'
                        : '다음 단계'}
                </small>
              </div>
            </li>
          ))}
        </ol>
        {requestState === 'error' && (
          <Button size="large" onClick={retry}>다시 시도</Button>
        )}
        {requestState === 'not-available' && (
          <Link className="button button--large" to={programDetailPath(pblancId)}>공고 상세로 돌아가기</Link>
        )}
      </section>
    </main>
  )
}

export default ReviewLoadingPage
