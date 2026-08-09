import type {
  Category,
  Clip,
  ClipCategory,
  DbState,
  ReplyStatus,
  Sender,
  ShareRecord,
  SourceApp,
  WatchStatus,
} from './types'
import { canonicalize, extractUrls } from './urls'
import { uid } from './types'

const KEY = 'reelshelf.web.v1'

const empty: DbState = {
  clips: [],
  shares: [],
  senders: [],
  categories: [],
  clipCategories: [],
}

function load(): DbState {
  try {
    const raw = localStorage.getItem(KEY)
    if (!raw) return structuredClone(empty)
    return { ...empty, ...JSON.parse(raw) }
  } catch {
    return structuredClone(empty)
  }
}

function save(state: DbState) {
  localStorage.setItem(KEY, JSON.stringify(state))
}

type Listener = () => void
const listeners = new Set<Listener>()

export function subscribe(listener: Listener): () => void {
  listeners.add(listener)
  return () => listeners.delete(listener)
}

function notify() {
  listeners.forEach((l) => l())
}

export function getState(): DbState {
  return load()
}

function update(mutator: (state: DbState) => void): DbState {
  const state = load()
  mutator(state)
  save(state)
  notify()
  return state
}

export function createSender(displayName: string): Sender {
  const trimmed = displayName.trim()
  if (!trimmed) throw new Error('Sender name required')
  const now = Date.now()
  const sender: Sender = {
    id: uid(),
    displayName: trimmed,
    lastUsedAt: now,
    createdAt: now,
    isFavorite: false,
  }
  update((s) => {
    s.senders.push(sender)
  })
  return sender
}

export function setFavorite(senderId: string, isFavorite: boolean) {
  update((s) => {
    const sender = s.senders.find((x) => x.id === senderId)
    if (sender) sender.isFavorite = isFavorite
  })
}

export function renameSender(senderId: string, displayName: string) {
  const trimmed = displayName.trim()
  if (!trimmed) throw new Error('Sender name required')
  update((s) => {
    const sender = s.senders.find((x) => x.id === senderId)
    if (sender) sender.displayName = trimmed
  })
}

export function mergeSenders(fromId: string, intoId: string) {
  if (fromId === intoId) return
  update((s) => {
    s.shares.forEach((share) => {
      if (share.senderId === fromId) share.senderId = intoId
    })
    s.senders = s.senders.filter((x) => x.id !== fromId)
    const into = s.senders.find((x) => x.id === intoId)
    if (into) into.lastUsedAt = Date.now()
  })
}

export function createCategory(name: string): Category {
  const trimmed = name.trim()
  if (!trimmed) throw new Error('Category name required')
  const state = load()
  if (state.categories.some((c) => c.name.toLowerCase() === trimmed.toLowerCase())) {
    throw new Error('Category already exists')
  }
  const now = Date.now()
  const category: Category = { id: uid(), name: trimmed, createdAt: now, updatedAt: now }
  update((s) => {
    s.categories.push(category)
  })
  return category
}

export function renameCategory(id: string, name: string) {
  const trimmed = name.trim()
  if (!trimmed) throw new Error('Category name required')
  update((s) => {
    const cat = s.categories.find((c) => c.id === id)
    if (cat) {
      cat.name = trimmed
      cat.updatedAt = Date.now()
    }
  })
}

export function deleteCategory(id: string) {
  update((s) => {
    s.categories = s.categories.filter((c) => c.id !== id)
    s.clipCategories = s.clipCategories.filter((cc) => cc.categoryId !== id)
  })
}

export function setClipCategory(clipId: string, categoryId: string, assigned: boolean) {
  update((s) => {
    s.clipCategories = s.clipCategories.filter(
      (cc) => !(cc.clipId === clipId && cc.categoryId === categoryId),
    )
    if (assigned) s.clipCategories.push({ clipId, categoryId })
  })
}

export interface IngestResult {
  clipId: string
  wasExisting: boolean
}

export function ingest(text: string, senderId: string, sourceApp: SourceApp): IngestResult[] {
  const urls = extractUrls(text)
  if (urls.length === 0) throw new Error('No http(s) URLs found')
  const now = Date.now()
  const results: IngestResult[] = []

  update((s) => {
    const sender = s.senders.find((x) => x.id === senderId)
    if (!sender) throw new Error('Unknown sender')
    sender.lastUsedAt = now

    for (const url of urls) {
      const canonical = canonicalize(url)
      let clip = s.clips.find(
        (c) =>
          (canonical.platformContentId &&
            c.platform === canonical.platform &&
            c.platformContentId === canonical.platformContentId) ||
          c.canonicalUrl === canonical.canonicalUrl,
      )
      let wasExisting = true
      if (!clip) {
        wasExisting = false
        clip = {
          id: uid(),
          originalUrl: canonical.originalUrl,
          canonicalUrl: canonical.canonicalUrl,
          platform: canonical.platform,
          platformContentId: canonical.platformContentId,
          title: null,
          creatorName: null,
          thumbnailUrl: null,
          watchStatus: 'UNWATCHED',
          createdAt: now,
          lastReceivedAt: now,
        }
        s.clips.push(clip)
      } else {
        clip.lastReceivedAt = now
      }

      const share: ShareRecord = {
        id: uid(),
        clipId: clip.id,
        senderId,
        sourceApp,
        receivedAt: now,
        originalText: text,
        replyStatus: 'NEEDS_REPLY',
        replyText: null,
      }
      s.shares.push(share)
      results.push({ clipId: clip.id, wasExisting })
    }
  })

  // Fire-and-forget YouTube oEmbed enrichment
  results.forEach((r) => {
    void enrichYoutube(r.clipId)
  })

  return results
}

async function enrichYoutube(clipId: string) {
  const state = load()
  const clip = state.clips.find((c) => c.id === clipId)
  if (!clip || clip.platform !== 'YOUTUBE') return
  try {
    const endpoint =
      'https://www.youtube.com/oembed?format=json&url=' + encodeURIComponent(clip.canonicalUrl)
    const res = await fetch(endpoint)
    if (!res.ok) return
    const json = (await res.json()) as {
      title?: string
      author_name?: string
      thumbnail_url?: string
    }
    update((s) => {
      const c = s.clips.find((x) => x.id === clipId)
      if (!c) return
      c.title = json.title || c.title
      c.creatorName = json.author_name || c.creatorName
      c.thumbnailUrl = json.thumbnail_url || c.thumbnailUrl
    })
  } catch {
    // ignore CORS / network failures — URL remains saved
  }
}

export function setWatchStatus(clipId: string, watchStatus: WatchStatus) {
  update((s) => {
    const clip = s.clips.find((c) => c.id === clipId)
    if (clip) clip.watchStatus = watchStatus
  })
}

export function setReplyStatus(shareId: string, replyStatus: ReplyStatus, replyText?: string | null) {
  update((s) => {
    const share = s.shares.find((x) => x.id === shareId)
    if (!share) return
    share.replyStatus = replyStatus
    if (replyText !== undefined) share.replyText = replyText
  })
}

export function orderedSenders(state: DbState = load()): Sender[] {
  return [...state.senders].sort((a, b) => {
    if (a.isFavorite !== b.isFavorite) return a.isFavorite ? -1 : 1
    return b.lastUsedAt - a.lastUsedAt
  })
}

export type { Clip, ShareRecord, Sender, Category, ClipCategory, DbState }
