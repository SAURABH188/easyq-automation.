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

## Current Tests

- Login page opens
- Valid beta user can log in

The login page locators are intentionally flexible for the first version. After inspecting the actual HTML fields, replace them with stable IDs or `data-testid` attributes if available.
