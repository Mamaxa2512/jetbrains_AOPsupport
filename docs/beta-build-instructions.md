# Beta Build Instructions

## Building the Plugin

### Prerequisites
- Java 21 (for build toolchain)
- Git

### Build Steps

1. **Clone the repository**
   ```bash
   git clone <repository-url>
   cd AOP
   ```

2. **Run quality checks**
   ```bash
   ./gradlew clean build ktlintCheck
   ```
   
   This will:
   - Compile the plugin
   - Run all 45 tests
   - Run static checks on Gradle scripts
   - Generate the plugin artifact

3. **Run plugin verifier**
   ```bash
   ./gradlew verifyPlugin
   ```
   
   This validates compatibility with target IDE versions.

4. **Locate the build artifact**
   ```bash
   ls -lh build/distributions/
   ```
   
   The plugin ZIP will be named: `AOP-1.0.0-beta.1.zip`

### Quick Build (skip tests)
```bash
./gradlew buildPlugin -x test
```

## Installing the Beta Build

### In IntelliJ IDEA

1. Open `Settings` → `Plugins`
2. Click the `⚙️` gear icon → `Install Plugin from Disk...`
3. Select `build/distributions/AOP-1.0.0-beta.1.zip`
4. Restart IntelliJ IDEA

### Verification

After restart, verify the plugin is loaded:
1. `Settings` → `Plugins` → `Installed` → Look for "AOP Support"
2. Open a Java file with `@Aspect` annotation
3. Check that:
   - Gutter icons appear
   - Annotations are highlighted
   - Completion works in pointcut strings
   - Inspections are active

## Running in Sandbox IDE

For development/testing without installing:

```bash
./gradlew runIde
```

This launches a separate IntelliJ IDEA instance with the plugin pre-installed.

## Troubleshooting

### Build fails with "Java 21 required"
Ensure `JAVA_HOME` points to JDK 21:
```bash
export JAVA_HOME=/path/to/jdk-21
./gradlew --version  # verify
```

### Plugin verifier fails
Check the verifier report:
```bash
cat build/reports/pluginVerifier/verification-report.txt
```

### Tests fail
Run tests with verbose output:
```bash
./gradlew test --info
```

Check test reports:
```bash
open build/reports/tests/test/index.html
```

## CI Build

The CI pipeline (`.github/workflows/ci.yml`) automatically:
- Builds the plugin
- Runs all tests
- Runs plugin verifier
- Fails the build if any step fails

Pull requests must pass CI before merge.
