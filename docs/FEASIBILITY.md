# Feasibility findings

Phase 0 evidence log. Do not invent device-dependent results.

## Environment

| Item | Value |
| --- | --- |
| App package | `com.reelshelf.app` |
| Spike build | `assembleDebug` / `0.1.0-spike` |
| Dev machine SDK | Android SDK Platform 35 (command-line tools) |
| Connected devices (agent session) | None (`adb devices` empty; no emulator image installed) |

## Share payload matrix (T0.3)

Fill after installing the debug APK and sharing from each source app into **ReelShelf**.

Record from the diagnostic screen: Action, MIME type, Text present/length, URL count hint, Calling package, Referring package, Referrer URI, Extras keys, Text preview.

| Source app | App version | Device / Android | Case | Payload shape | Sender ID present? | Conversation ID present? | Source package available? | Notes |
| --- | --- | --- | --- | --- | --- | --- | --- | --- |
| LINE | | | Plain video URL | **Blocked — user test required** | | | | |
| LINE | | | Text containing one URL | **Blocked — user test required** | | | | |
| LINE | | | Native preview / attachment | **Blocked — user test required** | | | | |
| LINE | | | Text with multiple URLs | **Blocked — user test required** | | | | |
| Messenger | | | Plain video URL | **Blocked — user test required** | | | | |
| Messenger | | | Text containing one URL | **Blocked — user test required** | | | | |
| Messenger | | | Native preview / attachment | **Blocked — user test required** | | | | |
| Messenger | | | Text with multiple URLs | **Blocked — user test required** | | | | |

### How to run the user tests

1. Build/install: `.\gradlew.bat installDebug` (device connected).
2. In LINE or Messenger, long-press a message/link → Share → **ReelShelf**.
3. Copy the diagnostic fields into the matrix above.
4. Also try Chrome or any notes app sharing a plain URL to confirm the receiver works before LINE/Messenger tests.

### Assumptions until proven otherwise

- Sender/conversation identity will not be present in share intents.
- Source package may appear via `callingPackage` / referrer, but must not be required for saving.
- Native attachment shares may arrive as non-`text/plain` or without `EXTRA_TEXT`; MVP may need additional MIME filters after these tests.

## Short-link expansion (T0.6)

| Case | Status | Notes |
| --- | --- | --- |
| Bounded redirect client + expander | Implemented | `ShortLinkExpander` + `HttpRedirectClient`; max redirects / timeout |
| Fake-client unit tests | Passed | Success, timeout, login wall, too many redirects, skip non-short hosts |
| Live TikTok/YouTube/Instagram/Facebook short links | **Blocked — requires network + real links** | User/agent with network may run later; record failures needing login/cookies here |

### Links that may need login/cookies (to fill after live tests)

| Platform | Example short host | Live result | Can expand without login? |
| --- | --- | --- | --- |
| TikTok | `vt.tiktok.com` / `vm.tiktok.com` | Pending | |
| YouTube | `youtu.be` | Pending | |
| Instagram | `instagr.am` | Pending | |
| Facebook | `fb.watch` | Pending | |

Saving remains independent of expansion success.

## Metadata capability matrix (T0.7)

| Platform | Candidate mechanism | Agent status | Title | Creator | Thumbnail | Auth / ToS risk | MVP recommendation |
| --- | --- | --- | --- | --- | --- | --- | --- |
| YouTube | oEmbed / Open Graph | **Blocked — live fetch not run** | | | | Avoid scraping logged-in HTML | Prefer official oEmbed if reliable |
| TikTok | oEmbed / OG | **Blocked — live fetch not run** | | | | Often blocks bots | Save URL-only if blocked |
| Instagram | OG / embed | **Blocked — live fetch not run** | | | | Login walls common | Do not scrape; URL-only OK |
| Facebook | OG | **Blocked — live fetch not run** | | | | Frequently login-gated | Optional best-effort only |

Code status: `MetadataFetcher` / `MetadataResult` support partial data + retryable/permanent/login failures. No prohibited scrapers added.

## Opening-behavior matrix (T0.8)

| Platform | Native app installed? | Expected handler | Live result | Notes |
| --- | --- | --- | --- | --- |
| YouTube | yes/no | YouTube app or browser | **Blocked — no device** | |
| TikTok | yes/no | TikTok app or browser | **Blocked — no device** | |
| Instagram | yes/no | Instagram app or browser | **Blocked — no device** | |
| Facebook | yes/no | Facebook app or browser | **Blocked — no device** | |
| Private / deleted / login-required | n/a | Browser error or app wall | **Blocked — needs real links** | Inbox item must remain valid with URL only |

Code status: `ClipOpener` returns `Started` / `NoHandler` / `InvalidUrl` / `Failed`.

## Decision gate (T0.9)

**Status: approved for Phase 1** (product owner, 2026-08-09).

Device-dependent LINE/Messenger and live metadata rows remain open for later filling; Phase 1 proceeded with the proposed defaults:

- Manual sender selection
- Source app from callingPackage/referrer when present
- Best-effort metadata only (placeholders until allow-listed)
- External open via intents
- No conversation deep links

### Confirmed in code (no device required)

- Share receiver activity is registered for `ACTION_SEND` / `text/plain`.
- Release logging omits shared message bodies.
- URL extraction and platform canonicalization unit tests pass for YouTube, TikTok, Instagram, Facebook, and fallback.
- Short-link expansion failures do not block producing a usable original URL.
- Metadata and open helpers tolerate partial / failed outcomes.

### Blocked pending user / device

1. Real LINE and Messenger payload shapes (T0.3).
2. Whether `callingPackage` / referrer reliably identifies source app.
3. Live short-link expansion and metadata reliability per platform (T0.6–T0.7).
4. Opening behavior with/without native apps (T0.8).

### Product decisions required before Phase 1

| Decision | Proposed default until tests complete | Approval |
| --- | --- | --- |
| Sender selection | Remain **manual** (create/recent senders) | Pending |
| Source-app inference | Prefer `callingPackage` / referrer when present; else manual optional field | Pending |
| Metadata per platform | Best-effort only after allow-list from live tests; never block save | Pending |
| In-app playback | Defer; open externally via `ClipOpener` for MVP | Pending |
| Reply shortcuts | Defer deep-link claims until payload matrix proves conversation IDs (expected: none) | Pending |

**Gate status: STOPPED for product approval.** Do not start Phase 1 until the blocked device tests are filled in or explicitly waived.
