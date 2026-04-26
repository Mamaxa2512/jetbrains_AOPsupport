# Beta Scope Decision

Status date: 2026-04-26
Decision owner: Team
Roadmap reference: `ROADMAP.md` -> Immediate Next Sprint item 4

## Decision
For internal beta, the plugin scope is:
- Spring AOP workflows in Java projects (primary, fully supported)
- AspectJ annotation syntax subset for editor assistance (best effort)

This is a controlled middle ground between Spring-only and full AspectJ parity.

## In Scope (Internal Beta)
- `@Aspect` and advice annotation highlighting/navigation/completion support
- Pointcut syntax checks based on supported designator list in `AopInspectionRules`
- Spring bean-oriented inspection (`AspectNotBeanInspection`) and safe quick-fix
- Features validated by current non-fixture test suite

## Out of Scope (Internal Beta)
- Full semantic weaving analysis for standalone AspectJ projects
- Complete AspectJ language parity across all advanced constructs
- Cross-file call graph and deep matching engine
- Tool window and advanced architecture visualizations

## Supported Pointcut Designators (Beta)
`execution`, `within`, `this`, `target`, `args`, `@target`, `@within`, `@annotation`, `@args`, `bean`, `cflow`, `cflowbelow`, `initialization`, `preinitialization`, `staticinitialization`, `handler`, `adviceexecution`

## User-Facing Policy
- Spring-centric warnings (for example, non-bean `@Aspect`) are intentional and remain enabled.
- AspectJ-only workflows are not blocked, but behavior outside the listed subset is best effort.
- Bugs outside this scope are triaged as backlog unless they affect Spring AOP core flows.

## Exit Criteria for Scope Lock
- Scope statement published and referenced by roadmap
- Shared scope lists centralized in code to avoid drift
- Inspection descriptions and plugin metadata aligned with this policy

