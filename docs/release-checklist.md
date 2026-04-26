# Release Checklist (Draft)

Status date: 2026-04-24
Purpose: Internal beta readiness checklist.

## 1) Pre-Release Validation
- [ ] Version and changelog prepared
- [ ] Plugin metadata reviewed (`src/main/resources/META-INF/plugin.xml`)
- [ ] Inspection descriptions present and up to date
- [ ] No TODO/FIXME blockers for release scope

## 2) CI/Quality Gates
- [ ] `build` passes on CI
- [ ] `ktlintKotlinScriptCheck` passes on CI
- [ ] Plugin verifier task passes on CI (`verifyPlugin` or `runPluginVerifier`)
- [ ] No critical warnings in verifier output

## 3) Local Smoke Test
- [ ] Plugin starts in sandbox IDE
- [ ] AOP annotation highlighting works
- [ ] Line markers appear for aspects/advice methods
- [ ] Pointcut completion appears in supported annotations
- [ ] Inspections report expected diagnostics on sample project

## 4) Packaging and Notes
- [ ] Build artifact generated successfully
- [ ] Release notes include known limitations
- [ ] Upgrade notes written (if behavior changed)

## 5) Post-Release Ops
- [ ] Feedback channel confirmed
- [ ] Issue templates ready
- [ ] Triage owner and response SLA confirmed

