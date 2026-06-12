# Login and Forgot Password Test Scenarios

## Automation Priority

Start automation in this order:

1. P1 smoke and validation tests
2. P2 functional edge cases
3. P3 UI/alignment checks, if they are stable enough for automation

## P1 Tests

| Test ID | Scenario | Test Case | Estimate |
|---|---|---|---|
| TS001 | Login Page Load | Verify login page loads successfully with valid URL | 10 |
| TS002 | Login Page UI | Verify logo, title, fields, and button are displayed | 5 |
| TS007 | Email Field | Verify email field accepts valid input | 5 |
| TS008 | Email Field | Verify email format validation | 5 |
| TS009 | Email Field | Verify mandatory field validation | 5 |
| TS012 | Password Field | Verify password field accepts input | 2 |
| TS014 | Password Field | Verify mandatory validation | 2 |
| TS017 | Login Functionality | Verify login with valid credentials | 1 |
| TS018 | Login Functionality | Verify login with invalid credentials | 2 |
| TS019 | Login Functionality | Verify login with empty fields | 1 |
| TS024 | Forgot Password | Verify error handling for invalid/empty email | 2 |
| TS026 | Navigation & Session | Verify redirect after successful login | 1 |
| TS027 | Navigation & Session | Verify logout and re-login flow | 1 |
| TS028 | Navigation & Session | Verify session timeout behavior | 1 |
| TS030 | Performance | Verify login response time | 1 |

## Starter Automated Tests Added

The starter class `EasyQLoginValidation.java` covers:

- TS001
- TS002
- TS004
- TS005
- TS006
- TS007
- TS012
- TS017
- TS021
- TS022

Some validations, such as exact error messages, password reset link behavior, logout, and session timeout, should be added after confirming the exact UI messages and business rules.
