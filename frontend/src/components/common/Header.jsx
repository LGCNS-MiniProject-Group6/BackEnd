import { useEffect, useState } from 'react'
import { NavLink, useNavigate } from 'react-router-dom'
import supportUpLogo from '../../assets/support-up-logo.png'
import { ROUTES } from '../../constants/routes'
import { useAuth } from '../../hooks/useAuth'
import Button from './Button'

function Header() {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [isScrolled, setIsScrolled] = useState(false)
  const [isMenuOpen, setIsMenuOpen] = useState(false)

  useEffect(() => {
    const handleScroll = () => setIsScrolled(window.scrollY > 18)
    handleScroll()
    window.addEventListener('scroll', handleScroll, { passive: true })
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const handleLogout = async () => {
    await logout()
    navigate(ROUTES.LOGIN, { replace: true })
  }

  const closeMenu = () => setIsMenuOpen(false)

  return (
    <header className={`site-header ${isScrolled ? 'site-header--scrolled' : ''}`.trim()}>
      <div className="site-header__inner">
        <NavLink className="brand" to={ROUTES.HOME} aria-label="지원UP 홈" onClick={closeMenu}>
          <img className="brand__logo" src={supportUpLogo} alt="" />
          지원UP
        </NavLink>
        <button
          className={`site-header__menu ${isMenuOpen ? 'is-open' : ''}`.trim()}
          type="button"
          aria-label={isMenuOpen ? '메뉴 닫기' : '메뉴 열기'}
          aria-expanded={isMenuOpen}
          aria-controls="site-navigation"
          onClick={() => setIsMenuOpen((open) => !open)}
        >
          <span /><span />
        </button>
        <nav className={`site-nav ${isMenuOpen ? 'is-open' : ''}`.trim()} id="site-navigation" aria-label="주요 메뉴">
          <NavLink to={ROUTES.HOME} onClick={closeMenu}>홈</NavLink>
          <NavLink to={ROUTES.PROGRAMS} onClick={closeMenu}>사업 찾기</NavLink>
          <NavLink to={ROUTES.MY_PAGE} onClick={closeMenu}>마이페이지</NavLink>
        </nav>
        <div className="site-header__user">
          <span>{user?.name ?? '사용자'}님</span>
          <Button variant="ghost" size="small" onClick={handleLogout}>로그아웃</Button>
        </div>
      </div>
    </header>
  )
}

export default Header
