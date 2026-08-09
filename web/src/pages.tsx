import { Link, useNavigate } from 'react-router-dom'
import {
  createCategory,
  createSender,
  deleteCategory,
  getState,
  ingest,
  mergeSenders,
  orderedSenders,
  renameCategory,
  renameSender,
  setClipCategory,
  setFavorite,
  setReplyStatus,
  setWatchStatus,
} from './lib/db'
import { isCompleted, outstandingReplyCount, type InboxFilter, type SourceApp } from './lib/types'
import { savedMessage, sentBy, statusLine } from './lib/copy'
import { useLocale } from './lib/locale'
import { displayTitle, extractUrls } from './lib/urls'
import { useDb } from './lib/useDb'
import { Shell } from './components/Shell'
import { useEffect, useMemo, useRef, useState, type DragEvent, type FormEvent } from 'react'

const REPLY_PRESETS = ['😂', '❤️', 'That was good']
const CLIP_MIME = 'application/x-sentby-clip'

export function InboxPage({ doneOnly = false }: { doneOnly?: boolean }) {
  const { t, lang } = useLocale()
  const navigate = useNavigate()
  const db = useDb()
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState<InboxFilter>(doneOnly ? 'COMPLETED' : 'ALL')
  const [categoryId, setCategoryId] = useState<string | null>(null)
  const [draggingClipId, setDraggingClipId] = useState<string | null>(null)
  const [dropHoverId, setDropHoverId] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const suppressClick = useRef(false)

  const effectiveFilter = doneOnly ? 'COMPLETED' : filter

  useEffect(() => {
    if (!toast) return
    const id = window.setTimeout(() => setToast(null), 2200)
    return () => window.clearTimeout(id)
  }, [toast])

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase()
    return [...db.clips]
      .map((clip) => {
        const shares = db.shares.filter((s) => s.clipId === clip.id)
        const senderList = shares
          .map((s) => db.senders.find((x) => x.id === s.senderId)?.displayName)
          .filter((n): n is string => Boolean(n))
        const assignedIds = db.clipCategories
          .filter((cc) => cc.clipId === clip.id)
          .map((cc) => cc.categoryId)
        const cats = assignedIds
          .map((id) => db.categories.find((c) => c.id === id)?.name)
          .filter(Boolean)
        return {
          clip,
          shares,
          senderList: [...new Set(senderList)],
          assignedCategoryIds: new Set(assignedIds),
          categoryNames: cats.join(', '),
          outstanding: outstandingReplyCount(shares),
          completed: isCompleted(clip, shares),
        }
      })
      .filter((row) => {
        if (effectiveFilter === 'UNWATCHED' && row.clip.watchStatus !== 'UNWATCHED') return false
        if (effectiveFilter === 'WATCHED' && row.clip.watchStatus !== 'WATCHED') return false
        if (effectiveFilter === 'NEEDS_REPLY' && row.outstanding === 0) return false
        if (effectiveFilter === 'COMPLETED' && !row.completed) return false
        if (categoryId && !row.assignedCategoryIds.has(categoryId)) return false
        if (!q) return true
        const hay = [
          row.clip.title,
          row.clip.creatorName,
          row.clip.originalUrl,
          row.clip.canonicalUrl,
          row.clip.platform,
          row.senderList.join(' '),
          row.categoryNames,
        ]
          .join(' ')
          .toLowerCase()
        return hay.includes(q)
      })
      .sort((a, b) => {
        const aw = a.clip.watchStatus === 'UNWATCHED' ? 0 : 1
        const bw = b.clip.watchStatus === 'UNWATCHED' ? 0 : 1
        if (aw !== bw) return aw - bw
        return b.clip.lastReceivedAt - a.clip.lastReceivedAt
      })
  }, [db, query, effectiveFilter, categoryId])

  function onClipDragStart(e: DragEvent, clipId: string) {
    e.dataTransfer.setData(CLIP_MIME, clipId)
    e.dataTransfer.setData('text/plain', clipId)
    e.dataTransfer.effectAllowed = 'copy'
    setDraggingClipId(clipId)
    suppressClick.current = true
  }

  function onClipDragEnd() {
    setDraggingClipId(null)
    setDropHoverId(null)
    window.setTimeout(() => {
      suppressClick.current = false
    }, 50)
  }

  function onCategoryDragOver(e: DragEvent, id: string) {
    e.preventDefault()
    e.dataTransfer.dropEffect = 'copy'
    setDropHoverId(id)
  }

  function onCategoryDrop(e: DragEvent, catId: string, catName: string) {
    e.preventDefault()
    const clipId = e.dataTransfer.getData(CLIP_MIME) || e.dataTransfer.getData('text/plain')
    setDropHoverId(null)
    setDraggingClipId(null)
    if (!clipId) return
    const already = db.clipCategories.some((cc) => cc.clipId === clipId && cc.categoryId === catId)
    if (already) {
      setToast(t.alreadyInCategory(catName))
      return
    }
    setClipCategory(clipId, catId, true)
    setToast(t.assignedToCategory(catName))
  }

  return (
    <Shell showHero={!doneOnly}>
      <p className="note-line">{doneOnly ? t.doneNote : t.webDemoNote}</p>
      <div className="field">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={t.searchPlaceholder}
        />
      </div>
      {!doneOnly && (
        <div className="chip-row">
          {(['UNWATCHED', 'WATCHED', 'NEEDS_REPLY'] as InboxFilter[]).map((f) => (
            <button
              key={f}
              className={`chip ${filter === f ? 'active' : ''}`}
              onClick={() => setFilter(filter === f ? 'ALL' : f)}
            >
              {f === 'UNWATCHED' ? t.unwatched : f === 'WATCHED' ? t.watched : t.needsReply}
            </button>
          ))}
        </div>
      )}

      <div
        className={`category-dropzone ${draggingClipId ? 'is-dragging' : ''}`}
        aria-label={t.categories}
      >
        <div className="category-dropzone-label">
          {db.categories.length === 0 ? t.dragNeedCategories : t.dragHint}
        </div>
        <div className="chip-row category-drop-chips">
          <button
            type="button"
            className={`chip ${!categoryId ? 'active' : ''}`}
            onClick={() => setCategoryId(null)}
          >
            {t.allCategories}
          </button>
          {db.categories.map((c) => (
            <button
              key={c.id}
              type="button"
              className={`chip droppable ${categoryId === c.id ? 'active' : ''} ${
                dropHoverId === c.id ? 'drop-hover' : ''
              } ${draggingClipId ? 'drop-ready' : ''}`}
              onClick={() => setCategoryId(categoryId === c.id ? null : c.id)}
              onDragOver={(e) => onCategoryDragOver(e, c.id)}
              onDragEnter={(e) => onCategoryDragOver(e, c.id)}
              onDragLeave={() => setDropHoverId((cur) => (cur === c.id ? null : cur))}
              onDrop={(e) => onCategoryDrop(e, c.id, c.name)}
            >
              {dropHoverId === c.id && draggingClipId ? t.dropOnCategory : c.name}
            </button>
          ))}
          {db.categories.length === 0 && (
            <Link className="chip" to="/categories">
              {t.newCategory}
            </Link>
          )}
        </div>
      </div>

      {toast && <div className="toast">{toast}</div>}

      {rows.length === 0 ? (
        <div className="panel empty-state">
          <p className="empty-mark">{doneOnly ? '✓' : '✦'}</p>
          <p>{doneOnly ? t.allCaughtUp : t.emptyInbox}</p>
          {!doneOnly && (
            <Link className="btn primary" to="/paste">
              {t.pasteLink}
            </Link>
          )}
        </div>
      ) : (
        <div className="inbox-list">
          {rows.map((row, index) => {
            const line = statusLine(
              row.completed,
              row.clip.watchStatus === 'WATCHED',
              row.outstanding,
              t,
            )
            const pillClass = row.completed
              ? 'ok'
              : row.outstanding > 0
                ? 'warn'
                : ''
            return (
              <div
                key={row.clip.id}
                className={`card ${draggingClipId === row.clip.id ? 'is-dragging' : ''}`}
                style={{ ['--i' as string]: index }}
                draggable
                role="link"
                tabIndex={0}
                onDragStart={(e) => onClipDragStart(e, row.clip.id)}
                onDragEnd={onClipDragEnd}
                onClick={() => {
                  if (suppressClick.current) return
                  navigate(`/clip/${row.clip.id}`)
                }}
                onKeyDown={(e) => {
                  if (e.key === 'Enter' || e.key === ' ') {
                    e.preventDefault()
                    navigate(`/clip/${row.clip.id}`)
                  }
                }}
              >
                <div className="thumb">
                  {row.clip.thumbnailUrl ? (
                    <img src={row.clip.thumbnailUrl} alt="" draggable={false} />
                  ) : (
                    row.clip.platform.slice(0, 2)
                  )}
                </div>
                <div>
                  <p className="title">
                    {displayTitle(row.clip.title, row.clip.originalUrl, row.clip.canonicalUrl)}
                  </p>
                  <div className="meta">
                    {row.clip.platform.toLowerCase()}
                    {row.clip.creatorName ? ` · ${row.clip.creatorName}` : ''}
                  </div>
                  <div className="meta">
                    {new Date(row.clip.lastReceivedAt).toLocaleString(lang === 'th' ? 'th-TH' : 'en')} ·{' '}
                    {sentBy(row.senderList, t)}
                    {row.categoryNames ? ` · ${row.categoryNames}` : ''}
                  </div>
                  <div className={`status-pill ${pillClass}`}>{line}</div>
                </div>
                {row.completed ? <div className="check" title={t.done}>✓</div> : <div />}
              </div>
            )
          })}
        </div>
      )}
    </Shell>
  )
}

export function PastePage() {
  const { t } = useLocale()
  const db = useDb()
  const senders = orderedSenders(db)
  const [text, setText] = useState('')
  const [senderId, setSenderId] = useState(senders[0]?.id ?? '')
  const [newSender, setNewSender] = useState('')
  const [sourceApp, setSourceApp] = useState<SourceApp>('OTHER')
  const [message, setMessage] = useState<string | null>(null)
  const urlCount = extractUrls(text).length

  function onCreateSender() {
    const s = createSender(newSender)
    setSenderId(s.id)
    setNewSender('')
  }

  function onSave(e: FormEvent) {
    e.preventDefault()
    try {
      let sid = senderId
      let senderName = senders.find((s) => s.id === sid)?.displayName ?? ''
      if (!sid) {
        if (!newSender.trim()) {
          setMessage(t.selectSender)
          return
        }
        senderName = newSender.trim()
        sid = createSender(newSender).id
        setNewSender('')
      }
      const results = ingest(text, sid, sourceApp)
      const existing = results.filter((r) => r.wasExisting).length
      const created = results.length - existing
      setMessage(savedMessage(created, existing, senderName || 'sender', t))
      setText('')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : t.saveFailed)
    }
  }

  const favorites = senders.filter((s) => s.isFavorite)
  const others = senders.filter((s) => !s.isFavorite)

  return (
    <Shell title={t.pasteTitle}>
      <form className="panel stack" onSubmit={onSave}>
        <div className="field">
          <label>{t.pasteLabel}</label>
          <textarea rows={4} value={text} onChange={(e) => setText(e.target.value)} />
        </div>
        <div className="muted">{t.urlsDetected(urlCount)}</div>
        <div>
          <div className="muted">{t.sender}</div>
          {favorites.length > 0 && (
            <>
              <div className="muted">{t.favorites}</div>
              <div className="chip-row">
                {favorites.map((s) => (
                  <button
                    type="button"
                    key={s.id}
                    className={`chip ${senderId === s.id ? 'active' : ''}`}
                    onClick={() => setSenderId(s.id)}
                  >
                    ★ {s.displayName}
                  </button>
                ))}
              </div>
            </>
          )}
          <div className="chip-row">
            {others.map((s) => (
              <button
                type="button"
                key={s.id}
                className={`chip ${senderId === s.id ? 'active' : ''}`}
                onClick={() => setSenderId(s.id)}
              >
                {s.displayName}
              </button>
            ))}
          </div>
          <div className="field">
            <label>{t.newSender}</label>
            <div className="row">
              <input value={newSender} onChange={(e) => setNewSender(e.target.value)} />
              <button type="button" onClick={onCreateSender} disabled={!newSender.trim()}>
                {t.create}
              </button>
            </div>
          </div>
        </div>
        <div>
          <div className="muted">{t.sourceApp}</div>
          <div className="chip-row">
            {(['LINE', 'MESSENGER', 'OTHER'] as SourceApp[]).map((app) => (
              <button
                type="button"
                key={app}
                className={`chip ${sourceApp === app ? 'active' : ''}`}
                onClick={() => setSourceApp(app)}
              >
                {app}
              </button>
            ))}
          </div>
        </div>
        {message && <p>{message}</p>}
        <button className="primary" disabled={urlCount === 0}>
          {t.save}
        </button>
        <Link to="/">{t.backToInbox}</Link>
      </form>
    </Shell>
  )
}

export function ClipDetailPage({ clipId }: { clipId: string }) {
  const { t } = useLocale()
  const db = useDb()
  const clip = db.clips.find((c) => c.id === clipId)
  const shares = db.shares.filter((s) => s.clipId === clipId)
  const assigned = new Set(db.clipCategories.filter((cc) => cc.clipId === clipId).map((cc) => cc.categoryId))
  const completed = clip ? isCompleted(clip, shares) : false
  const [custom, setCustom] = useState<Record<string, string>>({})

  if (!clip) {
    return (
      <Shell>
        <p>
          {t.clipNotFound} <Link to="/">{t.inbox}</Link>
        </p>
      </Shell>
    )
  }

  function openClip() {
    window.open(clip!.originalUrl, '_blank', 'noopener,noreferrer')
    setWatchStatus(clip!.id, 'WATCHED')
  }

  async function copyReply(shareId: string, text: string) {
    await navigator.clipboard.writeText(text)
    setReplyStatus(shareId, 'REPLIED', text)
    alert(t.copiedAlert)
  }

  return (
    <Shell title={displayTitle(clip.title, clip.originalUrl, clip.canonicalUrl)}>
      <div className="panel stack">
        <div className="card" style={{ border: 0, padding: 0 }}>
          <div className="thumb">
            {clip.thumbnailUrl ? <img src={clip.thumbnailUrl} alt="" /> : clip.platform.slice(0, 2)}
          </div>
          <div>
            <p className="title">{displayTitle(clip.title, clip.originalUrl, clip.canonicalUrl)}</p>
            <div className="meta">
              {clip.platform.toLowerCase()}
              {clip.creatorName ? ` · ${clip.creatorName}` : ''}
            </div>
            <div className="meta">{clip.originalUrl}</div>
            {completed ? (
              <div className="row">
                <span className="check">✓</span>
                <span className="status-pill ok">{t.completedDetail}</span>
              </div>
            ) : (
              <div className="status-pill">
                {clip.watchStatus === 'WATCHED' ? t.watched : t.unwatched}
              </div>
            )}
          </div>
        </div>
        <div className="row">
          <button className="primary" onClick={openClip}>
            {t.openClip}
          </button>
          <button
            onClick={() =>
              setWatchStatus(clip.id, clip.watchStatus === 'WATCHED' ? 'UNWATCHED' : 'WATCHED')
            }
          >
            {clip.watchStatus === 'WATCHED' ? t.watchedUndo : t.markWatched}
          </button>
        </div>
        <p className="muted">{t.openMarksWatched}</p>

        <h3>{t.categories}</h3>
        {db.categories.length === 0 ? (
          <p className="muted">{t.noCategories}</p>
        ) : (
          <div className="chip-row">
            {db.categories.map((c) => (
              <button
                key={c.id}
                className={`chip ${assigned.has(c.id) ? 'active' : ''}`}
                onClick={() => setClipCategory(clip.id, c.id, !assigned.has(c.id))}
              >
                {c.name}
              </button>
            ))}
          </div>
        )}

        <h3>{t.senders}</h3>
        {shares.map((share) => {
          const sender = db.senders.find((s) => s.id === share.senderId)
          return (
            <div key={share.id} className="panel stack">
              <strong>
                {sender?.displayName ?? t.unknown} · {share.sourceApp}
              </strong>
              <div className="chip-row">
                {(['NEEDS_REPLY', 'REPLIED', 'NO_REPLY_NEEDED'] as const).map((status) => (
                  <button
                    key={status}
                    className={`chip ${share.replyStatus === status ? 'active' : ''}`}
                    onClick={() => setReplyStatus(share.id, status)}
                  >
                    {status === 'NEEDS_REPLY'
                      ? t.needsReply
                      : status === 'NO_REPLY_NEEDED'
                        ? t.noReplyNeeded
                        : t.replied}
                  </button>
                ))}
              </div>
              <div className="chip-row">
                {REPLY_PRESETS.map((preset) => (
                  <button key={preset} className="chip" onClick={() => void copyReply(share.id, preset)}>
                    {preset}
                  </button>
                ))}
              </div>
              <div className="field">
                <label>{t.customReply}</label>
                <input
                  value={custom[share.id] ?? share.replyText ?? ''}
                  onChange={(e) => setCustom((m) => ({ ...m, [share.id]: e.target.value }))}
                />
              </div>
              <button
                onClick={() => {
                  const text = (custom[share.id] ?? '').trim()
                  if (text) void copyReply(share.id, text)
                }}
              >
                {t.copyReply}
              </button>
            </div>
          )
        })}
        <Link to="/">{t.backToInbox}</Link>
      </div>
    </Shell>
  )
}

export function SendersPage() {
  const { t } = useLocale()
  const db = useDb()
  const senders = orderedSenders(db)
  const [names, setNames] = useState<Record<string, string>>({})
  const [mergeFrom, setMergeFrom] = useState<string | null>(null)

  return (
    <Shell title={t.senders}>
      <p className="muted">{t.sendersHint}</p>
      {senders.map((sender) => {
        const clips = db.shares
          .filter((s) => s.senderId === sender.id)
          .map((s) => s.clipId)
        const uniqueClips = [...new Set(clips)]
        return (
          <div key={sender.id} className="panel stack">
            <div className="row">
              <button className="ghost" onClick={() => setFavorite(sender.id, !sender.isFavorite)}>
                {sender.isFavorite ? '★' : '☆'}
              </button>
              <strong>{sender.displayName}</strong>
              <span className="muted">{t.clipsCount(uniqueClips.length)}</span>
            </div>
            <input
              value={names[sender.id] ?? sender.displayName}
              onChange={(e) => setNames((m) => ({ ...m, [sender.id]: e.target.value }))}
            />
            <div className="row">
              <button
                onClick={() => renameSender(sender.id, names[sender.id] ?? sender.displayName)}
              >
                {t.saveName}
              </button>
              <button
                onClick={() => {
                  if (!mergeFrom) {
                    setMergeFrom(sender.id)
                    return
                  }
                  if (mergeFrom === sender.id) {
                    setMergeFrom(null)
                    return
                  }
                  const from = getState().senders.find((s) => s.id === mergeFrom)
                  if (from && confirm(t.mergeConfirm(from.displayName, sender.displayName))) {
                    mergeSenders(mergeFrom, sender.id)
                  }
                  setMergeFrom(null)
                }}
              >
                {!mergeFrom
                  ? t.mergeSource
                  : mergeFrom === sender.id
                    ? t.sourceSelected
                    : t.mergeInto}
              </button>
            </div>
          </div>
        )
      })}
      <Link to="/">{t.backToInbox}</Link>
    </Shell>
  )
}

export function CategoriesPage() {
  const { t } = useLocale()
  const db = useDb()
  const [name, setName] = useState('')
  const [edits, setEdits] = useState<Record<string, string>>({})

  return (
    <Shell title={t.categories}>
      <div className="panel stack">
        <div className="field">
          <label>{t.newCategory}</label>
          <div className="row">
            <input value={name} onChange={(e) => setName(e.target.value)} />
            <button
              className="primary"
              onClick={() => {
                try {
                  createCategory(name)
                  setName('')
                } catch (e) {
                  alert(e instanceof Error ? e.message : t.failed)
                }
              }}
            >
              {t.create}
            </button>
          </div>
        </div>
        {db.categories.map((c) => (
          <div key={c.id} className="stack">
            <input
              value={edits[c.id] ?? c.name}
              onChange={(e) => setEdits((m) => ({ ...m, [c.id]: e.target.value }))}
            />
            <div className="row">
              <button onClick={() => renameCategory(c.id, edits[c.id] ?? c.name)}>{t.rename}</button>
              <button onClick={() => deleteCategory(c.id)}>{t.delete}</button>
            </div>
          </div>
        ))}
      </div>
      <Link to="/">{t.backToInbox}</Link>
    </Shell>
  )
}

export function PrivacyPage() {
  const { t } = useLocale()
  return (
    <Shell title={t.privacyTitle}>
      <div className="panel stack">
        <p>{t.privacyP1}</p>
        <p>{t.privacyP2}</p>
        <p>{t.privacyP3}</p>
        <p>{t.privacyP4}</p>
        <Link to="/">{t.backToInbox}</Link>
      </div>
    </Shell>
  )
}
