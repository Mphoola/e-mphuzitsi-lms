# Test Coverage Guide for E-Empuzitsi LMS

This guide explains how to run test coverage analysis for the E-Empuzitsi Learning Management System.

## Prerequisites

- Java 21
- Maven (or use the included Maven wrapper `./mvnw`)
- All project dependencies installed

## Coverage Tools Configured

### 1. JaCoCo (Java Code Coverage)

JaCoCo is configured in `pom.xml` with the following features:
- **Automatic coverage collection** during test execution
- **HTML reports** generated in `target/site/jacoco/`
- **CSV and XML reports** for CI/CD integration
- **Coverage thresholds** (80% line coverage by default)
- **Exclusions** for generated code (Lombok, builders, etc.)

## How to Run Coverage

### Option 1: Using the Coverage Script (Recommended)

We've created a helper script `run-coverage.sh` with multiple options:

```bash
# Make script executable (one time)
chmod +x run-coverage.sh

# Basic coverage (fastest)
./run-coverage.sh basic

# Coverage with quality gates (fails if < 70%)
./run-coverage.sh check

# Entity tests only
./run-coverage.sh entity

# Full comprehensive coverage
./run-coverage.sh full

# Open HTML report in browser
./run-coverage.sh report

# Clean coverage data
./run-coverage.sh clean
```

### Option 2: Direct Maven Commands

```bash
# Basic test with coverage
./mvnw clean test jacoco:report

# Test with coverage verification
./mvnw clean test jacoco:report jacoco:check

# Run only entity tests
./mvnw clean test jacoco:report -Dtest="**/*EntityTest"

# Full verification with profiles
./mvnw clean verify -P coverage-check
```

### Option 3: IDE Integration

Most IDEs support JaCoCo coverage:

**IntelliJ IDEA:**
1. Right-click on test class/package
2. Select "Run with Coverage"
3. View coverage in the IDE

**VS Code with Java Extension:**
1. Use Command Palette: "Java: Run Tests with Coverage"
2. Or use the Test Explorer with coverage enabled

## Coverage Reports

### HTML Report Location
After running coverage, open: `target/site/jacoco/index.html`

### Report Contents
- **Overview**: Overall project coverage percentages
- **Package View**: Coverage by package
- **Class View**: Coverage by class with highlighted code
- **Method View**: Method-level coverage details

### Coverage Metrics Explained
- **Instructions**: Java bytecode instructions covered
- **Branches**: Decision points (if/else, switch) covered
- **Lines**: Source code lines executed
- **Methods**: Methods called during tests
- **Classes**: Classes loaded and used

## Coverage Thresholds

Current thresholds configured:
- **Line Coverage**: 80% (configurable in pom.xml)
- **Branch Coverage**: 60% (in coverage-check profile)

### Adjusting Thresholds

Edit `pom.xml` in the JaCoCo plugin configuration:

```xml
<limit>
    <counter>LINE</counter>
    <value>COVEREDRATIO</value>
    <minimum>0.80</minimum> <!-- 80% - adjust as needed -->
</limit>
```

## Exclusions

The following are excluded from coverage analysis:
- Lombok generated code (`**/*$*Builder*`)
- Main application class (`EEmpuzitsiApplication.class`)
- Configuration classes (`**/config/*`)
- DTO classes (`**/dto/*`)

### Adding More Exclusions

Edit the JaCoCo plugin `<excludes>` section in `pom.xml`:

```xml
<excludes>
    <exclude>**/YourClassToExclude.class</exclude>
    <exclude>**/package/to/exclude/*</exclude>
</excludes>
```

## CI/CD Integration

### GitHub Actions Example

```yaml
- name: Run Tests with Coverage
  run: ./mvnw clean test jacoco:report

- name: Upload Coverage Reports
  uses: codecov/codecov-action@v3
  with:
    file: target/site/jacoco/jacoco.xml
```

### Coverage Badge

After integrating with Codecov or similar:
```markdown
[![Coverage](https://codecov.io/gh/Mphoola/e-mphuzitsi-lms/branch/main/graph/badge.svg)](https://codecov.io/gh/Mphoola/e-mphuzitsi-lms)
```

## Current Test Coverage Status

As of the latest run:
- **Entity Tests**: 12 test classes with 77+ tests
- **Coverage Areas**: 
  - ✅ Entity layer (comprehensive)
  - ⚠️ Service layer (needs expansion)
  - ⚠️ Controller layer (needs expansion)
  - ⚠️ Repository layer (needs expansion)

## Best Practices

### 1. Aim for Meaningful Coverage
- Focus on business logic coverage
- Don't just chase percentages
- Ensure edge cases are tested

### 2. Regular Coverage Monitoring
- Run coverage with each build
- Set up automated coverage checks
- Monitor coverage trends over time

### 3. Coverage-Driven Development
- Write tests first (TDD)
- Check coverage after adding features
- Use coverage to identify missing tests

## Troubleshooting

### Common Issues

1. **"No coverage data" error**
   ```bash
   ./run-coverage.sh clean
   ./run-coverage.sh basic
   ```

2. **Tests failing in coverage mode**
   - Check if JaCoCo agent affects test behavior
   - Verify test configuration

3. **Low coverage percentages**
   - Review excluded packages
   - Add tests for uncovered code paths
   - Consider if exclusions are appropriate

### Getting Help

- Check the HTML coverage report for detailed information
- Review test logs in `target/surefire-reports/`
- Use verbose mode: `./mvnw clean test jacoco:report -X`

## Examples

### Entity Test Coverage
```bash
# Run all entity tests with coverage
./run-coverage.sh entity

# Results: High coverage expected (90%+)
# Location: target/site/jacoco/com.mphoola.e_empuzitsi.entity/
```

### Service Layer Coverage
```bash
# Run service tests (when available)
./mvnw clean test jacoco:report -Dtest="**/*ServiceTest"
```

### Integration Test Coverage
```bash
# Full integration test coverage
./mvnw clean verify -P coverage
```

---

**Note**: This coverage setup provides comprehensive analysis for the E-Empuzitsi LMS project. Regular coverage monitoring helps maintain code quality and identifies areas needing additional testing.
