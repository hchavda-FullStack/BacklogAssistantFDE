# Bug Verification: Unknown endpoint returns 404

- **Slug**: unknown-endpoint-500
- **Tested**: 2026-10-14
- **Assessment**: ./assessment.md
- **Fix**: ./fix.md
- **Result**: verified

## Summary

The original unknown-endpoint 500 defect is resolved. Automated tests pass, and the user confirmed that the original curl reproduction returns HTTP 404 after restarting the application, with the expected ApiErrorResponse fields preserved. Genuine unexpected exceptions still return the existing generic HTTP 500 response, as verified by the passing regression test.

## Checks Performed

| Check | Command / Action | Result | Notes |
|-------|------------------|--------|-------|
| Pre-fix automated reproduction | `mvn -o "-Dtest=BacklogControllerTest" test` | pass | Earlier agent-run baseline confirmed the bug: 6 tests, 2 failures, 0 errors, 0 skipped; both unknown GETs expected 404 but received 500. |
| Post-fix focused tests | `mvn -o "-Dtest=BacklogControllerTest" test` | pass | Agent-run evidence from this session: exit 0, 6 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. |
| Full regression suite | `mvn clean test` | pass | Agent-run evidence from this session: exit 0, 22 tests, 0 failures, 0 errors, 0 skipped, BUILD SUCCESS. Surefire report totals were subsequently checked. |
| Original live-server reproduction after restart | `curl -i "http://localhost:8080/api/does-not-exist"` | pass | User-supplied verified runtime evidence: HTTP 404 after restarting the application. Not independently rerun by the agent during this report update. |
| Runtime ApiErrorResponse fields | Inspect runtime response | pass | User confirms status 404, error Not Found, message The requested resource was not found., and validationErrors null. |
| Genuine unexpected exception | `BacklogControllerTest::shouldPreserve500ForUnexpectedException` | pass | RuntimeException from the mocked backlog service returns HTTP 500, status 500, error Internal server error, and message An unexpected error occurred. Internal exception details are not exposed in the response message. |
| Existing controller behavior | Successful POST, validation, and rate-limit tests | pass | Existing 200, 400, and 429 cases passed. Both unknown-path cases also passed verifyNoInteractions for backlog, PDF, and rate-limit services. |
| Diff integrity | `git diff --check` and production diff inspection | pass | Previously checked in this session: only the approved import and dedicated 404 handler were added to production code. Generic 500 handler and ApiErrorResponse helper are unchanged. |
| Compilation | Maven compiler phases in `mvn clean test` | pass | Production and test sources compiled successfully. No separate lint plugin is configured in pom.xml. |

The focused and full Maven checks were performed earlier in this session and were not repeated for this documentation update. Runtime evidence was supplied explicitly by the user following application restart. The user authorized overwriting the previous partial report. No Java or test source changes were made during verification or this report update. Extension configuration has an empty hooks map, so no before/after bug-test hooks apply.

## Output Excerpts

Pre-fix focused run:

```text
Status expected:<404> but was:<500>
Tests run: 6, Failures: 2, Errors: 0, Skipped: 0
BUILD FAILURE
```

Post-fix focused run:

```text
Tests run: 6, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

Full Maven suite:

```text
Tests run: 22, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
Total time: 28.006 s
```

User-confirmed runtime result after restart (relevant fields, not a verbatim full response):

```text
HTTP 404
status = 404
error = "Not Found"
message = "The requested resource was not found."
validationErrors = null
```

## Residual Risks

- The live-server result is user-verified evidence, not an independently captured agent HTTP transcript. Runtime timestamp was not included in that evidence; the passing MockMvc regression checks a nonempty timestamp.
- Genuine unexpected-exception 500 behavior is verified by automated regression, not by injecting a failure into the running application.
- Other framework client-error mappings and deployments that disable default resource mappings remain outside the assessed remediation's scope.

## Recommendation

Close unknown-endpoint-500 as verified. The original request now returns HTTP 404 in both automated regression and the user-confirmed restarted application, preserving the existing ApiErrorResponse format. The passing unexpected-exception regression confirms genuine unexpected failures retain generic HTTP 500 behavior, and the full 22-test suite reports no regressions.
