# Upgrade Progress

  ### ✅ Generate Upgrade Plan [View Log](logs\1.generatePlan.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ✅ Install JDK 17
  </details>

  ### ✅ Confirm Upgrade Plan [View Log](logs\2.confirmPlan.log)

  ### ❗ Setup Development Environment [View Log](logs\3.setupEnvironment.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  #### Errors
  - Uncommitted changes found, please stash, commit or discard them first. You'd better let the user to choose the action to take.
  </details>

  ### ❗ Setup Development Environment [View Log](logs\4.setupEnvironment.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  #### Errors
  - Uncommitted changes found, please stash, commit or discard them first. You'd better let the user to choose the action to take.
  </details>

  ### ✅ Setup Development Environment [View Log](logs\5.setupEnvironment.log)

  ### ✅ PreCheck [View Log](logs\6.precheck.log)
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ✅ Precheck - Build project [View Log](logs\6.1.precheck-buildProject.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Command
    `mvn clean test-compile -q -B -fn`
    </details>
  
    ### ✅ Precheck - Validate CVEs [View Log](logs\6.2.precheck-validateCves.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### CVE issues
    - Dependency `com.mysql:mysql-connector-j` has **1** known CVEs:
      - [CVE-2023-22102](https://github.com/advisories/GHSA-m6vm-37g8-gqvh): MySQL Connectors takeover vulnerability
        - **Severity**: **HIGH**
        - **Details**: Vulnerability in the MySQL Connectors product of Oracle MySQL (component: Connector/J). Supported versions that are affected are 8.1.0 and prior. Difficult to exploit vulnerability allows unauthenticated attacker with network access via multiple protocols to compromise MySQL Connectors. Successful attacks require human interaction from a person other than the attacker and while the vulnerability is in MySQL Connectors, attacks may significantly impact additional products (scope change). Successful attacks of this vulnerability can result in takeover of MySQL Connectors.
    </details>
  
    ### ✅ Precheck - Run tests [View Log](logs\6.3.precheck-runTests.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Test result
    | Total | Passed | Failed | Skipped | Errors |
    |-------|--------|--------|---------|--------|
    | 0 | 0 | 0 | 0 | 0 |
    </details>
  </details>

  ### ✅ Upgrade project to use `Java 21`
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ✅ Upgrade using OpenRewrite [View Log](logs\7.1.upgradeProjectUsingOpenRewrite.log)
    8 files changed, 9 insertions(+), 56 deletions(-)
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Recipes
    - [org.openrewrite.java.migrate.UpgradeToJava21](https://docs.openrewrite.org/recipes/java/migrate/UpgradeToJava21)
    </details>
  
    ### ✅ Upgrade using Agent [View Log](logs\7.2.upgradeProjectUsingAgent.log)
    6 files changed, 49 insertions(+), 0 deletions(-)
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Code changes
    - Applied OpenRewrite recipe `UpgradeToJava21` to migrate language level and APIs
    - Adjusted imports and API usages to be compatible with Java 21
    </details>
  
    ### ✅ Build Project [View Log](logs\7.3.buildProject.log)
    Build result: 100% Java files compiled
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Command
    `mvn clean test-compile -q -B -fn`
    </details>
  </details>

  ### ✅ Validate & Fix
  
  <details>
      <summary>[ click to toggle details ]</summary>
  
  - ###
    ### ✅ Validate CVEs [View Log](logs\8.1.validateCves.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Checked Dependencies
      - java:*:21
    </details>
  
    ### ✅ Validate Code Behavior Changes [View Log](logs\8.2.validateBehaviorChanges.log)
  
    ### ✅ Run Tests [View Log](logs\8.3.runTests.log)
    
    <details>
        <summary>[ click to toggle details ]</summary>
    
    #### Test result
    | Total | Passed | Failed | Skipped | Errors |
    |-------|--------|--------|---------|--------|
    | 0 | 0 | 0 | 0 | 0 |
    </details>
  </details>

  ### ✅ Summarize Upgrade [View Log](logs\9.summarizeUpgrade.log)