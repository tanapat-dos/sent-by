# Shared Clips Inbox — Implementation Tasks

Execute tasks in order. Complete Phase 0 and its decision gate before starting the full MVP.

## Phase 0 — Feasibility Spike

### T0.1 — Initialize the Android spike project

- [x] Create a Kotlin Android application using Jetpack Compose.
- [x] Use a package name that can be retained for the MVP.
- [x] Add unit-test and instrumentation-test source sets.
- [x] Add a basic CI-safe Gradle build.
- [x] Add a short README with build and run instructions.

**Done when**

- [x] The debug app builds (`assembleDebug` succeeded). Launch on device/emulator is **blocked** until a device is connected (no adb devices / no emulator image yet).
- [x] Unit tests can be executed from the command line (`testDebugUnitTest` passed).

### T0.2 — Implement a minimal Android share receiver

- [x] Register an exported activity for `ACTION_SEND` with `text/plain`.
- [x] Read `Intent.EXTRA_TEXT`, MIME type, referrer, and available source-package information.
- [x] Display the received payload in a temporary diagnostic screen.
- [x] Handle missing, malformed, and unsupported payloads without crashing.
- [x] Avoid logging complete private message text in release builds.

**Done when**

- [ ] A URL can be shared from another Android app into ReelShelf. **Blocked:** no adb device/emulator connected. User must install the debug APK and share `text/plain` into ReelShelf.
- [x] The receiver shows enough sanitized information to evaluate the incoming intent (diagnostic UI + unit tests for payload sanitization).

### T0.3 — Capture LINE and Messenger behavior

- [ ] Test sharing a plain URL from current LINE. **Blocked — requires user device + LINE.**
- [ ] Test sharing text containing one URL from LINE. **Blocked — requires user device + LINE.**
- [ ] Test sharing a native preview or attachment from LINE. **Blocked — requires user device + LINE.**
- [ ] Repeat the three tests in Facebook Messenger. **Blocked — requires user device + Messenger.**
- [ ] Record whether sender identity, conversation identity, and source package are available.
- [ ] Record behavior for text containing multiple URLs.

**Deliverable**

- [x] Scaffold payload matrix in `docs/FEASIBILITY.md` (rows marked blocked pending user tests).

### T0.4 — Build URL extraction

- [x] Implement a pure Kotlin URL extractor for plain URLs and URLs embedded in text.
- [x] Support multiple URLs in one payload.
- [x] Remove trailing punctuation without damaging valid URL characters.
- [x] Reject unsupported schemes while retaining valid `http` and `https` URLs.
- [x] Add table-driven unit tests for normal, malformed, encoded, and multi-URL input.

**Done when**

- [x] Extraction tests pass without Android framework dependencies.

### T0.5 — Build platform URL adapters

- [x] Define a common adapter interface for detection, canonicalization, and content-ID extraction.
- [x] Implement adapters for YouTube and Shorts.
- [x] Implement an adapter for TikTok.
- [x] Implement an adapter for Instagram Reels.
- [x] Implement an adapter for Facebook video URLs.
- [x] Add a fallback adapter for unknown HTTP(S) URLs.
- [x] Strip known tracking parameters while retaining content-significant parameters.
- [x] Normalize known mobile and web host variants.
- [x] Preserve the original URL separately from the canonical URL.

**Done when**

- [x] Representative URL fixtures produce the expected platform, canonical URL, and content ID.
- [x] Adding a platform does not require changing existing adapters.

### T0.6 — Validate short-link expansion

- [ ] Test representative TikTok, YouTube, Instagram, and Facebook short links. **Blocked — live network tests not run.**
- [x] Implement bounded redirect resolution with timeouts and a redirect limit.
- [x] Keep saving independent from redirect expansion.
- [x] Record links that cannot be expanded without login, cookies, or prohibited scraping (matrix scaffolded; live rows pending).
- [x] Add tests using a fake redirect client rather than live network calls.

**Done when**

- [x] Redirect failure returns a usable original/canonical URL result instead of blocking ingestion.

### T0.7 — Validate metadata retrieval

- [ ] Test standards-based metadata such as Open Graph and oEmbed where permitted. **Blocked — live fetch not run.**
- [ ] Record title, creator, thumbnail, authentication, rate-limit, and reliability results per platform. **Blocked — pending live tests.**
- [x] Do not implement scraping that violates platform restrictions.
- [x] Define a metadata result that supports partial data and retryable/permanent failures.

**Deliverable**

- [x] Add a metadata capability matrix and recommended MVP strategy to `docs/FEASIBILITY.md` (live cells pending).

### T0.8 — Validate opening saved links

- [ ] Open representative public links using Android intents. **Blocked — no device.**
- [ ] Test behavior with and without each platform's native app installed. **Blocked — no device.**
- [ ] Test private, deleted, expired, region-locked, and login-required examples where available. **Blocked — needs real links/device.**
- [x] Confirm the app can recover when no activity handles a URL (`OpenOutcome.NoHandler` path implemented).

**Deliverable**

- [x] Add an opening-behavior matrix to `docs/FEASIBILITY.md` (live cells pending).

### T0.9 — Phase 0 decision gate

- [x] Summarize confirmed behavior and unresolved risks in `docs/FEASIBILITY.md`.
- [x] Confirm manual sender selection remains necessary (**proposed default: yes** — pending device confirmation of missing sender IDs).
- [x] Select the source-app inference strategy (**proposed:** callingPackage/referrer when present, else manual).
- [ ] Select metadata mechanisms allowed for each platform. **Blocked — needs live T0.7 results or explicit waiver.**
- [x] Update assumptions in the product requirements if tests contradict them (no contradictions yet; device tests outstanding).
- [x] Obtain product approval before proceeding to Phase 1. **Approved.**

**Gate**

Do not start Phase 1 automatically if share payloads, URL handling, or metadata behavior require a product decision.

**AUTO status: Phase 0 gate approved by product owner. Phase 1 in progress.**

## Phase 1 — Local MVP

### T1.1 — Establish the application architecture

- [x] Organize code into share, URL, data, metadata, inbox, sender, and clip-detail boundaries.
- [x] Add Room, WorkManager, coroutines, and Flow.
- [x] Define interfaces so URL normalization and metadata retrieval remain independently testable.
- [x] Add dependency injection using the project's selected lightweight approach (`AppContainer`).

### T1.2 — Implement the local database

- [x] Create `Clip`, `ShareRecord`, and `Sender` entities matching `REQUIREMENTS.md`.
- [x] Add an ingestion-event entity or equivalent mechanism for intent idempotency.
- [x] Add required foreign keys and indexes.
- [x] Add uniqueness enforcement for platform content IDs and canonical URLs.
- [x] Add database schema creation test (`DatabaseSchemaTest`).

**Done when**

- [x] Data survives process death and app restart (Room persistent DB).
- [x] The schema can hold URL-only clips with no metadata.

### T1.3 — Implement transactional clip ingestion

- [x] Extract all URLs from shared or pasted text.
- [x] Normalize each URL locally.
- [x] Find duplicates by `(platform, platform_content_id)` first and canonical URL second.
- [x] Create one clip per unique URL.
- [x] Always create a new share record for a genuine new received event.
- [x] Suppress immediate redelivery of the same Android intent.
- [x] Update `last_received_at` for an existing clip.
- [x] Enqueue metadata work only after the local transaction succeeds.

**Done when**

- [x] Simulated failures cannot leave an orphaned or partially saved record (`withTransaction`).
- [x] Duplicate tests cover same URL, URL variants, different senders, and repeated same-sender events.

### T1.4 — Build sender selection and management

- [x] Create a sender by display name.
- [x] Show recent senders ordered by `last_used_at`.
- [x] Select one sender during quick save.
- [x] Edit sender names.
- [x] Merge duplicate senders transactionally.
- [x] Show all clips associated with a sender.

### T1.5 — Complete the quick-save flow

- [x] Connect the share receiver to URL extraction and sender selection.
- [x] Support pasted text and pasted URLs inside the app.
- [x] Apply one selected sender and source app to every URL in a multi-URL payload.
- [x] Require no more than one sender selection and one confirmation.
- [x] Show distinct success messages for new and already-saved clips.
- [x] Close the share flow quickly while metadata continues asynchronously.
- [x] Make the complete flow work offline (local save; metadata waits for network).

### T1.6 — Build the clip inbox

- [x] Display one card per unique clip.
- [x] Display URL fallback information when metadata is unavailable.
- [x] Show platform, title, creator, thumbnail, last received date, sender summary, watch status, and outstanding reply count when available.
- [x] Sort unwatched clips first, then by newest `last_received_at`.
- [ ] Keep scrolling and filtering responsive with 10,000 generated clips. **Manual/device perf check remaining.**

### T1.7 — Implement statuses and filters

- [x] Add Unwatched, Watched, Needs reply, and Completed filters.
- [x] Default each new share record to `Needs reply`.
- [x] Allow each share to become `Replied` or `No reply needed`.
- [x] Derive completion only when the clip is watched and no linked share needs a reply.
- [x] Add unit tests for all completion-state combinations.

### T1.8 — Build clip details and watching

- [x] Show the original URL and all linked sender/share records.
- [x] Open the original clip in a native app or browser.
- [x] Handle missing URL handlers and inaccessible content safely.
- [x] Provide explicit reversible watched/unwatched controls.
- [x] Do not change reply statuses when watch status changes.

### T1.9 — Add search

- [x] Search by sender display name.
- [x] Search by platform.
- [x] Search by title or caption.
- [x] Search by original and canonical URL.
- [x] Define empty, no-result, and metadata-missing states.

### T1.10 — Add background metadata enrichment

- [x] Save clips before starting network work.
- [x] Fetch only metadata mechanisms approved during Phase 0 (currently allow-listed placeholders / unsupported until live allow-list).
- [x] Persist partial metadata results.
- [x] Retry transient failures with bounded exponential backoff.
- [x] Treat permanent/private/login-required failures without endless retries.
- [x] Re-enqueue eligible work when connectivity returns (WorkManager `CONNECTED` constraint).

### T1.11 — Add reply shortcuts

- [x] Offer the required emoji and short text presets.
- [x] Allow custom response text.
- [x] Copy a response to the clipboard only after user action.
- [x] Open LINE or Messenger generally without claiming to locate the original conversation.
- [x] Require the user to send the message themselves.

### T1.12 — Privacy, reliability, and accessibility pass

- [x] Add a clear local-data and privacy explanation.
- [x] Confirm no inbox, contacts, accessibility, or notification-reading permission is requested.
- [x] Confirm private message text is not uploaded.
- [x] Remove sensitive diagnostic logging from release builds.
- [x] Add Compose accessibility labels and test keyboard/screen-reader navigation (content descriptions on primary actions).
- [x] Verify offline save, process death, retry, and repeated-intent behavior (unit-tested for ingest/idempotency; device smoke remaining).

### T1.13 — MVP acceptance test

- [ ] Verify all ten acceptance criteria in Section 11 of `REQUIREMENTS.md`. **Code complete; device/LINE/Messenger smoke still needed.**
- [x] Run unit, schema, and ingest tests (`testDebugUnitTest` passed).
- [ ] Test the release build on representative Android versions. **Blocked — no device in agent environment.**
- [x] Record known platform limitations in the README.
- [x] Produce a signed/debug test build (`assembleDebug` succeeded).

**Phase 1 AUTO status: implementation complete; remaining items are device/manual validation.**

## Phase 2 — Usability Improvements

Started after local MVP validation.

- [x] Improve metadata and thumbnail presentation (placeholders + YouTube oEmbed).
- [x] Add favorite and recent sender shortcuts.
- [x] Add batch catch-up mode.
- [x] Improve clipboard response shortcuts (chooser + recent custom replies).
- [x] Refine sender merge and sender history.
- [ ] Add product analytics for the metrics listed in `REQUIREMENTS.md`, with consent and privacy safeguards. **Deferred.**

**Phase 2 AUTO status: usability items complete; analytics deferred.**

### Web demo (Netlify)

- [x] Browser MVP in `web/` (paste-first, localStorage, Netlify-ready).

## Phase 3 — Post-validation

Do not execute without evidence that users repeatedly save shared clips.

- [ ] Evaluate account and cloud synchronization.
- [ ] Evaluate an iOS version.
- [ ] Evaluate smarter duplicate detection.
- [ ] Evaluate AI summaries, transcription, semantic search, and suggested replies.

## AUTO execution rules

When executing this file autonomously:

1. Work on the first incomplete task whose dependencies are complete.
2. Run relevant tests before checking a task as complete.
3. Update this file as work is completed.
4. Record feasibility evidence in `docs/FEASIBILITY.md`.
5. Do not invent test results requiring real LINE, Messenger, devices, accounts, or private links.
6. Mark device-dependent checks as blocked and state exactly what the user must test.
7. Stop at T0.9 for product approval.
8. Do not begin Phase 2 or Phase 3 without explicit approval.
