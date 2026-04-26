# Phase 1 Scope (Foundation Stabilization)

Status date: 2026-04-24
Roadmap reference: `ROADMAP.md` -> Phase 1

## Goal
Create a stable, repeatable baseline for development and internal beta preparation.

## In Scope (Phase 1)
- CI quality gates for each pull request:
  - Build (`build`)
  - Static checks (`ktlintKotlinScriptCheck` for Gradle Kotlin scripts)
  - Plugin verification (`verifyPlugin` or `runPluginVerifier`)
- Baseline scope alignment for beta:
  - Primary target: Spring AOP workflows in Java projects
  - Support currently implemented annotations/inspections/completion/markers
- Basic release process draft with preflight checks and smoke validation
- Reproducible local build requirements documented
- Full Kotlin source formatting enforcement is deferred to Phase 2+

## Out of Scope (Phase 1)
- New end-user features (tool window, advanced graphing, deep cross-file matching)
- Full AspectJ parity and advanced semantic engine
- Broad UX redesign of icons/tooltips/completion ranking
- Public release and Marketplace rollout

## Explicit Constraints
- Keep plugin compatibility centered on IntelliJ IDEA Community 2024.3 baseline used in `build.gradle.kts`
- Avoid risky refactors; focus on quality gates and process reliability
- Treat missing JDK/toolchain issues as environment setup tasks, not feature work

## Phase 1 Exit Criteria
- PR checks enforce build + static checks + plugin verifier
- No critical plugin loading/registration warnings
- Team can run the same verification steps locally
- Scope boundaries are explicit and shared across the team

