import { Link } from 'react-router-dom'
import supportUpLogo from '../../assets/support-up-logo.png'
import { ROUTES } from '../../constants/routes'

function HomeFooter() {
  return (
    <footer className="home-footer">
      <div>
        <Link className="brand" to={ROUTES.HOME} aria-label="지원UP 홈">
          <img className="brand__logo" src={supportUpLogo} alt="" />
          지원UP
        </Link>
        <p>1인 소상공인을 위한 정부지원사업 AI 신청 도우미</p>
      </div>
      <nav aria-label="하단 메뉴">
        <Link to={ROUTES.PROGRAMS}>지원사업 찾기</Link>
        <Link to={ROUTES.MY_PAGE}>내 사업정보</Link>
      </nav>
      <small>AI 분석 결과는 신청 판단을 돕는 참고 정보이며, 최종 조건은 원문 공고를 확인해주세요.</small>
    </footer>
  )
}

export default HomeFooter
