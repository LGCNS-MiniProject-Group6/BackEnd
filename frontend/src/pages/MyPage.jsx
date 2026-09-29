import { useEffect, useState } from 'react'
import { Link } from 'react-router-dom'
import { favoriteApi } from '../api/favoriteApi'
import { programApi } from '../api/programApi'
import { reviewApi } from '../api/reviewApi'
import AppLayout from '../components/common/AppLayout'
import Card from '../components/common/Card'
import EmptyState from '../components/common/EmptyState'
import ErrorMessage from '../components/common/ErrorMessage'
import Loading from '../components/common/Loading'
import ReviewStatusBadge from '../components/review/ReviewStatusBadge'
import { programDetailPath, reviewPath, ROUTES } from '../constants/routes'
import { useAuth } from '../hooks/useAuth'
import { formatDate, getDday } from '../utils/dateUtils'
import { formatCurrency } from '../utils/formatUtils'
import { normalizeReview, toReviewStatusKey } from '../utils/reviewUtils'

const REVIEW_HISTORY_SIZE = 5

function MyPage() {
  const { user, business, refreshSession } = useAuth()
  const [favorites, setFavorites] = useState([])
  const [favoriteState, setFavoriteState] = useState('loading')
  const [removingId, setRemovingId] = useState('')
  const [reviewHistory, setReviewHistory] = useState([])
  const [reviewHistoryState, setReviewHistoryState] = useState('loading')

  const fetchReviewHistory = async () => {
    const { data } = await reviewApi.getReviews({ page: 0, size: REVIEW_HISTORY_SIZE })
    const items = data?.content ?? []
    return Promise.all(items.map(async (item) => {
      const [programResult, detailResult] = await Promise.allSettled([
        programApi.getProgramDetail(item.pblancId),
        reviewApi.getReviewDetail(item.reviewId),
      ])
      const review = detailResult.status === 'fulfilled' ? normalizeReview(detailResult.value.data) : null
      return {
        reviewId: item.reviewId,
        pblancId: item.pblancId,
        title: programResult.status === 'fulfilled' ? programResult.value.data?.title : null,
        status: review?.status ?? toReviewStatusKey(item.status),
        createdAt: item.createdAt,
        matchedCount: review?.matchedCount ?? null,
        needCheckCount: review?.needCheckCount ?? null,
        unmatchedCount: review?.unmatchedCount ?? null,
      }
    }))
  }

  const loadReviewHistory = () => {
    setReviewHistoryState('loading')
    fetchReviewHistory()
      .then((enriched) => {
        setReviewHistory(enriched)
        setReviewHistoryState('success')
      })
      .catch(() => setReviewHistoryState('error'))
  }

  const loadFavorites = async () => {
    setFavoriteState('loading')
    try {
      const { data } = await favoriteApi.getFavorites()
      setFavorites(Array.isArray(data) ? data : [])
      setFavoriteState('success')
    } catch {
      setFavoriteState('error')
    }
  }

  useEffect(() => {
    let active = true
    refreshSession().catch(() => undefined)
    favoriteApi.getFavorites()
      .then(({ data }) => {
        if (!active) return
        setFavorites(Array.isArray(data) ? data : [])
        setFavoriteState('success')
      })
      .catch(() => {
        if (active) setFavoriteState('error')
      })
    return () => {
      active = false
    }
  }, [refreshSession])

  useEffect(() => {
    let active = true
    fetchReviewHistory()
      .then((enriched) => {
        if (!active) return
        setReviewHistory(enriched)
        setReviewHistoryState('success')
      })
      .catch(() => {
        if (active) setReviewHistoryState('error')
      })
    return () => {
      active = false
    }
  }, [])

  const removeFavorite = async (pblancId) => {
    if (removingId) return
    setRemovingId(pblancId)
    try {
      await favoriteApi.removeFavorite(pblancId)
      setFavorites((current) => current.filter((favorite) => favorite.pblancId !== pblancId))
    } catch (error) {
      if (error.response?.status === 404) {
        setFavorites((current) => current.filter((favorite) => favorite.pblancId !== pblancId))
      }
    } finally {
      setRemovingId('')
    }
  }

  return (
    <AppLayout>
      <header className="page-heading">
        <span className="eyebrow">내 활동 한눈에 보기</span>
        <h1>마이페이지</h1>
        <p>프로필과 사업정보, 관심공고, AI 검수기록을 한 곳에서 관리하세요.</p>
      </header>

      <div className="mypage-layout">
        <aside className="mypage-sidebar">
          <div className="mypage-sidebar__profile">
            <div>{user?.name?.slice(0, 1) || '사'}</div>
            <strong>{user?.name || '사용자'}</strong>
            <span>{business?.industry || '사업정보 미등록'}</span>
          </div>
          <nav aria-label="마이페이지 메뉴">
            <a href="#profile">내 프로필</a>
            <a href="#business">사업정보</a>
            <a href="#reviews">AI 검수기록</a>
            <a href="#favorites">관심공고</a>
          </nav>
        </aside>

        <div className="mypage-content">
          <Card className="mypage-card" as="section">
            <div className="mypage-card__heading">
              <div><span className="eyebrow">계정 정보</span><h2 id="profile">내 프로필</h2></div>
              <Link className="button button--secondary button--small" to={ROUTES.PROFILE_EDIT}>프로필 수정</Link>
            </div>
            <dl className="info-grid">
              <div><dt>이름</dt><dd>{user?.name || '정보 없음'}</dd></div>
              <div><dt>이메일</dt><dd>{user?.email || '정보 없음'}</dd></div>
              <div><dt>휴대폰</dt><dd>{user?.phone || '정보 없음'}</dd></div>
            </dl>
          </Card>

          <Card className="mypage-card" as="section">
            <div className="mypage-card__heading">
              <div><span className="eyebrow">맞춤 추천 기준</span><h2 id="business">사업정보</h2></div>
              <Link className="button button--secondary button--small" to={ROUTES.PROFILE_EDIT}>{business ? '사업정보 수정' : '사업정보 입력'}</Link>
            </div>
            {business ? (
              <dl className="info-grid info-grid--three">
                <div><dt>지역</dt><dd>{business.region}</dd></div>
                <div><dt>업종</dt><dd>{business.industry}</dd></div>
                <div><dt>개업일</dt><dd>{business.openingDate}</dd></div>
                <div><dt>상시근로자</dt><dd>{business.employeeCount ?? 0}명</dd></div>
                <div><dt>연 매출</dt><dd>{`${formatCurrency(business.annualRevenue)}원`}</dd></div>
              </dl>
            ) : (
              <EmptyState title="사업정보가 아직 없습니다." description="사업정보를 입력하면 맞춤 공고 분석에 활용할 수 있어요." />
            )}
          </Card>

          <section className="mypage-section" id="reviews">
            <div className="section-heading section-heading--compact">
              <h2>최근 AI 검수기록</h2>
              <p>내가 실행한 AI 검수 결과를 다시 확인할 수 있어요.</p>
            </div>
            {reviewHistoryState === 'loading' ? (
              <Loading label="검수기록을 불러오고 있어요." />
            ) : reviewHistoryState === 'error' ? (
              <ErrorMessage title="검수기록을 불러오지 못했습니다." onRetry={loadReviewHistory} />
            ) : reviewHistory.length === 0 ? (
              <EmptyState title="아직 AI 검수기록이 없습니다." description="관심있는 공고 상세 화면에서 AI 검수를 실행해보세요." />
            ) : reviewHistory.map((item) => (
              <Card className="activity-card" key={item.reviewId}>
                <div>
                  <ReviewStatusBadge status={item.status} />
                  <h3>{item.title || item.pblancId}</h3>
                  <p>
                    {item.matchedCount === null
                      ? '검수 상세 정보를 불러오지 못했습니다.'
                      : `충족 ${item.matchedCount} / 추가 확인 ${item.needCheckCount} / 미충족 가능 ${item.unmatchedCount}`}
                    {' · '}{formatDate(item.createdAt)}
                  </p>
                </div>
                <Link
                  className="button button--primary button--small"
                  to={reviewPath(item.pblancId)}
                  state={{ reviewId: item.reviewId }}
                >
                  검수 결과 다시보기
                </Link>
              </Card>
            ))}
          </section>

          <section className="mypage-section" id="favorites">
            <div className="section-heading section-heading--compact">
              <h2>관심공고</h2>
              <p>Backend에 저장한 관심공고를 확인하세요.</p>
            </div>
            {favoriteState === 'loading' ? (
              <Loading label="관심공고를 불러오고 있어요." />
            ) : favoriteState === 'error' ? (
              <ErrorMessage title="관심공고를 불러오지 못했습니다." onRetry={loadFavorites} />
            ) : favorites.length === 0 ? (
              <EmptyState title="저장한 관심공고가 없습니다." description="지원사업 목록에서 관심 있는 공고를 저장해보세요." />
            ) : favorites.map((favorite) => (
              <Card className="activity-card" key={favorite.favoriteId}>
                <div>
                  <span className="eyebrow">{favorite.applyEndDate ? getDday(favorite.applyEndDate) : '마감일 미정'}</span>
                  <h3>{favorite.title}</h3>
                  <p>{favorite.organization} · 마감 {formatDate(favorite.applyEndDate)}</p>
                </div>
                <div className="inline-actions">
                  <Link className="button button--secondary button--small" to={programDetailPath(favorite.pblancId)}>상세보기</Link>
                  <button type="button" className="text-button" disabled={removingId === favorite.pblancId} onClick={() => removeFavorite(favorite.pblancId)}>
                    {removingId === favorite.pblancId ? '해제 중...' : '관심공고 해제'}
                  </button>
                </div>
              </Card>
            ))}
          </section>
        </div>
      </div>
    </AppLayout>
  )
}

export default MyPage
