# Shared Clips Inbox — Product Requirements

## 1. Product Summary

Shared Clips Inbox is a mobile app for people who receive many short-video links through LINE and Facebook Messenger but do not have time to watch and respond immediately.

Users manually send a received message or link to the app through the phone's native share sheet. The app collects the clip into one catch-up queue, detects repeated links, records who sent each copy, and helps the user track whether they have watched and replied to each sender.

The product is not a general bookmark manager. Its central job is managing the social obligation created by clips sent by other people.

## 2. Problem Statement

Short-video links arrive throughout the day in different conversations. When users return later:

- Clips are scattered across LINE and Messenger chats.
- The same clip may have been sent by multiple people.
- Users cannot easily tell what they have watched or replied to.
- Interesting clips are difficult to find again.
- Watching a clip once does not remove the need to respond to every sender.

## 3. Product Goal

Give users one low-effort inbox where they can process videos shared by friends without losing sender context or watching duplicate clips repeatedly.

### Primary success condition

A user can save a received clip in a few seconds, later watch it once, and see every person who still needs a response.

## 4. Target User

The initial target user:

- Uses LINE and/or Facebook Messenger frequently.
- Receives multiple Reels, TikToks, Shorts, or other video links daily.
- Often postpones watching them until after work or before bed.
- Feels some obligation to acknowledge or respond to the sender.

The first launch market may focus on Android users in Thailand, where LINE and Messenger are common.

## 5. MVP Scope

### 5.1 Supported input

The app must appear as a target in the Android native share sheet.

For MVP, it accepts:

- Shared plain-text URLs
- Shared text containing one or more URLs
- URLs copied and pasted directly into the app

Initial video-link support should include:

- Instagram Reels
- TikTok videos
- YouTube videos and Shorts
- Facebook video links when metadata can be retrieved

LINE and Messenger are the initial source chat apps, but the app must not depend on reading their private inbox APIs.

### 5.2 Quick-save flow

1. User long-presses a message or link in LINE or Messenger.
2. User selects Share and chooses Shared Clips Inbox.
3. The app extracts the URL.
4. The user selects a sender from recent senders or creates a new sender.
5. The user optionally selects the source app if it cannot be inferred.
6. The app saves the share and immediately closes or shows a brief success state.

Target: after choosing the app from the share sheet, saving should require no more than one required user selection and one confirmation. A remembered default or single-tap sender selection may count as confirmation.

### 5.3 Clip inbox

The main screen displays one card per unique clip, not one card per share.

Each card should display, when available:

- Thumbnail
- Platform
- Creator or page name
- Short title or caption
- Date most recently received
- Number and names of senders
- Watch status
- Outstanding reply count

Default ordering: unwatched clips first, then newest received.

Required filters:

- Unwatched
- Watched
- Needs reply
- Completed

### 5.4 Duplicate handling

The MVP must detect duplicates using canonical URLs and platform content IDs.

Normalization should attempt to:

- Expand supported short URLs when practical.
- Remove known tracking parameters.
- Normalize mobile and web URL variants.
- Extract stable platform/video identifiers when available.

When an existing clip is shared again:

- Do not create another clip card.
- Create a new share record linked to the existing clip.
- Preserve the new sender, source app, received time, and optional note.
- Show a confirmation such as: `Already saved — added May as another sender.`

Cross-platform visual/audio fingerprinting is not required for MVP.

### 5.5 Sender management

Because native sharing may not include the sender or conversation identity, sender selection is manual in MVP.

Users must be able to:

- Create a sender using a display name.
- Select from recent senders during quick save.
- Edit or merge duplicate sender records.
- View all clips received from a sender.

Contact-book permission is not required for MVP.

### 5.6 Watching clips

The user can open a clip from the inbox.

- If reliable in-app embedding is available, the app may display the clip in-app.
- Otherwise, open the original URL in the source content app or browser.
- The user must be able to mark the clip watched manually.
- Opening the clip may mark it watched, but this behavior should be configurable or easily reversible.

Watching a clip changes the clip-level status but does not automatically mark sender replies complete.

### 5.7 Reply tracking

Every share record has an independent reply status:

- Needs reply
- Replied
- No reply needed

After watching, the app shows all senders associated with the clip and lets the user update each status.

For MVP, the app may provide quick reaction text such as `😂`, `❤️`, `That was good`, or a custom response. It can copy the response to the clipboard and open LINE or Messenger generally.

The MVP must not claim that it can automatically find the original conversation or send a message without user confirmation.

### 5.8 Basic search

Users can search by:

- Sender name
- Platform
- Title/caption metadata
- Original URL

AI semantic search, transcription, and summarization are not required for MVP.

## 6. Core Data Model

### Clip

| Field | Description |
| --- | --- |
| `id` | Internal unique identifier |
| `original_url` | URL originally received |
| `canonical_url` | Normalized URL used for deduplication |
| `platform` | Instagram, TikTok, YouTube, Facebook, or other |
| `platform_content_id` | Stable content identifier when extractable |
| `title` | Retrieved title/caption when available |
| `creator_name` | Retrieved creator/page when available |
| `thumbnail_url` | Preview image when available |
| `watch_status` | Unwatched or watched |
| `created_at` | First saved time |
| `last_received_at` | Most recent linked share time |

### ShareRecord

| Field | Description |
| --- | --- |
| `id` | Internal unique identifier |
| `clip_id` | Linked clip |
| `sender_id` | Linked sender |
| `source_app` | LINE, Messenger, or other |
| `received_at` | Time added to the inbox |
| `original_text` | Shared message text when available |
| `user_note` | Optional note entered by the user |
| `reply_status` | Needs reply, replied, or no reply needed |
| `reply_text` | Optional drafted/copied response |

### Sender

| Field | Description |
| --- | --- |
| `id` | Internal unique identifier |
| `display_name` | User-defined sender name |
| `last_used_at` | Used to order recent senders |
| `created_at` | Creation time |

## 7. Key Business Rules

1. A clip is unique primarily by `(platform, platform_content_id)` and secondarily by `canonical_url`.
2. One clip can have many share records.
3. Repeated shares from the same sender should still be stored only if they represent a new received event; the UI may collapse them.
4. A clip is completed only when it is watched and every linked share is either replied or marked no reply needed.
5. Failure to retrieve metadata must never prevent the URL from being saved.
6. Private, deleted, expired, region-locked, or login-required links remain valid inbox items even if only the URL can be displayed.

## 8. Non-Functional Requirements

### Performance

- The share receiver should open quickly and allow saving even while metadata loads in the background.
- Saving a URL should not depend on a successful external metadata request.
- The local inbox should remain responsive with at least 10,000 clips.

### Privacy

- The app must not request access to the user's complete LINE or Messenger inbox.
- Store only content explicitly shared into the app.
- Clearly explain what URLs, sender labels, notes, and metadata are stored.
- Avoid uploading private message text unless cloud sync is enabled and disclosed.

### Reliability

- Share handling must be idempotent to prevent accidental duplicate records from repeated Android intents.
- Background metadata failures should retry safely without blocking the inbox.
- Original URLs must be retained even after normalization.

### Offline behavior

- Users should be able to save and organize shared URLs while offline.
- Metadata enrichment can occur later when connectivity returns.

## 9. Out of Scope for MVP

- Reading complete LINE or Messenger inboxes
- Automatically identifying the sender from private chats
- Automatically sending reactions or messages
- Guaranteed deep links to the original conversation/message
- Downloading or permanently hosting copyrighted videos
- Cross-platform video/audio fingerprint matching
- AI transcription, summaries, categories, or reply generation
- Social feed, public profiles, or discovery features
- iOS version
- Desktop/browser extension
- Payments or subscriptions

## 10. Early Technical Validation Spikes

These tests should be completed before building the full UI:

1. From current Android versions of LINE and Messenger, determine exactly what payload is received when sharing:
   - A plain video URL
   - A message containing a URL and text
   - A native attachment or preview
2. Confirm that neither app reliably provides sender/conversation identity; document any exceptions without depending on them.
3. Test whether the app can infer the source package name from the incoming Android share intent.
4. Test URL normalization and redirect expansion for representative Instagram, TikTok, YouTube, and Facebook links.
5. Determine which platforms allow usable preview metadata without login or prohibited scraping.
6. Verify what happens when opening each saved URL: browser, native app, login wall, private post, or deleted post.

The product design must be adjusted based on these results before implementing reply shortcuts or in-app playback.

## 11. MVP Acceptance Criteria

The MVP is complete when a test user can:

1. Share a supported video link from LINE or Messenger into the app.
2. Assign the shared link to a sender with minimal interaction.
3. See the clip in an unwatched inbox even if metadata retrieval fails.
4. Share the same normalized link from another sender without creating a second clip card.
5. Open the original clip and mark it watched.
6. See all people who sent the clip.
7. Independently mark each sender replied or no reply needed.
8. Filter the inbox to show clips that still need attention.
9. Search saved clips by sender or available metadata.
10. Close and reopen the app without losing local data.

## 12. Suggested Delivery Phases

### Phase 0 — Feasibility spike

- Build a minimal Android share receiver.
- Log and inspect payloads from LINE and Messenger.
- Implement canonicalization tests using real shared links.

### Phase 1 — Local MVP

- Share receiver and paste-link input
- Manual sender selection
- Local database
- Canonical URL deduplication
- Inbox and status filters
- Watch and per-sender reply tracking

### Phase 2 — Usability improvements

- Metadata enrichment and thumbnails
- Recent/favorite sender shortcuts
- Batch catch-up mode
- Clipboard response shortcuts
- Sender merge and history

### Phase 3 — Only after user validation

- Account and cloud sync
- iOS version
- Smarter duplicate detection
- AI summaries, transcription, semantic search, or suggested replies

## 13. Product Metrics for Testing

During a small beta, measure:

- Clips saved per active user per week
- Percentage of saved clips later opened
- Duplicate-share rate
- Average number of senders per duplicated clip
- Percentage of share records marked replied/no reply needed
- Median time from receiving to completing a clip
- Save-flow abandonment rate
- Weekly retention after two and four weeks

The most important validation question is whether users repeatedly route received clips into the app. If they do not form that habit, additional AI features will not solve the core product problem.

## 14. Implementation Guidance for Cursor

- Begin with Phase 0; do not scaffold the entire final architecture first.
- Keep platform-specific URL normalization behind testable adapters.
- Separate `Clip` from `ShareRecord`; do not duplicate clips per sender.
- Design saving as local-first and enqueue metadata enrichment separately.
- Do not rely on undocumented LINE or Messenger APIs, UI automation, notification scraping, or accessibility services for MVP.
- Add automated tests for URL parsing, canonicalization, duplicate decisions, and completion-state rules before building advanced UI.
- Record assumptions and feasibility findings in `docs/FEASIBILITY.md` as they are tested.

