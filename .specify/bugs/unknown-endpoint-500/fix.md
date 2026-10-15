# Bug Fix: Unknown endpoint returns 404

- **Slug**: unknown-endpoint-500
- **Fixed**: 2026-10-14
- **Assessment**: ./assessment.md
- **Status**: applied

## Summary

Added a specific NoResourceFoundException handler returning 404 through the existing ApiErrorResponse helper. The generic unexpected-exception 500 handler is unchanged.

## Changes

| File | Change | Notes |
|------|--------|-------|
| `src/main/java/com/fde/backlogassistant/exception/GlobalExceptionHandler.java` | modified | Added only the approved import and dedicated 404 handler. |
| `src/test/java/com/fde/backlogassistant/controller/BacklogControllerTest.java` | added tests | Applied the user-approved observable API assertions. |

## Tests Added or Updated

- `BacklogControllerTest::shouldReturn404ForUnknownEndpoint` — parameterized GETs for /api/does-not-exist and /api/backlog/does-not-exist; checks HTTP 404, JSON error fields, timestamp, null validationErrors, and no service interactions.
- `BacklogControllerTest::shouldPreserve500ForUnexpectedException` — a genuine unexpected RuntimeException retains HTTP 500 and the sanitized error/message.

## Local Verification

- Before production edit: `mvn -o "-Dtest=BacklogControllerTest" test` — 6 tests, 2 failures, 0 errors, 0 skipped. Both unknown-endpoint cases expected 404 but received 500; the other four cases passed. Logs showed NoResourceFoundException reaching the global catch-all.
- After production edit: same command — BUILD SUCCESS; 6 tests, 0 failures, 0 errors, 0 skipped. Suite time 8.327 seconds; Maven total time 24.434 seconds.
- All six cases passed: both unknown-endpoint parameters, successful generation, rate-limit 429, validation 400, and unexpected-exception 500.
- `git diff --check` — passed. Inspected production diff and confirmed it exactly matches the approved change. No other production file changed.
- No live-server curl verification was performed. The expected RuntimeException stack trace in the successful test output comes from the deliberate 500 regression scenario.

## Deviations from Assessment

At the user's explicit request, tests assert observable API behavior without importing or asserting the Spring exception type, and omit the redundant ObjectMapper validationErrors presence check. No production remediation deviation.

## Follow-ups

- Optional independent verification: `$speckit-bug-test slug=unknown-endpoint-500`.
- No before/after bug-fix hooks are registered in `.specify/extensions.yml` (hooks is empty).
