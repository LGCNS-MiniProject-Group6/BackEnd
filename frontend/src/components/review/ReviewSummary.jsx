import { REVIEW_STATUS } from '../../constants/reviewStatus'
import ReviewStatusBadge from './ReviewStatusBadge'

function ReviewSummary({ review }) {
  const count = (status) => review.conditions.filter((item) => item.status === status).length

  return (
    <section className="review-summary">
      <div className="review-summary__eyebrow">
        <ReviewStatusBadge status={review.status} />
        <span>{review.conditions.length}개 조건 검수 완료</span>
      </div>
      <h2>{review.summary}</h2>
      <p>{review.description}</p>
      <div className="review-summary__counts">
        <div><span>충족</span><strong>{count(REVIEW_STATUS.MATCHED)}개</strong></div>
        <div><span>추가 확인 필요</span><strong>{count(REVIEW_STATUS.NEED_CHECK)}개</strong></div>
        <div><span>미충족 가능</span><strong>{count(REVIEW_STATUS.UNMATCHED)}개</strong></div>
      </div>
    </section>
  )
}

export default ReviewSummary
