import { Link } from 'react-router-dom'
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
import { displayTitle, extractUrls } from './lib/urls'
import { useDb } from './lib/useDb'
import { Shell } from './components/Shell'
import { useMemo, useState, type FormEvent } from 'react'

const REPLY_PRESETS = ['😂', '❤️', 'That was good']

export function InboxPage() {
  const db = useDb()
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState<InboxFilter>('ALL')
  const [categoryId, setCategoryId] = useState<string | null>(null)

  const rows = useMemo(() => {
    const q = query.trim().toLowerCase()
    return [...db.clips]
      .map((clip) => {
        const shares = db.shares.filter((s) => s.clipId === clip.id)
        const senders = shares
          .map((s) => db.senders.find((x) => x.id === s.senderId)?.displayName)
          .filter(Boolean)
        const cats = db.clipCategories
          .filter((cc) => cc.clipId === clip.id)
          .map((cc) => db.categories.find((c) => c.id === cc.categoryId)?.name)
          .filter(Boolean)
        return {
          clip,
          shares,
          senderNames: [...new Set(senders)].join(', '),
          categoryNames: cats.join(', '),
          outstanding: outstandingReplyCount(shares),
          completed: isCompleted(clip, shares),
        }
      })
      .filter((row) => {
        if (filter === 'UNWATCHED' && row.clip.watchStatus !== 'UNWATCHED') return false
        if (filter === 'WATCHED' && row.clip.watchStatus !== 'WATCHED') return false
        if (filter === 'NEEDS_REPLY' && row.outstanding === 0) return false
        if (filter === 'COMPLETED' && !row.completed) return false
        if (categoryId && !db.clipCategories.some((cc) => cc.clipId === row.clip.id && cc.categoryId === categoryId)) {
          return false
        }
        if (!q) return true
        const hay = [
          row.clip.title,
          row.clip.creatorName,
          row.clip.originalUrl,
          row.clip.canonicalUrl,
          row.clip.platform,
          row.senderNames,
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
  }, [db, query, filter, categoryId])

  return (
    <Shell>
      <p className="muted">
        Web demo — paste links here. Data stays in this browser only (no Play share sheet).
      </p>
      <div className="field">
        <input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search sender, platform, title, URL"
        />
      </div>
      <div className="chip-row">
        {(['UNWATCHED', 'WATCHED', 'NEEDS_REPLY', 'COMPLETED'] as InboxFilter[]).map((f) => (
          <button
            key={f}
            className={`chip ${filter === f ? 'active' : ''}`}
            onClick={() => setFilter(filter === f ? 'ALL' : f)}
          >
            {f === 'NEEDS_REPLY' ? 'Needs reply' : f[0] + f.slice(1).toLowerCase()}
          </button>
        ))}
      </div>
      {db.categories.length > 0 && (
        <div className="chip-row">
          <button className={`chip ${!categoryId ? 'active' : ''}`} onClick={() => setCategoryId(null)}>
            All categories
          </button>
          {db.categories.map((c) => (
            <button
              key={c.id}
              className={`chip ${categoryId === c.id ? 'active' : ''}`}
              onClick={() => setCategoryId(categoryId === c.id ? null : c.id)}
            >
              {c.name}
            </button>
          ))}
        </div>
      )}
      {rows.length === 0 ? (
        <p className="panel">Inbox is empty. <Link to="/paste">Paste a link</Link> to start.</p>
      ) : (
        rows.map((row) => (
          <Link key={row.clip.id} className="card" to={`/clip/${row.clip.id}`}>
            <div className="thumb">
              {row.clip.thumbnailUrl ? (
                <img src={row.clip.thumbnailUrl} alt="" />
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
                {new Date(row.clip.lastReceivedAt).toLocaleString()} · {row.senderNames || 'No senders'}
                {row.categoryNames ? ` · ${row.categoryNames}` : ''}
              </div>
              <div className={`meta ${row.completed ? 'ok' : ''}`}>
                {row.completed
                  ? 'Completed'
                  : row.clip.watchStatus === 'WATCHED'
                    ? row.outstanding
                      ? `Watched · ${row.outstanding} need reply`
                      : 'Watched'
                    : row.outstanding
                      ? `Unwatched · ${row.outstanding} need reply`
                      : 'Unwatched'}
              </div>
            </div>
            {row.completed ? <div className="check" title="Completed">✓</div> : <div />}
          </Link>
        ))
      )}
    </Shell>
  )
}

export function PastePage() {
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
      if (!sid) {
        if (!newSender.trim()) {
          setMessage('Select or create a sender')
          return
        }
        sid = createSender(newSender).id
        setNewSender('')
      }
      const results = ingest(text, sid, sourceApp)
      const existing = results.filter((r) => r.wasExisting).length
      const created = results.length - existing
      setMessage(
        existing && !created
          ? `Already saved — added sender to ${existing} clip(s).`
          : existing
            ? `Saved ${created} new, updated ${existing} existing.`
            : `Saved ${results.length} clip(s).`,
      )
      setText('')
    } catch (err) {
      setMessage(err instanceof Error ? err.message : 'Save failed')
    }
  }

  const favorites = senders.filter((s) => s.isFavorite)
  const others = senders.filter((s) => !s.isFavorite)

  return (
    <Shell title="Paste link">
      <form className="panel stack" onSubmit={onSave}>
        <div className="field">
          <label>Paste link or text with URLs</label>
          <textarea rows={4} value={text} onChange={(e) => setText(e.target.value)} />
        </div>
        <div className="muted">{urlCount} URL(s) detected</div>
        <div>
          <div className="muted">Sender</div>
          {favorites.length > 0 && (
            <>
              <div className="muted">Favorites</div>
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
            <label>New sender</label>
            <div className="row">
              <input value={newSender} onChange={(e) => setNewSender(e.target.value)} />
              <button type="button" onClick={onCreateSender} disabled={!newSender.trim()}>
                Create
              </button>
            </div>
          </div>
        </div>
        <div>
          <div className="muted">Source app</div>
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
          Save
        </button>
        <Link to="/">Back to inbox</Link>
      </form>
    </Shell>
  )
}

export function ClipDetailPage({ clipId }: { clipId: string }) {
  const db = useDb()
  const clip = db.clips.find((c) => c.id === clipId)
  const shares = db.shares.filter((s) => s.clipId === clipId)
  const assigned = new Set(db.clipCategories.filter((cc) => cc.clipId === clipId).map((cc) => cc.categoryId))
  const completed = clip ? isCompleted(clip, shares) : false
  const [custom, setCustom] = useState<Record<string, string>>({})

  if (!clip) {
    return (
      <Shell>
        <p>Clip not found. <Link to="/">Inbox</Link></p>
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
    alert('Copied — paste into LINE, Messenger, or another chat app yourself.')
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
                <strong style={{ color: 'var(--ok)' }}>Completed — watched and all replies handled</strong>
              </div>
            ) : (
              <div className="meta">{clip.watchStatus.toLowerCase()}</div>
            )}
          </div>
        </div>
        <div className="row">
          <button className="primary" onClick={openClip}>
            Open clip
          </button>
          <button
            onClick={() =>
              setWatchStatus(clip.id, clip.watchStatus === 'WATCHED' ? 'UNWATCHED' : 'WATCHED')
            }
          >
            {clip.watchStatus === 'WATCHED' ? 'Watched (tap to undo)' : 'Mark watched'}
          </button>
        </div>
        <p className="muted">Opening marks it watched. Reply status stays manual.</p>

        <h3>Categories</h3>
        {db.categories.length === 0 ? (
          <p className="muted">No categories yet.</p>
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

        <h3>Senders</h3>
        {shares.map((share) => {
          const sender = db.senders.find((s) => s.id === share.senderId)
          return (
            <div key={share.id} className="panel stack">
              <strong>
                {sender?.displayName ?? 'Unknown'} · {share.sourceApp}
              </strong>
              <div className="chip-row">
                {(['NEEDS_REPLY', 'REPLIED', 'NO_REPLY_NEEDED'] as const).map((status) => (
                  <button
                    key={status}
                    className={`chip ${share.replyStatus === status ? 'active' : ''}`}
                    onClick={() => setReplyStatus(share.id, status)}
                  >
                    {status === 'NEEDS_REPLY'
                      ? 'Needs reply'
                      : status === 'NO_REPLY_NEEDED'
                        ? 'No reply needed'
                        : 'Replied'}
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
                <label>Custom reply</label>
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
                Copy reply
              </button>
            </div>
          )
        })}
        <Link to="/">Back to inbox</Link>
      </div>
    </Shell>
  )
}

export function SendersPage() {
  const db = useDb()
  const senders = orderedSenders(db)
  const [names, setNames] = useState<Record<string, string>>({})
  const [mergeFrom, setMergeFrom] = useState<string | null>(null)

  return (
    <Shell title="Senders">
      <p className="muted">Favorites appear first when pasting links. Merge asks for confirmation.</p>
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
              <span className="muted">{uniqueClips.length} clip(s)</span>
            </div>
            <input
              value={names[sender.id] ?? sender.displayName}
              onChange={(e) => setNames((m) => ({ ...m, [sender.id]: e.target.value }))}
            />
            <div className="row">
              <button
                onClick={() => renameSender(sender.id, names[sender.id] ?? sender.displayName)}
              >
                Save name
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
                  if (
                    from &&
                    confirm(`Merge "${from.displayName}" into "${sender.displayName}"?`)
                  ) {
                    mergeSenders(mergeFrom, sender.id)
                  }
                  setMergeFrom(null)
                }}
              >
                {!mergeFrom
                  ? 'Use as merge source'
                  : mergeFrom === sender.id
                    ? 'Source selected'
                    : 'Merge into this sender'}
              </button>
            </div>
          </div>
        )
      })}
      <Link to="/">Back to inbox</Link>
    </Shell>
  )
}

export function CategoriesPage() {
  const db = useDb()
  const [name, setName] = useState('')
  const [edits, setEdits] = useState<Record<string, string>>({})

  return (
    <Shell title="Categories">
      <div className="panel stack">
        <div className="field">
          <label>New category</label>
          <div className="row">
            <input value={name} onChange={(e) => setName(e.target.value)} />
            <button
              className="primary"
              onClick={() => {
                try {
                  createCategory(name)
                  setName('')
                } catch (e) {
                  alert(e instanceof Error ? e.message : 'Failed')
                }
              }}
            >
              Create
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
              <button onClick={() => renameCategory(c.id, edits[c.id] ?? c.name)}>Rename</button>
              <button onClick={() => deleteCategory(c.id)}>Delete</button>
            </div>
          </div>
        ))}
      </div>
      <Link to="/">Back to inbox</Link>
    </Shell>
  )
}

export function PrivacyPage() {
  return (
    <Shell title="Privacy & data">
      <div className="panel stack">
        <p>ReelShelf Web stores only what you paste into this browser.</p>
        <p>
          Locally we may store URLs, sender labels you create, notes/replies, categories, and public
          preview metadata when available.
        </p>
        <p>We do not read LINE or Messenger. There is no cloud sync in this demo.</p>
        <p>Clearing site data in your browser deletes your inbox.</p>
        <Link to="/">Back to inbox</Link>
      </div>
    </Shell>
  )
}
