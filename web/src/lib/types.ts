export type Platform = 'YOUTUBE' | 'TIKTOK' | 'INSTAGRAM' | 'FACEBOOK' | 'OTHER'
export type WatchStatus = 'UNWATCHED' | 'WATCHED'
export type ReplyStatus = 'NEEDS_REPLY' | 'REPLIED' | 'NO_REPLY_NEEDED'
export type SourceApp = 'LINE' | 'MESSENGER' | 'OTHER'
export type InboxFilter = 'ALL' | 'UNWATCHED' | 'WATCHED' | 'NEEDS_REPLY' | 'COMPLETED'

export interface Clip {
  id: string
  originalUrl: string
  canonicalUrl: string
  platform: Platform
  platformContentId: string | null
  title: string | null
  creatorName: string | null
  thumbnailUrl: string | null
  watchStatus: WatchStatus
  createdAt: number
  lastReceivedAt: number
}

export interface ShareRecord {
  id: string
  clipId: string
  senderId: string
  sourceApp: SourceApp
  receivedAt: number
  originalText: string | null
  replyStatus: ReplyStatus
  replyText: string | null
}

export interface Sender {
  id: string
  displayName: string
  lastUsedAt: number
  createdAt: number
  isFavorite: boolean
}

export interface Category {
  id: string
  name: string
  createdAt: number
  updatedAt: number
}

export interface ClipCategory {
  clipId: string
  categoryId: string
}

export interface DbState {
  clips: Clip[]
  shares: ShareRecord[]
  senders: Sender[]
  categories: Category[]
  clipCategories: ClipCategory[]
}

export interface CanonicalUrlResult {
  originalUrl: string
  canonicalUrl: string
  platform: Platform
  platformContentId: string | null
}

export function isCompleted(clip: Clip, shares: ShareRecord[]): boolean {
  if (clip.watchStatus !== 'WATCHED') return false
  if (shares.length === 0) return false
  return shares.every((s) => s.replyStatus !== 'NEEDS_REPLY')
}

export function outstandingReplyCount(shares: ShareRecord[]): number {
  return shares.filter((s) => s.replyStatus === 'NEEDS_REPLY').length
}

export function uid(): string {
  return crypto.randomUUID()
}
