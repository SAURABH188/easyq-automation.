# easyQ Automation

Selenium Java TestNG automation framework for the easyQ beta web application.

## Tech Stack

- Java 17
- Maven
- Selenium WebDriver
- TestNG
- WebDriverManager

## Beta URL

`https://beta.easyqsolutions.com/#/easyqsolutions/login`

## Eclipse Setup

1. Open Eclipse.
2. Select `File > Import > Maven > Existing Maven Projects`.
3. Choose this `easyq-automation` folder.
4. Wait for Maven dependencies to download.
5. Set this environment variable in the TestNG run configuration:

```text
EASYQ_PASSWORD=your_password_here
```

The username is already kept in `src/test/resources/config.properties`. Keep the password out of source files.

## Run Tests

Run `testng.xml` as a TestNG Suite.

## GitHub Actions Automatic Run

This project includes a GitHub Actions workflow at `.github/workflows/easyq-automation.yml`.

The workflow can run Selenium TestNG suites on GitHub using headless Chrome. Reports, screenshots, and downloaded files are saved as GitHub run artifacts.

### What Runs Automatically

- A safe scheduled run executes every day at 07:00 IST and 19:00 IST.
- The scheduled run uses `testng-login-ui-ux.xml`.
- Workflow mutation is disabled for scheduled runs, so QP/QO/R&A records are not changed automatically.

### How to Run Manually

1. Open the GitHub repository.
2. Go to `Actions`.
3. Select `easyQ Selenium Automation`.
4. Click `Run workflow`.
5. Select the required TestNG suite.
6. Keep `allow_workflow_mutations=false` for read-only/UI checks.
7. Select `allow_workflow_mutations=true` only for beta workflow suites that create drafts, send reviews, reject, approve, or move records.
8. Click `Run workflow`.

### Required GitHub Secrets

Add these under `Repository Settings > Secrets and variables > Actions > Secrets`:

```text
EASYQ_PASSWORD
EASYQ_ADMIN_PASSWORD
EASYQ_DOC_CONTROLLER_PASSWORD
EASYQ_DOC_CONTROLLER_AMITT_PASSWORD
EASYQ_DOC_CONTROLLER_ANASUYA_PASSWORD
EASYQ_DOC_CONTROLLER_SHUBHAM_PASSWORD
EASYQ_ASSIGNEE_AMIT_PASSWORD
EASYQ_ASSIGNEE_SWATI_PASSWORD
EASYQ_ASSIGNEE_KARTIK_PASSWORD
EASYQ_ASSIGNEE_AYESHA_PASSWORD
EASYQ_ASSIGNEE_ANUSHKA_PASSWORD
EASYQ_ASSIGNEE_HIMI_PASSWORD
EASYQ_ASSIGNEE_KAVITA_PASSWORD
EASYQ_ASSIGNEE_SAURABH_PASSWORD
EASYQ_QP_REVIEWER1_PASSWORD
EASYQ_TEST_USER_PASSWORD
EASYQ_NEW_PASSWORD
EASYQ_SUPER_ADMIN_PASSWORD
```

Only add the secrets needed by the suite you are running. For example, login UI tests need the admin password, while QP/QO/R&A workflow tests need reviewer and approver passwords.

### Optional GitHub Variables

Add these under `Repository Settings > Secrets and variables > Actions > Variables` if you want GitHub to drive user-management test data:

```text
EASYQ_TEST_USER_FIRST_NAME
EASYQ_TEST_USER_LAST_NAME
EASYQ_TEST_USER_EMAIL
EASYQ_TEST_USER_DESIGNATION
EASYQ_TEST_USER_GROUP
EASYQ_TEST_USER_ROLE
EASYQ_EDIT_USER_EMAIL
EASYQ_EDIT_USER_NEW_FIRST_NAME
EASYQ_EDIT_USER_NEW_LAST_NAME
EASYQ_DISABLE_USER_EMAIL
EASYQ_DUPLICATE_USER_EMAIL
EASYQ_EXPECTED_LICENSE_AVAILABLE
EASYQ_EXPECTED_LICENSE_PURCHASED
EASYQ_DOC_CONTROLLER_AMITT_USERNAME
EASYQ_DOC_CONTROLLER_ANASUYA_USERNAME
EASYQ_DOC_CONTROLLER_SHUBHAM_USERNAME
EASYQ_SUPER_ADMIN_USERNAME
```

### Reports

After each run, open the workflow run and download `easyq-test-artifacts-*`.

The artifact can contain:

- Maven Surefire reports from `target/surefire-reports`
- TestNG reports and screenshots from `test-output`
- Downloaded validation files from `target/easyq-downloads`

## Current Tests

- Login page opens
- Valid beta user can log in

The login page locators are intentionally flexible for the first version. After inspecting the actual HTML fields, replace them with stable IDs or `data-testid` attributes if available.
