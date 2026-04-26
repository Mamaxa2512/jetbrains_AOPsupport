# Compatibility Matrix

## Target IDE Versions

| IDE Version | Build Number | Status | Notes |
|---|---|---|---|
| IntelliJ IDEA Community 2025.1 | IC-251.x | Primary baseline | Used in `build.gradle.kts` and CI |
| IntelliJ IDEA Ultimate 2025.1 | IU-251.x | Expected compatible | Same platform, no Ultimate-only APIs used |
| IntelliJ IDEA Community 2024.3 | IC-243.x | Legacy baseline | Previously used, not current target |
| IntelliJ IDEA Community 2025.2+ | IC-252.x+ | Not validated | Planned for next compatibility pass |

## Java Language Level

| Java Version | Status |
|---|---|
| Java 8+ source in projects | Supported (plugin inspects Java PSI) |
| Java 21 toolchain (plugin build) | Required |

## Spring AOP / AspectJ Scope

| Feature | Status |
|---|---|
| `@Aspect` detection | Supported |
| `@Before`, `@After`, `@Around`, `@AfterReturning`, `@AfterThrowing` | Supported |
| `@Pointcut` | Supported |
| Spring bean annotations (`@Component`, `@Service`, `@Repository`, `@Controller`, `@Configuration`, `@RestController`) | Supported |
| Meta-annotations (custom annotations composed from Spring beans) | Supported (recursive resolution) |
| Full AspectJ compile-time weaving | Out of scope (beta) |
| XML-based Spring AOP config | Out of scope (beta) |

## Known Limitations

- Fixture-based tests use stub annotations; no runtime AspectJ/Spring jar required in the plugin itself.
- Plugin verifier runs against `recommended()` IDE set; see `build.gradle.kts`.
- Kotlin source files are not inspected (Java PSI only in current scope).

## Verification

Plugin compatibility is verified automatically in CI via:
```
./gradlew verifyPlugin
```
