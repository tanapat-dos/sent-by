import type { CanonicalUrlResult, Platform } from './types'

const TRACKING = new Set([
  'utm_source', 'utm_medium', 'utm_campaign', 'utm_term', 'utm_content', 'utm_id',
  'fbclid', 'gclid', 'igshid', 'igsh', 'si', 'feature', 'pp', 'ref', 'ref_url',
  'mibextid', 'rdid', 'share_app_id', 'share_id', '_r', '_t',
])

export function extractUrls(input: string | null | undefined): string[] {
  if (!input?.trim()) return []
  const re = /https?:\/\/[^\s<>"'\\]+/gi
  const found = new Set<string>()
  for (const match of input.matchAll(re)) {
    const cleaned = stripTrailingJunk(match[0])
    if (isHttpUrl(cleaned)) found.add(cleaned)
  }
  return [...found]
}

function stripTrailingJunk(raw: string): string {
  return raw.replace(/[)\]}>,.;:!?'"”’]+$/g, '')
}

function isHttpUrl(value: string): boolean {
  try {
    const u = new URL(value)
    return (u.protocol === 'http:' || u.protocol === 'https:') && !!u.hostname
  } catch {
    return false
  }
}

function parseQuery(search: string): Record<string, string> {
  const out: Record<string, string> = {}
  const q = search.startsWith('?') ? search.slice(1) : search
  if (!q) return out
  for (const part of q.split('&')) {
    if (!part) continue
    const i = part.indexOf('=')
    const k = decodeURIComponent(i < 0 ? part : part.slice(0, i))
    const v = decodeURIComponent(i < 0 ? '' : part.slice(i + 1))
    out[k] = v
  }
  return out
}

function stripTracking(params: Record<string, string>, keep: string[] = []): Record<string, string> {
  const keepSet = new Set(keep)
  return Object.fromEntries(
    Object.entries(params).filter(([k]) => keepSet.has(k) || !TRACKING.has(k.toLowerCase())),
  )
}

function buildQuery(params: Record<string, string>): string {
  return Object.entries(params)
    .map(([k, v]) => `${encodeURIComponent(k)}=${encodeURIComponent(v)}`)
    .join('&')
}

function rebuild(host: string, path: string, query: Record<string, string> = {}): string {
  const q = buildQuery(query)
  const normalized = path.replace(/\/+$/, '') || ''
  return `https://${host.toLowerCase()}${normalized}${q ? `?${q}` : ''}`
}

function segments(pathname: string): string[] {
  return pathname.split('/').filter(Boolean)
}

export function canonicalize(originalUrl: string): CanonicalUrlResult {
  const trimmed = originalUrl.trim()
  let url: URL
  try {
    url = new URL(trimmed)
  } catch {
    return { originalUrl: trimmed, canonicalUrl: trimmed, platform: 'OTHER', platformContentId: null }
  }
  const host = url.hostname.toLowerCase()
  const params = parseQuery(url.search)
  const segs = segments(url.pathname)

  if (
    host === 'youtu.be' ||
    host === 'www.youtu.be' ||
    host === 'youtube.com' ||
    host === 'www.youtube.com' ||
    host === 'm.youtube.com' ||
    host.endsWith('.youtube.com')
  ) {
    const videoId =
      host === 'youtu.be' || host === 'www.youtu.be'
        ? segs[0]
        : segs[0]?.toLowerCase() === 'shorts' || segs[0]?.toLowerCase() === 'embed' || segs[0]?.toLowerCase() === 'live'
          ? segs[1]
          : params.v
    return {
      originalUrl: trimmed,
      canonicalUrl: videoId
        ? rebuild('www.youtube.com', '/watch', { v: videoId })
        : rebuild('www.youtube.com', url.pathname, stripTracking(params, ['v', 'list'])),
      platform: 'YOUTUBE',
      platformContentId: videoId || null,
    }
  }

  if (host.includes('tiktok.com')) {
    const videoIdx = segs.findIndex((s) => s.toLowerCase() === 'video')
    const videoId = videoIdx >= 0 ? segs[videoIdx + 1] : null
    const user = segs.find((s) => s.startsWith('@'))
    const path =
      videoId && user ? `/${user}/video/${videoId}` : videoId ? `/video/${videoId}` : url.pathname
    const short = host === 'vt.tiktok.com' || host === 'vm.tiktok.com'
    return {
      originalUrl: trimmed,
      canonicalUrl: short
        ? rebuild(host, url.pathname)
        : rebuild('www.tiktok.com', path),
      platform: 'TIKTOK',
      platformContentId: videoId && /^\d+$/.test(videoId) ? videoId : null,
    }
  }

  if (host.includes('instagram.com') || host.includes('instagr.am')) {
    const kindIdx = segs.findIndex((s) => ['reel', 'reels', 'p', 'tv'].includes(s.toLowerCase()))
    const contentId = kindIdx >= 0 ? segs[kindIdx + 1] : null
    const kind = kindIdx >= 0 ? (segs[kindIdx].toLowerCase() === 'reels' ? 'reel' : segs[kindIdx].toLowerCase()) : null
    return {
      originalUrl: trimmed,
      canonicalUrl:
        kind && contentId
          ? rebuild('www.instagram.com', `/${kind}/${contentId}`)
          : rebuild('www.instagram.com', url.pathname, stripTracking(params)),
      platform: 'INSTAGRAM',
      platformContentId: contentId || null,
    }
  }

  if (host.includes('facebook.com') || host === 'fb.watch' || host === 'www.fb.watch' || host.includes('fb.com')) {
    const contentId =
      (params.v && /^\d+$/.test(params.v) ? params.v : null) ||
      params.story_fbid ||
      (segs[0]?.toLowerCase() === 'reel' ? segs[1] : null) ||
      (host.includes('fb.watch') ? segs[0] : null)
    return {
      originalUrl: trimmed,
      canonicalUrl: contentId
        ? host.includes('fb.watch')
          ? rebuild('fb.watch', `/${contentId}`)
          : segs[0]?.toLowerCase() === 'reel'
            ? rebuild('www.facebook.com', `/reel/${contentId}`)
            : rebuild('www.facebook.com', '/watch', { v: contentId })
        : rebuild(host.includes('fb.watch') ? 'fb.watch' : 'www.facebook.com', url.pathname, stripTracking(params, ['v', 'story_fbid'])),
      platform: 'FACEBOOK',
      platformContentId: contentId || null,
    }
  }

  return {
    originalUrl: trimmed,
    canonicalUrl: rebuild(host, url.pathname, stripTracking(params)),
    platform: 'OTHER' as Platform,
    platformContentId: null,
  }
}

export function hostOf(url: string): string | null {
  try {
    return new URL(url).hostname.replace(/^www\./, '')
  } catch {
    return null
  }
}

export function displayTitle(title: string | null, originalUrl: string, canonicalUrl = originalUrl): string {
  if (title?.trim()) return title
  const host = hostOf(canonicalUrl)
  return host ? `${host} video` : originalUrl
}
