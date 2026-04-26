# Issue Triage Flow

## Purpose
This document defines the process for triaging and responding to issues reported during the beta phase.

## Triage Owner
- **Primary:** [Team Lead / Maintainer Name]
- **Backup:** [Secondary Contact]

## Response SLA

| Priority | Initial Response | Resolution Target |
|---|---|---|
| **Critical** (crash, data loss, plugin won't load) | 1 business day | 3 business days |
| **High** (core feature broken, major false positive) | 2 business days | 1 week |
| **Medium** (minor bug, enhancement) | 1 week | Best effort |
| **Low** (cosmetic, nice-to-have) | Best effort | Backlog |

## Triage Process

### 1. Initial Review (within 24 hours)
- [ ] Verify issue template is filled out
- [ ] Check for duplicates
- [ ] Assign priority label
- [ ] Assign to milestone (if applicable)

### 2. Priority Assignment

**Critical**
- Plugin crashes or fails to load
- Data loss or corruption
- Security vulnerability

**High**
- Core feature completely broken (inspections, completion, navigation)
- Major false positive affecting many users
- Performance regression (>2s delay in common operations)

**Medium**
- Minor bug in non-critical feature
- False positive in edge case
- Enhancement request with clear use case

**Low**
- Cosmetic issue
- Enhancement without strong justification
- Documentation improvement

### 3. Investigation
- [ ] Reproduce the issue locally
- [ ] Identify root cause
- [ ] Estimate effort (S/M/L/XL)
- [ ] Add `needs-reproduction` label if cannot reproduce

### 4. Resolution
- [ ] Implement fix or feature
- [ ] Add regression test
- [ ] Update CHANGELOG.md
- [ ] Link PR to issue

### 5. Verification
- [ ] Verify fix in sandbox IDE
- [ ] Request reporter to verify (if possible)
- [ ] Close issue with resolution comment

## Labels

### Priority
- `priority: critical`
- `priority: high`
- `priority: medium`
- `priority: low`

### Type
- `bug`
- `enhancement`
- `inspection` (false positive/negative)
- `documentation`
- `question`

### Status
- `needs-reproduction`
- `needs-investigation`
- `in-progress`
- `blocked`
- `wontfix`
- `duplicate`

### Component
- `component: inspection`
- `component: completion`
- `component: navigation`
- `component: highlighting`

## Escalation

If an issue requires escalation:
1. Add `escalated` label
2. Notify team lead immediately
3. Document reason for escalation in issue comment

## Beta Feedback Collection

During beta, track:
- Most common issues (create tracking issue)
- Feature requests by frequency
- Performance feedback
- Usability pain points

Review weekly and adjust roadmap accordingly.

## Post-Beta

After beta phase:
- Review all open issues
- Prioritize for 1.0.0 release
- Close issues marked `wontfix` or `duplicate`
- Update roadmap based on feedback
