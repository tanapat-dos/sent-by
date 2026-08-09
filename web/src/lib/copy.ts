export const APP_NAME = 'Sent By'
export const TAGLINE = 'Watch once. Reply to everyone.'
export const BLURB = 'All the clips your friends send you, organized into one catch-up inbox.'

export function sentBy(names: string[]): string {
  const cleaned = names.map((n) => n.trim()).filter(Boolean)
  if (cleaned.length === 0) return 'No senders yet'
  if (cleaned.length === 1) return `Sent by ${cleaned[0]}`
  if (cleaned.length === 2) return `Sent by ${cleaned[0]} and ${cleaned[1]}`
  const head = cleaned.slice(0, -1).join(', ')
  return `Sent by ${head}, and ${cleaned[cleaned.length - 1]}`
}

export function peopleAwaitingReply(count: number): string {
  if (count === 0) return 'No one awaiting reply'
  if (count === 1) return '1 person awaiting reply'
  return `${count} people awaiting reply`
}

export function statusLine(completed: boolean, watched: boolean, outstanding: number): string {
  if (completed) return 'Done'
  if (watched && outstanding > 0) return `Watched · ${peopleAwaitingReply(outstanding)}`
  if (watched) return 'Watched'
  if (outstanding > 0) return `Unwatched · ${peopleAwaitingReply(outstanding)}`
  return 'Unwatched'
}

export function alreadySavedAddedSender(senderName: string): string {
  return `Already saved — added ${senderName} as another sender`
}

export function savedMessage(created: number, existing: number, senderName: string): string {
  if (existing && !created) return alreadySavedAddedSender(senderName)
  if (existing) return `Saved ${created} new, updated ${existing} existing.`
  return created === 1 ? 'Saved 1 clip.' : `Saved ${created} clips.`
}

export const ALL_CAUGHT_UP = "You're all caught up"
export const EMPTY_INBOX =
  'Your catch-up inbox is empty. Paste a link to start catching up.'
export const COMPLETED_DETAIL = 'Done — watched and all replies handled'
