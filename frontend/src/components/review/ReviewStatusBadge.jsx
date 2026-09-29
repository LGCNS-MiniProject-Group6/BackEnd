import { REVIEW_STATUS_META } from '../../constants/reviewStatus'
import StatusBadge from '../common/StatusBadge'

function ReviewStatusBadge({ status }) {
  const meta = REVIEW_STATUS_META[status] ?? REVIEW_STATUS_META.needCheck
  return (
    <StatusBadge tone={meta.tone}>
      <span aria-hidden="true">{meta.icon}</span> {meta.label}
    </StatusBadge>
  )
}

export default ReviewStatusBadge
