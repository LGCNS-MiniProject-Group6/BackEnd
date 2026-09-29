import { useState } from 'react'
import { Link } from 'react-router-dom'
import { programDetailPath } from '../../constants/routes'
import Button from '../common/Button'
import Card from '../common/Card'
import StatusBadge from '../common/StatusBadge'
import ReviewStatusBadge from '../review/ReviewStatusBadge'
import { formatPeriod, getDday } from '../../utils/dateUtils'
import { favoriteApi } from '../../api/favoriteApi'
import { toReviewStatusKey } from '../../utils/reviewUtils'

function ProgramCard({ program, featured = false, onFavoriteChange }) {
  const {
    pblancId,
    title = '지원사업명 미정',
    category = '분야 정보 없음',
    organization = '기관 정보 없음',
    applicationStartAt,
    applicationEndAt,
    rawApplyPeriod,
    target = '지원대상 정보 없음',
    summary = '지원내용 정보가 아직 등록되지 않았습니다.',
    isFavorite: initialIsFavorite = false,
    recommendationScore = null,
    matchReason = null,
    reviewStatus = null,
  } = program ?? {}
  const dDay = getDday(applicationEndAt)

  const [isFavorite, setIsFavorite] = useState(initialIsFavorite)
  const [loading, setLoading] = useState(false)
  const displayedFavorite = onFavoriteChange ? initialIsFavorite : isFavorite

  const handleToggleFavorite = async (e) => {
    e.preventDefault() // Link 등 상위 요소와 겹칠 경우 대비
    if (loading) return
    setLoading(true)

    const prevState = displayedFavorite
    setIsFavorite(!prevState) // 낙관적 업데이트

    try {
      if (prevState) {
        await favoriteApi.removeFavorite(pblancId)
      } else {
        await favoriteApi.addFavorite(pblancId)
      }
      onFavoriteChange?.(pblancId, !prevState)
    } catch (err) {
      if (!prevState && err.response?.status === 409) {
        setIsFavorite(true)
        onFavoriteChange?.(pblancId, true)
      } else if (prevState && err.response?.status === 404) {
        setIsFavorite(false)
        onFavoriteChange?.(pblancId, false)
      } else {
        setIsFavorite(prevState) // 실패 시 롤백
      }
    } finally {
      setLoading(false)
    }
  }

  return (
    <Card as="article" className={featured ? 'program-card program-card--featured' : 'program-card'}>
      <div className="program-card__meta">
        <StatusBadge tone="info">{category}</StatusBadge>
        <StatusBadge tone={dDay === '마감' ? 'neutral' : 'success'}>{dDay}</StatusBadge>
        {/* 추천 점수(recommendationScore)와 과거 개별 검수 결과(reviewStatus)는 서로 다른 판단이라
            함께 표시하면 모순돼 보이므로, 추천 카드에서는 검수 이력 배지를 숨깁니다. */}
        {reviewStatus && recommendationScore === null && (
          <ReviewStatusBadge status={toReviewStatusKey(reviewStatus)} />
        )}
      </div>
      <h3>{title}</h3>
      <dl className="program-card__details">
        <div><dt>기관</dt><dd>{organization}</dd></div>
        <div><dt>신청기간</dt><dd>{formatPeriod(applicationStartAt, applicationEndAt, rawApplyPeriod)}</dd></div>
        <div><dt>지원대상</dt><dd>{target}</dd></div>
      </dl>
      <div className="program-card__support"><span>지원내용</span><p>{summary}</p></div>
      {matchReason && (
        <div className="program-card__support program-card__support--ai">
          <span>AI 추천 이유</span>
          <p>{matchReason}</p>
        </div>
      )}
      <div className="program-card__actions">
        <Button
          variant="secondary"
          onClick={handleToggleFavorite}
          disabled={loading}
          className={displayedFavorite ? 'is-favorite' : ''}
        >
          {displayedFavorite ? '♥' : '♡'} 관심공고
        </Button>
        <Link className="button button--primary button--medium" to={programDetailPath(pblancId)}>
          상세보기 <span aria-hidden="true">→</span>
        </Link>
      </div>
    </Card>
  )
}

export default ProgramCard
