import { Link } from 'react-router-dom'
import AppLayout from '../components/common/AppLayout'
import { ROUTES } from '../constants/routes'

function NotFoundPage() {
  return (
    <AppLayout className="center-page">
      <section className="not-found"><span>404</span><h1>요청한 페이지를 찾을 수 없어요.</h1><p>주소가 올바른지 확인하거나 홈에서 다시 시작해주세요.</p><Link className="button button--primary button--large" to={ROUTES.HOME}>홈으로 돌아가기</Link></section>
    </AppLayout>
  )
}

export default NotFoundPage
