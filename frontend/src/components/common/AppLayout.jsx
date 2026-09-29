import Header from './Header'
import Footer from './Footer'

function AppLayout({ children, className = '' }) {
  return (
    <div className="app-shell">
      <Header />
      <main className={`page-container ${className}`.trim()}>{children}</main>
      <Footer />
    </div>
  )
}

export default AppLayout
