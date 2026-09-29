import supportUpLogo from '../../assets/support-up-logo.png'
import '../../styles/Footer.css'

const footerLinks = [
  { label: '공지사항', href: '#' },
  { label: '개인정보처리방침', href: '#', strong: true },
  { label: '이용약관', href: '#' },
  { label: '이메일무단수집거부', href: '#' },
  { label: '자주하는 질문', href: '#' },
]

function Footer() {
  return (
    <footer className="site-footer">
      <div className="site-footer__inner">
        <div className="site-footer__top">
          <div className="site-footer__brand">
            <div className="site-footer__logo">
              <img src={supportUpLogo} alt="" />
              <strong>지원UP</strong>
            </div>
            <p>정부지원사업 AI 신청 도우미</p>
          </div>

          <nav className="site-footer__links" aria-label="하단 메뉴">
            {footerLinks.map(({ label, href, strong }) => (
              <a key={label} href={href} className={strong ? 'is-strong' : undefined}>
                {label}
              </a>
            ))}
          </nav>
        </div>

        <div className="site-footer__bottom">
          <p className="site-footer__info">
            <span>LG CNS AM INSPIRE CAMP 6조</span>
            <span>
              문의 <a href="mailto:lgcns6th@email.com">lgcns6th@email.com</a>
            </span>
          </p>
          <p className="site-footer__copy">© 2026 지원UP. All rights reserved.</p>
        </div>
      </div>
    </footer>
  )
}

export default Footer
