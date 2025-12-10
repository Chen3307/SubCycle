
# Upgrade Java Project

## 🖥️ Project Information
- **Project path**: c:\codes\SubCycle\backend
- **Java version**: 21
- **Build tool type**: Maven
- **Build tool path**: C:\apache-maven-3.9.11\bin

## 🎯 Goals

- Upgrade Java to 21

## 🔀 Changes

### Test Changes
|     | Total | Passed | Failed | Skipped | Errors |
|-----|-------|--------|--------|---------|--------|
| Before | 0 | 0 | 0 | 0 | 0 |
| After | 0 | 0 | 0 | 0 | 0 |
### Dependency Changes


#### Upgraded Dependencies
| Dependency | Original Version | Current Version | Module |
|------------|------------------|-----------------|--------|
| Java | 17 | 21 | Root Module |

### Code commits

All code changes have been committed to branch `appmod/java-upgrade-20251210064356`, here are the details:
7 files changed, 9 insertions(+), 7 deletions(-)

- d393964 -- Upgrade project to use `Java 21` using openrewrite.

- 87b9e1c -- Upgrade to Java 21 via OpenRewrite
### Potential Issues

#### CVEs
- com.mysql:mysql-connector-j:
  - [**HIGH**][CVE-2023-22102](https://github.com/advisories/GHSA-m6vm-37g8-gqvh): MySQL Connectors takeover vulnerability
