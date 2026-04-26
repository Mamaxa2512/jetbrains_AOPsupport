# Release Checklist (Draft)

Status date: 2026-04-26
Purpose: Internal beta readiness checklist.

## 1) Pre-Release Validation
- [x] Version and changelog prepared (`CHANGELOG.md`, version set to `1.0.0-beta.1`)
- [x] Plugin metadata reviewed (`src/main/resources/META-INF/plugin.xml`)
- [x] Inspection descriptions present and up to date
- [x] No TODO/FIXME blockers for release scope

## 2) CI/Quality Gates
- [x] `build` passes on CI
- [x] `ktlintKotlinScriptCheck` passes on CI
- [x] Plugin verifier task passes on CI (`verifyPlugin` or `runPluginVerifier`)
- [x] No critical warnings in verifier output

## 3) Local Smoke Test
- [ ] Plugin starts in sandbox IDE (`./gradlew runIde`)
- [ ] AOP annotation highlighting works
- [ ] Line markers appear for aspects/advice methods
- [ ] Pointcut completion appears in supported annotations
- [ ] Inspections report expected diagnostics on sample project
- [ ] See detailed checklist: `docs/smoke-test-guide.md`

## 4) Packaging and Notes
- [ ] Build artifact generated successfully (`./gradlew buildPlugin`)
- [x] Release notes include known limitations (`docs/release-notes-beta-1.md`)
- [x] Upgrade notes written (first release, no upgrade path yet)

## 5) Post-Release Ops
- [x] Feedback channel confirmed (issue templates in `.github/ISSUE_TEMPLATE/`)
- [x] Issue templates ready (bug report, feature request, false positive)
- [x] Triage owner and response SLA confirmed (`docs/issue-triage-flow.md`)

