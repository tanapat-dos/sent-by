import { Link, NavLink } from 'react-router-dom'
import type { ReactNode } from 'react'
import { HelpGuide, TipHint } from './HelpGuide'
import { useLocale } from '../lib/locale'

export function Shell({
  title,
  showHero = false,
  children,
}: {
  title?: string
  showHero?: boolean
  children: ReactNode
}) {
  const { t, lang, setLang } = useLocale()

  return (
    <div className="app-shell">
      {showHero && (
        <div className="atmosphere" aria-hidden>
          <span className="reel" />
          <span className="reel" />
          <span className="reel" />
        </div>
      )}
      <header className="topbar">
        <Link to="/" className="brand-block">
          <h1 className="brand">{t.appName}</h1>
          <p className="tagline">{t.tagline}</p>
        </Link>
        <nav className="primary-nav" aria-label="Main">
          <TipHint tip={t.navTips.inbox}>
            <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/" end>
              {t.inbox}
            </NavLink>
          </TipHint>
          <TipHint tip={t.navTips.senders}>
            <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/senders">
              {t.senders}
            </NavLink>
          </TipHint>
          <TipHint tip={t.navTips.done}>
            <NavLink className={({ isActive }) => `nav-link${isActive ? ' active' : ''}`} to="/done">
              {t.done}
            </NavLink>
          </TipHint>
        </nav>
      </header>
      {showHero && <p className="hero-blurb">{t.blurb}</p>}
      {title && title !== t.appName ? <h2 className="page-title">{title}</h2> : null}
      <div className="nav-actions">
        <TipHint tip={t.navTips.paste}>
          <NavLink className="btn primary" to="/paste">
            {t.pasteLink}
          </NavLink>
        </TipHint>
        <HelpGuide autoOpenOnce={showHero} />
        <TipHint tip={t.navTips.categories}>
          <NavLink className="btn" to="/categories">
            {t.categories}
          </NavLink>
        </TipHint>
        <TipHint tip={t.navTips.privacy}>
          <NavLink className="btn" to="/privacy">
            {t.privacy}
          </NavLink>
        </TipHint>
        <div className="lang-switch" role="group" aria-label={t.language}>
          <button
            type="button"
            className={`chip ${lang === 'en' ? 'active' : ''}`}
            onClick={() => setLang('en')}
          >
            {t.langEn}
          </button>
          <button
            type="button"
            className={`chip ${lang === 'th' ? 'active' : ''}`}
            onClick={() => setLang('th')}
          >
            {t.langTh}
          </button>
        </div>
      </div>
      {children}
    </div>
  )
}
