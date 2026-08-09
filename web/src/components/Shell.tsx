import { Link, NavLink } from 'react-router-dom'
import type { ReactNode } from 'react'
import { APP_NAME, BLURB, TAGLINE } from '../lib/copy'

export function Shell({
  title,
  showHero = false,
  children,
}: {
  title?: string
  showHero?: boolean
  children: ReactNode
}) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" style={{ textDecoration: 'none', color: 'inherit' }}>
          <h1 className="brand">{APP_NAME}</h1>
          <p className="tagline">{TAGLINE}</p>
        </Link>
        <nav className="primary-nav" aria-label="Main">
          <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/" end>
            Inbox
          </NavLink>
          <span className="nav-sep" aria-hidden>
            |
          </span>
          <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/senders">
            Senders
          </NavLink>
          <span className="nav-sep" aria-hidden>
            |
          </span>
          <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/done">
            Done
          </NavLink>
        </nav>
      </header>
      {showHero && <p className="hero-blurb">{BLURB}</p>}
      {title && title !== APP_NAME ? <h2 className="page-title">{title}</h2> : null}
      <div className="nav-actions">
        <NavLink className="btn" to="/paste">
          Paste link
        </NavLink>
        <NavLink className="btn" to="/categories">
          Categories
        </NavLink>
        <NavLink className="btn" to="/privacy">
          Privacy
        </NavLink>
      </div>
      {children}
    </div>
  )
}
