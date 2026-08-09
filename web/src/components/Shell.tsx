import { Link, NavLink } from 'react-router-dom'
import type { ReactNode } from 'react'

export function Shell({
  title = 'ReelShelf',
  children,
}: {
  title?: string
  children: ReactNode
}) {
  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" style={{ textDecoration: 'none' }}>
          <h1 className="brand">{title}</h1>
        </Link>
        <nav className="nav-actions">
          <NavLink className="btn" to="/paste">
            Paste link
          </NavLink>
          <NavLink className="btn" to="/senders">
            Senders
          </NavLink>
          <NavLink className="btn" to="/categories">
            Categories
          </NavLink>
          <NavLink className="btn" to="/privacy">
            Privacy
          </NavLink>
        </nav>
      </header>
      {children}
    </div>
  )
}
