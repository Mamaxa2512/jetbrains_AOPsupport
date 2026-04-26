# AOP Plugin Roadmap

Current stage: Prototype / Early MVP
Target: Internal beta in ~12 weeks

## Milestones

### Phase 1 - Foundation Stabilization (P0, Weeks 1-2)
Goals:
- Lock beta scope (Spring AOP first, AspectJ partial/full decision)
- Make build and verification deterministic
- Define quality gates for each pull request

Deliverables:
- Scope document with in/out boundaries
- CI pipeline: build, plugin verifier, static checks
- Basic release checklist draft

Acceptance criteria:
- `build` and verifier pass on every PR
- No critical plugin loading/registration warnings
- Team can produce reproducible local build

### Phase 2 - Core Analysis Hardening (P0, Weeks 3-5)
Goals:
- Increase correctness of inspections
- Reduce false positives and false negatives in core rules

Deliverables:
- Improved `PointcutSyntaxInspection` (better parser rules and diagnostics)
- Improved `AspectNotBeanInspection` (more Spring bean cases)
- Clear quick-fix behavior guidelines

Acceptance criteria:
- Handles representative invalid/valid pointcut samples
- Correctly flags non-bean `@Aspect` classes in common Spring setups
- Quick fixes are safe and idempotent

### Phase 3 - Editor UX Improvements (P1, Weeks 6-8)
Goals:
- Make day-to-day editing smoother and more discoverable

Deliverables:
- Context-aware completions in pointcut strings
- Better gutter navigation targets and tooltips
- Refined AOP annotation highlighting behavior

Acceptance criteria:
- Completion suggestions are relevant to context
- Navigation markers are stable and not noisy
- Highlighting is consistent across supported annotations

### Phase 4 - Test Coverage and Compatibility (P1, Weeks 9-10)
Goals:
- Add regression protection before beta rollout

Deliverables:
- Automated tests for inspections, completion, and line markers
- Test fixtures for Java/Spring AOP sample projects
- Compatibility matrix for target IDE versions

Acceptance criteria:
- Core features have automated regression tests
- Tests pass in CI for selected IDE baselines
- Known limitations documented

### Phase 5 - Beta Release Preparation (P0, Weeks 11-12)
Goals:
- Prepare a safe internal beta and feedback loop

Deliverables:
- Internal beta build
- Changelog and release notes
- Issue templates and triage flow

Acceptance criteria:
- Beta install/upgrade path validated
- No blocker issues in smoke testing
- Feedback channel and response SLA defined

### Phase 6 - Post-Beta Expansion (P2, Backlog)
Goals:
- Plan advanced functionality after core stability

Candidates:
- Tool window for aspect/pointcut overview
- Cross-file pointcut usage graph
- Navigation between advice and matched methods (deeper analysis)

Acceptance criteria:
- Epics prioritized by user impact and implementation cost
- Dependencies and risks documented

## Priority Legend
- P0: Required before public release
- P1: High value, should land before/around beta
- P2: Nice-to-have after beta feedback

## Immediate Next Sprint (Suggested)
1. Add CI + plugin verifier as mandatory checks
2. Write first regression tests for two existing inspections
3. Expand pointcut syntax validation with representative fixtures
4. Define beta scope (Spring AOP only vs broader AspectJ support) - completed (`docs/beta-scope.md`)

## Phase 1 Implementation Status (2026-04-25)
- Status: completed
- Scope document added: `docs/phase-1-scope.md`
- Release checklist draft added: `docs/release-checklist.md`
- CI quality gates added: `.github/workflows/ci.yml`
- Build/static/verifier gate configuration added in: `build.gradle.kts`
- Toolchain resolver enabled in: `settings.gradle.kts`
- Plugin verifier descriptor fixes applied in: `src/main/resources/META-INF/plugin.xml`

## Phase 2 Implementation Status (2026-04-26)
- Status: in progress
- Hardened pointcut validation with richer diagnostics in: `src/main/kotlin/org/example/aop/inspection/AopInspectionRules.kt`
- Added extra malformed-expression diagnostics (empty negation, consecutive operators, missing operator between clauses) in: `src/main/kotlin/org/example/aop/inspection/AopInspectionRules.kt`
- Extended Spring bean detection rules (including meta-annotations) in: `src/main/kotlin/org/example/aop/inspection/AopInspectionRules.kt`
- Updated inspection to validate both `value` and `pointcut` attributes in: `src/main/kotlin/org/example/aop/inspection/PointcutSyntaxInspection.kt`
- Made `@Component` quick fix idempotent and safer in: `src/main/kotlin/org/example/aop/inspection/AspectNotBeanInspection.kt`
- Added regression tests for rule validation in: `src/test/kotlin/org/example/aop/inspection/AopInspectionRulesTest.kt`
- Added quick-fix safety contract: `docs/phase-2-quick-fix-guidelines.md`
- Beta scope decision documented: `docs/beta-scope.md`
- Shared annotation/designator scope lists reused in completion/annotator/line markers
- Updated inspection docs to match implemented diagnostics and fix behavior
- Fixture-based inspection tests are deferred until the IntelliJ test framework setup is stabilized
- Current test suite is green via `./gradlew test`

## Phase 3 Implementation Status (2026-04-26)
- Status: started
- Completion contributor made more context-aware for supported pointcut annotation attributes in: `src/main/kotlin/org/example/aop/completion/AopCompletionContributor.kt`
- Added unit tests for completion context/prefix extraction helpers in: `src/test/kotlin/org/example/aop/completion/AopCompletionContextTest.kt`

