# Phase 1 audit — 2026-11-14

Baseline: main `acb9829daf4720b6a81adaeef7f75def3aad8f00`; [CI passed](https://github.com/Batmancpu/Player/actions/runs/43588156049). Latest published test release: [v0.1.53](https://github.com/Batmancpu/Player/releases/tag/v0.1.53). User confirmed Obtainium update from 51 to 53; offline-library acceptance and all handset performance remain pending.

## Implementation order

1. **Current slice: prevent incomplete provider scans from replacing cached data.** Propagate cursor loading/error status, fail malformed mandatory rows, test rejection after earlier valid results and genuine empty success. No Room schema change.
2. **Lifecycle and one-grant ownership.** Move selection/scan state out of composable-local jobs into a lifecycle owner. Serialize cancel/join, selection, load and save; prevent old loads/scans publishing into a newly selected folder. Test rapid A→B→A, rescan during cached load, rotation, background and Glass Lab navigation.
3. **Provider safety and cancellation.** Add cancellation signals for blocking queries, cancellation checks inside cursor iteration, bounded directory/entry budgets; test cycles, deep trees, huge directories, containment failures, null cursors and provider death. Revalidate actual root access, not merely a persisted grant record. Reconcile/release stale grants on startup and selection; verify no persisted write grant remains. Default-deny OEM authorities until evidence.
4. **Persistence and parsing.** Add Room integration tests for atomic successful/empty reconciliation, rollback on failure/cancellation, tree isolation, reopen and update continuity. Export v1 schema before future migrations (currently exportSchema=false). Test Unicode, title-leading years, multi-episode ambiguity, noise and season grouping. Cache size/date and scan status are roadmap gaps.
5. **Useful offline UI and release gate.** Distinguish never-scanned/loading/empty/success/cancelled/revoked/provider-error states; restore permission with actionable reconnect. Implement real series grouping, avoid repeated parsing/filtering on composition, verify TalkBack/large text/insets. Run full CI and merged-manifest audit; publish one signed Phase 1 APK with checklist; wait for OPPO acceptance.

## Confirmed source findings and missing tests

- **H-008 (first fix):** adapter ignored EXTRA_LOADING/EXTRA_ERROR and skipped malformed rows; partial results could reach removeStale. New TreeListing status fails before reconciliation. JVM regression coverage is not Android cursor/Room integration coverage.
- **H-009 (open, source-confirmed race):** FolderSetupScreen independently loads cached rows and launches scans. A slow load can overwrite newer scan results. Folder changes cancel without joining; old job finalizers share isScanning and old work can race switching. Not reproduced on OPPO. Add deterministic lifecycle tests before fixing; no real media required.
- **H-010 (open, source-confirmed gap):** restoring a grant checks permission records, not root availability; resolving a name swallows errors. Revocation while active becomes generic scan error. Releasing the prior grant ignores failures; process death between acquisition and preference update can leave additional persisted grants. Test with a fake grant store/provider.
- **H-011 (open, source-confirmed gap):** scanner queue is iterative/cycle-deduplicated but unbounded; list materializes a full cursor and blocking query has no cancellation signal. Existing cancellation test covers a cooperative suspend callback only.
- Parser has only a small corpus; multi-episode files may be mislabeled or only partially interpreted. Movies/episodes are divided into headings, not grouped under series/seasons.
- No androidTest sources, Room reconciliation tests, migration fixtures, provider cursor tests, UI lifecycle tests, merged-manifest CI assertion or real-device scan evidence. Empty successful scans show no explicit completion state.
- Privacy source inspection: manifest has no INTERNET/broad media/storage permission and allowBackup=false; only read grants are requested, no source-write API or video-byte reads in scanner. Merged manifest, device network observations and actual source immutability still require verification.
- CI currently runs unit tests/debug build, then signed release build on main. Release minification is off; do not treat those builds as measured optimized performance. Signing setup and application identity must stay unchanged.

All issue IDs above use synthetic scenarios; no user media identifiers/logs are recorded.

## Online research and later-phase backlog

Read on 2026-11-14; findings are design inputs, not newly approved dependencies or copied source.

- [Apple HIG materials](https://developer.apple.com/design/human-interface-guidelines/materials.md): distinguish content from functional chrome; use effects sparingly and preserve contrast/accessibility. Translate into original Compose surfaces with stable hit targets and opaque equivalents.
- [Apple Liquid Glass overview](https://developer.apple.com/documentation/technologyoverviews/liquid-glass.md) and [WWDC interface examples](https://developer.apple.com/videos/play/wwdc2025/219/): retain edge-to-edge artwork and purposeful controls. Do not redistribute examples, SF fonts or glyphs. Production implementation stays in Phase 6.
- [DocumentsContract](https://developer.android.com/reference/android/provider/DocumentsContract): EXTRA_LOADING signals unfinished directory results; EXTRA_ERROR signals provider failure. This directly motivates H-008. Retry is explicit; no automatic busy polling.
- [Now in Android](https://github.com/android/nowinandroid): architecture/lifecycle reference. [Media3](https://github.com/androidx/media): first local-playback candidate. [Coil](https://github.com/coil-kt/coil): artwork candidate. [Haze](https://github.com/chrisbanes/haze): existing pinned 2.1.0 lab/rendering candidate. Preliminary repository review only; exact revision/license/NOTICE/dependency/security/activity/permission/APK audit must be recorded before adopting or adapting any new code. No upstream code copied in this slice.
- Prefer Room/Flow, standard Compose controls and Android SAF for Phase 1. Do not add a parser/network SDK merely to normalize names. Later TMDB DTOs must receive normalized fields only.
- Later benchmarks: profileable optimized build, Macrobenchmark/Perfetto 60-Hz baseline and Glass on/off, optional 120-Hz, 100/500/1000 synthetic entries, memory/thermal and ten-minute playback/slider sessions. No handset benchmark has run.

## Next command and limits

PR gate: `gradle --no-daemon --stacktrace :app:testDebugUnitTest :app:assembleDebug` on GitHub Actions; local cloud workspace lacks Gradle/Android SDK. Inspect logs on any failure, merge only green changes. Then implement item 2 with deterministic coroutine tests. Do not ask user to configure signing again or install a debug build.

## Candidate provenance checked during the audit

| Repository | Inspected revision | Latest default-branch commit date | Root license |
|---|---|---|---|
| android/nowinandroid | [2982e63c](https://github.com/android/nowinandroid/commit/2982e63cb691ad49cd009aab43dc206fee6699c2) | 2026-10-20 | [Apache-2.0](https://github.com/android/nowinandroid/blob/2982e63cb691ad49cd009aab43dc206fee6699c2/LICENSE) |
| androidx/media | [8c6678b6](https://github.com/androidx/media/commit/8c6678b657ede1e7883fc164ef73ed483c7796c3) | 2026-09-08 | [Apache-2.0](https://github.com/androidx/media/blob/8c6678b657ede1e7883fc164ef73ed483c7796c3/LICENSE) |
| coil-kt/coil | [a6cc8e3e](https://github.com/coil-kt/coil/commit/a6cc8e3e96ec79f71cea1b34c52df140126e067f) | 2026-11-14 | [Apache-2.0](https://github.com/coil-kt/coil/blob/a6cc8e3e96ec79f71cea1b34c52df140126e067f/LICENSE.txt) |
| chrisbanes/haze | [7b8d8310](https://github.com/chrisbanes/haze/commit/7b8d8310aed65339a43222244afcb60213cd1559) | 2026-11-14 | [Apache-2.0](https://github.com/chrisbanes/haze/blob/7b8d8310aed65339a43222244afcb60213cd1559/LICENSE) |

All four repositories were non-archived at inspection. Dates establish recent default-branch activity, not a security endorsement or release compatibility. Preserve license/attribution and applicable NOTICE on any eventual redistribution; inspect per-file licenses and transitive dependencies first. No candidate was newly integrated. Existing Haze 2.1.0 adoption predates this audit. Module-specific network/permission/security, APK-size and handset performance audits remain required before further adoption; especially do not import upstream apps' networking/configuration wholesale.



## First slice verification

[PR #1](https://github.com/Batmancpu/Player/pull/1) passed unit/debug CI 43612121975 and merged as 79ee09d. [Main CI #61](https://github.com/Batmancpu/Player/actions/runs/43612318514) also passed unit/debug, signed release assembly and permanent certificate validation. No new Release was published; OPPO cursor/Room behavior and performance remain unverified. Next coding task is H-009 lifecycle ordering.

## H-009 slice — 2026-11-14
Retained LibraryViewModel now owns a pure serialized LibrarySession with stale-operation guards. Scan waits for restoration; changing trees clears visible rows immediately and waits for obsolete work before activating the new Room tree. Picker-local cache deletion was removed to avoid A→B→A deletion races. Six JVM cases cover ordering/cancellation/error/scope ownership. PR CI pending at authoring; actual Android recreation/Room/grant behavior remains unverified. Next: H-010 root/grant recovery.

H-009 verification completed: [PR #2](https://github.com/Batmancpu/Player/pull/2) merged as 10dab08 after unit/debug CI 43613459732. [Main CI #65](https://github.com/Batmancpu/Player/actions/runs/43613651152) passed tests/build/signing/certificate pin. Android lifecycle/Room/provider behavior remains unverified. Next: H-010.
