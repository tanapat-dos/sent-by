import { useEffect, useId, useRef, useState, type ReactNode } from 'react'
import { createPortal } from 'react-dom'
import { useLocale } from '../lib/locale'

const SEEN_KEY = 'sentby.help.seen.v1'

export function HelpGuide({ autoOpenOnce = false }: { autoOpenOnce?: boolean }) {
  const { t } = useLocale()
  const [open, setOpen] = useState(false)
  const titleId = useId()
  const closeRef = useRef<HTMLButtonElement>(null)

  useEffect(() => {
    if (!autoOpenOnce) return
    try {
      if (localStorage.getItem(SEEN_KEY) === '1') return
      setOpen(true)
      localStorage.setItem(SEEN_KEY, '1')
    } catch {
      /* ignore private mode */
    }
  }, [autoOpenOnce])

  useEffect(() => {
    if (!open) return
    const prev = document.body.style.overflow
    document.body.style.overflow = 'hidden'
    closeRef.current?.focus()
    const onKey = (e: KeyboardEvent) => {
      if (e.key === 'Escape') setOpen(false)
    }
    window.addEventListener('keydown', onKey)
    return () => {
      document.body.style.overflow = prev
      window.removeEventListener('keydown', onKey)
    }
  }, [open])

  const dialog =
    open &&
    createPortal(
      <div
        className="help-backdrop"
        role="presentation"
        onClick={(e) => {
          if (e.target === e.currentTarget) setOpen(false)
        }}
      >
        <div
          className="help-dialog"
          role="dialog"
          aria-modal="true"
          aria-labelledby={titleId}
        >
          <div className="help-dialog-header">
            <h2 id={titleId}>{t.helpTitle}</h2>
            <button
              ref={closeRef}
              type="button"
              className="btn help-close"
              onClick={() => setOpen(false)}
            >
              {t.close}
            </button>
          </div>
          <div className="help-dialog-body">
            <p className="help-overview">{t.helpOverview}</p>
            <ol className="help-steps">
              {t.helpSteps.map((step) => (
                <li key={step.title}>
                  <strong>{step.title}</strong>
                  <p>{step.body}</p>
                </li>
              ))}
            </ol>
            <p className="muted help-foot">{t.helpFoot}</p>
          </div>
        </div>
      </div>,
      document.body,
    )

  return (
    <>
      <TipHint tip={t.navTips.help}>
        <button type="button" className="btn" onClick={() => setOpen(true)} aria-haspopup="dialog">
          {t.howToUse}
        </button>
      </TipHint>
      {dialog}
    </>
  )
}

export function TipHint({ tip, children }: { tip: string; children: ReactNode }) {
  return (
    <span className="tip-wrap">
      {children}
      <span className="tip-bubble" role="tooltip">
        {tip}
      </span>
    </span>
  )
}
