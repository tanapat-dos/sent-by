export type Lang = 'en' | 'th'

export type HelpStep = { title: string; body: string }

export type Dictionary = {
  appName: string
  tagline: string
  blurb: string
  inbox: string
  senders: string
  done: string
  pasteLink: string
  categories: string
  privacy: string
  howToUse: string
  close: string
  gotIt: string
  helpTitle: string
  helpOverview: string
  helpSteps: HelpStep[]
  helpFoot: string
  navTips: {
    inbox: string
    senders: string
    done: string
    paste: string
    categories: string
    help: string
    privacy: string
  }
  webDemoNote: string
  doneNote: string
  searchPlaceholder: string
  unwatched: string
  watched: string
  needsReply: string
  allCategories: string
  emptyInbox: string
  allCaughtUp: string
  completedDetail: string
  noSendersYet: string
  noOneAwaiting: string
  onePersonAwaiting: string
  peopleAwaiting: (n: number) => string
  sentByOne: (name: string) => string
  sentByTwo: (a: string, b: string) => string
  sentByMany: (head: string, last: string) => string
  alreadySaved: (name: string) => string
  savedNewExisting: (created: number, existing: number) => string
  savedOne: string
  savedMany: (n: number) => string
  selectSender: string
  saveFailed: string
  pasteTitle: string
  pasteLabel: string
  urlsDetected: (n: number) => string
  sender: string
  favorites: string
  newSender: string
  create: string
  sourceApp: string
  save: string
  backToInbox: string
  clipNotFound: string
  openClip: string
  markWatched: string
  watchedUndo: string
  openMarksWatched: string
  noCategories: string
  unknown: string
  noReplyNeeded: string
  replied: string
  customReply: string
  copyReply: string
  copiedAlert: string
  sendersHint: string
  clipsCount: (n: number) => string
  saveName: string
  mergeSource: string
  sourceSelected: string
  mergeInto: string
  mergeConfirm: (from: string, into: string) => string
  newCategory: string
  rename: string
  delete: string
  failed: string
  privacyTitle: string
  privacyP1: string
  privacyP2: string
  privacyP3: string
  privacyP4: string
  language: string
  langEn: string
  langTh: string
  dragHint: string
  dragNeedCategories: string
  dropOnCategory: string
  assignedToCategory: (name: string) => string
  alreadyInCategory: (name: string) => string
}

const en: Dictionary = {
  appName: 'Sent By',
  tagline: 'Watch once. Reply to everyone.',
  blurb: 'All the clips your friends send you, organized into one catch-up inbox.',
  inbox: 'Inbox',
  senders: 'Senders',
  done: 'Done',
  pasteLink: 'Paste link',
  categories: 'Categories',
  privacy: 'Privacy',
  howToUse: 'How to use',
  close: 'Close',
  gotIt: 'Got it',
  helpTitle: 'How Sent By works',
  helpOverview:
    'Sent By is a catch-up inbox for short video links friends send you. Save each clip once, watch it, then reply to everyone who shared it — without digging through chat history.',
  helpSteps: [
    {
      title: 'Save a clip',
      body: 'Paste a link (or share into the Android app from LINE/Messenger) and pick who sent it. If the same URL arrives again from someone else, we add them as another sender.',
    },
    {
      title: 'Watch once',
      body: 'Open the clip from your inbox. Opening marks it watched so you do not rewatch the same video for every person.',
    },
    {
      title: 'Reply to everyone',
      body: 'On the clip, reply per sender. Copy a quick reply, then paste it into the chat app yourself. Mark replied when you are done.',
    },
    {
      title: 'Stay caught up',
      body: 'Use Inbox for open items, Senders to manage people (and favorites), and Done for clips you have watched and finished replying to.',
    },
  ],
  helpFoot: 'Tip: open this anytime from How to use. Your data stays in this browser.',
  navTips: {
    inbox: 'Open clips waiting to be watched or replied to',
    senders: 'People who send you clips — rename, favorite, or merge duplicates',
    done: 'Clips you already watched and finished replying to',
    paste: 'Add a video link and choose who sent it',
    categories: 'Optional labels like Friends or Later',
    help: 'How Sent By works',
    privacy: 'What we store on this device',
  },
  webDemoNote: 'Web demo — paste links here. Data stays in this browser only.',
  doneNote: "Clips you've watched and replied to.",
  searchPlaceholder: 'Search sender, platform, title, URL',
  unwatched: 'Unwatched',
  watched: 'Watched',
  needsReply: 'Needs reply',
  allCategories: 'All categories',
  emptyInbox: 'Your catch-up inbox is empty. Paste a link to start catching up.',
  allCaughtUp: "You're all caught up",
  completedDetail: 'Done — watched and all replies handled',
  noSendersYet: 'No senders yet',
  noOneAwaiting: 'No one awaiting reply',
  onePersonAwaiting: '1 person awaiting reply',
  peopleAwaiting: (n) => `${n} people awaiting reply`,
  sentByOne: (name) => `Sent by ${name}`,
  sentByTwo: (a, b) => `Sent by ${a} and ${b}`,
  sentByMany: (head, last) => `Sent by ${head}, and ${last}`,
  alreadySaved: (name) => `Already saved — added ${name} as another sender`,
  savedNewExisting: (created, existing) => `Saved ${created} new, updated ${existing} existing.`,
  savedOne: 'Saved 1 clip.',
  savedMany: (n) => `Saved ${n} clips.`,
  selectSender: 'Select or create a sender',
  saveFailed: 'Save failed',
  pasteTitle: 'Paste link',
  pasteLabel: 'Paste link or text with URLs',
  urlsDetected: (n) => `${n} URL(s) detected`,
  sender: 'Sender',
  favorites: 'Favorites',
  newSender: 'New sender',
  create: 'Create',
  sourceApp: 'Source app',
  save: 'Save',
  backToInbox: 'Back to inbox',
  clipNotFound: 'Clip not found.',
  openClip: 'Open clip',
  markWatched: 'Mark watched',
  watchedUndo: 'Watched (tap to undo)',
  openMarksWatched: 'Opening marks it watched. Reply status stays manual.',
  noCategories: 'No categories yet.',
  unknown: 'Unknown',
  noReplyNeeded: 'No reply needed',
  replied: 'Replied',
  customReply: 'Custom reply',
  copyReply: 'Copy reply',
  copiedAlert: 'Copied — paste into LINE, Messenger, or another chat app yourself.',
  sendersHint: 'Favorites appear first when pasting links. Merge asks for confirmation.',
  clipsCount: (n) => `${n} clip(s)`,
  saveName: 'Save name',
  mergeSource: 'Use as merge source',
  sourceSelected: 'Source selected',
  mergeInto: 'Merge into this sender',
  mergeConfirm: (from, into) => `Merge "${from}" into "${into}"?`,
  newCategory: 'New category',
  rename: 'Rename',
  delete: 'Delete',
  failed: 'Failed',
  privacyTitle: 'Privacy & data',
  privacyP1: 'Sent By stores only what you paste into this browser.',
  privacyP2:
    'Locally we may store URLs, sender labels you create, notes/replies, categories, and public preview metadata when available.',
  privacyP3: 'We do not read LINE or Messenger. There is no cloud sync in this demo.',
  privacyP4: 'Clearing site data in your browser deletes your inbox.',
  language: 'Language',
  langEn: 'EN',
  langTh: 'ไทย',
  dragHint: 'Drag a clip onto a category to organize it.',
  dragNeedCategories: 'Create a category first, then drag clips onto it.',
  dropOnCategory: 'Drop to assign',
  assignedToCategory: (name) => `Added to ${name}`,
  alreadyInCategory: (name) => `Already in ${name}`,
}

const th: Dictionary = {
  appName: 'Sent By',
  tagline: 'ดูครั้งเดียว ตอบทุกคน',
  blurb: 'คลิปทั้งหมดที่เพื่อนส่งมา จัดไว้ในกล่องตามทันที่เดียว',
  inbox: 'กล่องรับ',
  senders: 'ผู้ส่ง',
  done: 'เสร็จแล้ว',
  pasteLink: 'วางลิงก์',
  categories: 'หมวดหมู่',
  privacy: 'ความเป็นส่วนตัว',
  howToUse: 'วิธีใช้',
  close: 'ปิด',
  gotIt: 'เข้าใจแล้ว',
  helpTitle: 'Sent By ทำงานยังไง',
  helpOverview:
    'Sent By คือกล่องตามทันสำหรับลิงก์วิดีโอสั้นที่เพื่อนส่งมา บันทึกคลิปครั้งเดียว ดูแล้ว ค่อยตอบทุกคนที่ส่งมา — ไม่ต้องคุ้ยแชทเก่า',
  helpSteps: [
    {
      title: 'บันทึกคลิป',
      body: 'วางลิงก์ (หรือแชร์เข้าแอป Android จาก LINE/Messenger) แล้วเลือกว่าใครส่งมา ถ้า URL เดิมมาจากคนอื่นอีก จะเพิ่มเป็นผู้ส่งอีกคน',
    },
    {
      title: 'ดูครั้งเดียว',
      body: 'เปิดคลิปจากกล่องรับ การเปิดจะทำเครื่องหมายว่าดูแล้ว เพื่อไม่ต้องดูซ้ำสำหรับทุกคน',
    },
    {
      title: 'ตอบทุกคน',
      body: 'ในหน้าคลิป ตอบแยกตามผู้ส่ง คัดลอกข้อความตอบด่วน แล้ววางในแอปแชทเอง เมื่อตอบแล้วให้ทำเครื่องหมายว่าตอบแล้ว',
    },
    {
      title: 'ตามทันอยู่เสมอ',
      body: 'ใช้กล่องรับสำหรับรายการที่ยังไม่จบ ผู้ส่งสำหรับจัดการคน (และรายการโปรด) และเสร็จแล้วสำหรับคลิปที่ดูและตอบครบแล้ว',
    },
  ],
  helpFoot: 'เคล็ดลับ: เปิดคู่มือนี้ได้ทุกเมื่อจากปุ่มวิธีใช้ ข้อมูลอยู่แค่ในเบราว์เซอร์นี้',
  navTips: {
    inbox: 'คลิปที่รอชมหรือรอตอบ',
    senders: 'คนที่ส่งคลิปมา — เปลี่ยนชื่อ ติดดาว หรือรวมชื่อซ้ำ',
    done: 'คลิปที่ดูและตอบครบแล้ว',
    paste: 'เพิ่มลิงก์วิดีโอและเลือกผู้ส่ง',
    categories: 'ป้ายกำกับเพิ่มเติม เช่น เพื่อน หรือไว้ทีหลัง',
    help: 'Sent By ทำงานยังไง',
    privacy: 'เราเก็บอะไรไว้บนอุปกรณ์นี้',
  },
  webDemoNote: 'เดโมเว็บ — วางลิงก์ที่นี่ ข้อมูลอยู่แค่ในเบราว์เซอร์นี้',
  doneNote: 'คลิปที่คุณดูและตอบแล้ว',
  searchPlaceholder: 'ค้นหาผู้ส่ง แพลตฟอร์ม ชื่อ หรือ URL',
  unwatched: 'ยังไม่ดู',
  watched: 'ดูแล้ว',
  needsReply: 'รอตอบ',
  allCategories: 'ทุกหมวดหมู่',
  emptyInbox: 'กล่องตามทันว่างอยู่ วางลิงก์เพื่อเริ่มตามทัน',
  allCaughtUp: 'ตามทันหมดแล้ว',
  completedDetail: 'เสร็จแล้ว — ดูแล้วและตอบครบทุกคน',
  noSendersYet: 'ยังไม่มีผู้ส่ง',
  noOneAwaiting: 'ไม่มีใครรอการตอบ',
  onePersonAwaiting: 'มี 1 คนรอการตอบ',
  peopleAwaiting: (n) => `มี ${n} คนรอการตอบ`,
  sentByOne: (name) => `ส่งโดย ${name}`,
  sentByTwo: (a, b) => `ส่งโดย ${a} และ ${b}`,
  sentByMany: (head, last) => `ส่งโดย ${head} และ ${last}`,
  alreadySaved: (name) => `บันทึกไว้แล้ว — เพิ่ม ${name} เป็นผู้ส่งอีกคน`,
  savedNewExisting: (created, existing) => `บันทึกใหม่ ${created} อัปเดตของเดิม ${existing}`,
  savedOne: 'บันทึก 1 คลิปแล้ว',
  savedMany: (n) => `บันทึก ${n} คลิปแล้ว`,
  selectSender: 'เลือกหรือสร้างผู้ส่ง',
  saveFailed: 'บันทึกไม่สำเร็จ',
  pasteTitle: 'วางลิงก์',
  pasteLabel: 'วางลิงก์หรือข้อความที่มี URL',
  urlsDetected: (n) => `พบ ${n} URL`,
  sender: 'ผู้ส่ง',
  favorites: 'รายการโปรด',
  newSender: 'ผู้ส่งใหม่',
  create: 'สร้าง',
  sourceApp: 'แอปต้นทาง',
  save: 'บันทึก',
  backToInbox: 'กลับไปกล่องรับ',
  clipNotFound: 'ไม่พบคลิป',
  openClip: 'เปิดคลิป',
  markWatched: 'ทำเครื่องหมายว่าดูแล้ว',
  watchedUndo: 'ดูแล้ว (แตะเพื่อยกเลิก)',
  openMarksWatched: 'การเปิดจะทำเครื่องหมายว่าดูแล้ว สถานะตอบยังต้องกดเอง',
  noCategories: 'ยังไม่มีหมวดหมู่',
  unknown: 'ไม่ทราบ',
  noReplyNeeded: 'ไม่ต้องตอบ',
  replied: 'ตอบแล้ว',
  customReply: 'ข้อความตอบเอง',
  copyReply: 'คัดลอกข้อความตอบ',
  copiedAlert: 'คัดลอกแล้ว — ไปวางใน LINE, Messenger หรือแอปแชทอื่นเอง',
  sendersHint: 'รายการโปรดจะขึ้นก่อนตอนวางลิงก์ การรวมชื่อจะถามยืนยัน',
  clipsCount: (n) => `${n} คลิป`,
  saveName: 'บันทึกชื่อ',
  mergeSource: 'ใช้เป็นต้นทางรวม',
  sourceSelected: 'เลือกต้นทางแล้ว',
  mergeInto: 'รวมเข้าผู้ส่งนี้',
  mergeConfirm: (from, into) => `รวม "${from}" เข้า "${into}"?`,
  newCategory: 'หมวดหมู่ใหม่',
  rename: 'เปลี่ยนชื่อ',
  delete: 'ลบ',
  failed: 'ไม่สำเร็จ',
  privacyTitle: 'ความเป็นส่วนตัวและข้อมูล',
  privacyP1: 'Sent By เก็บเฉพาะสิ่งที่คุณวางลงในเบราว์เซอร์นี้',
  privacyP2:
    'ในเครื่องอาจเก็บ URL ชื่อผู้ส่งที่คุณสร้าง โน้ต/ข้อความตอบ หมวดหมู่ และข้อมูลพรีวิวสาธารณะเมื่อมี',
  privacyP3: 'เราไม่อ่าน LINE หรือ Messenger และไม่มีซิงก์คลาวด์ในเดโมนี้',
  privacyP4: 'การล้างข้อมูลไซต์ในเบราว์เซอร์จะลบกล่องรับของคุณ',
  language: 'ภาษา',
  langEn: 'EN',
  langTh: 'ไทย',
  dragHint: 'ลากคลิปไปวางบนหมวดหมู่เพื่อจัดกลุ่ม',
  dragNeedCategories: 'สร้างหมวดหมู่ก่อน แล้วลากคลิปไปวาง',
  dropOnCategory: 'วางเพื่อใส่หมวดหมู่',
  assignedToCategory: (name) => `เพิ่มใน ${name} แล้ว`,
  alreadyInCategory: (name) => `อยู่ใน ${name} อยู่แล้ว`,
}

const dictionaries: Record<Lang, Dictionary> = { en, th }

export function getDictionary(lang: Lang): Dictionary {
  return dictionaries[lang] ?? en
}

export function sentBy(names: string[], t: Dictionary): string {
  const cleaned = names.map((n) => n.trim()).filter(Boolean)
  if (cleaned.length === 0) return t.noSendersYet
  if (cleaned.length === 1) return t.sentByOne(cleaned[0])
  if (cleaned.length === 2) return t.sentByTwo(cleaned[0], cleaned[1])
  const head = cleaned.slice(0, -1).join(', ')
  return t.sentByMany(head, cleaned[cleaned.length - 1])
}

export function peopleAwaitingReply(count: number, t: Dictionary): string {
  if (count === 0) return t.noOneAwaiting
  if (count === 1) return t.onePersonAwaiting
  return t.peopleAwaiting(count)
}

export function statusLine(
  completed: boolean,
  watched: boolean,
  outstanding: number,
  t: Dictionary,
): string {
  if (completed) return t.done
  if (watched && outstanding > 0) return `${t.watched} · ${peopleAwaitingReply(outstanding, t)}`
  if (watched) return t.watched
  if (outstanding > 0) return `${t.unwatched} · ${peopleAwaitingReply(outstanding, t)}`
  return t.unwatched
}

export function savedMessage(
  created: number,
  existing: number,
  senderName: string,
  t: Dictionary,
): string {
  if (existing && !created) return t.alreadySaved(senderName)
  if (existing) return t.savedNewExisting(created, existing)
  return created === 1 ? t.savedOne : t.savedMany(created)
}

/** @deprecated use getDictionary + helpers */
export const APP_NAME = en.appName
