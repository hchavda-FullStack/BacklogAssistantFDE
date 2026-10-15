# Bug Assessment: Unknown endpoint returns 500 instead of 404

- **Slug**: unknown-endpoint-500
- **Created**: 2026-10-14
- **Source**: pasted text
- **Verdict**: valid
- **Severity**: medium

## Report (verbatim or summarized)

The user reports that `curl -i "http://localhost:8080/api/does-not-exist"` returns HTTP 500 with:

```json
{
  "status": 500,
  "error": "Internal server error",
  "message": "An unexpected error occurred.",
  "validationErrors": null
}
```

Expected: HTTP 404 because neither a controller mapping nor a resource exists. Assessment only; do not change Java source or implement a fix. Preserve ApiErrorResponse where practical.

URL policy record: supplied URL `http://localhost:8080/api/does-not-exist`; parsed host `localhost`; branch `auto-refused: loopback host`. This is a reproduction target, not an external report source; no HTTP request was made.

## Symptom

An unknown API GET is classified as an unexpected application failure. The missing endpoint should return 404, while genuine unexpected exceptions should retain the existing sanitized 500 response.

## Reproduction

1. Run the application using its configured port 8080 and required environment configuration (`OPENAI_API_KEY` is referenced by application.yml).
2. Run `curl -i "http://localhost:8080/api/does-not-exist"`.
3. Compare the reported 500 response with the expected 404.

Evidence scope: the user's runtime result is supported by application source and inspection of the locally installed framework bytecode. No live request, new test, or application startup was performed during this assessment. No Java source was changed.

## Suspected Code Paths

- `pom.xml:12` — Spring Boot 3.5.4 parent; spring-boot-starter-web enables MVC. The locally cached Boot dependency BOM specifies Spring Framework 6.2.9.
- `src/main/resources/application.yml:1` — no override disabling static-resource mappings or restricting their default path pattern; port 8080 is configured.
- `src/main/java/com/fde/backlogassistant/controller/BacklogController.java:16` — controller prefix is `/api/backlog`; only POST `/generate` and `/generate/pdf` are mapped. No mapping exists for the reported URL.
- `src/main/java/com/fde/backlogassistant/exception/GlobalExceptionHandler.java:16` — unrestricted global RestControllerAdvice applies to resource-handler exceptions as well as controller exceptions.
- `src/main/java/com/fde/backlogassistant/exception/GlobalExceptionHandler.java:53` — catch-all Exception handler logs an unexpected error and explicitly returns HTTP 500 with the reported text.
- `src/main/java/com/fde/backlogassistant/exception/GlobalExceptionHandler.java:59` — shared response helper constructs ApiErrorResponse, including timestamp and nullable validationErrors.
- `src/main/java/com/fde/backlogassistant/exception/ApiErrorResponse.java:6` — existing error envelope has timestamp, status, error, message, and validationErrors.
- `src/test/java/com/fde/backlogassistant/controller/BacklogControllerTest.java:20` — existing WebMvcTest uses MockMvc and mocked services; tests cover successful POST, validation 400, and rate limiting 429, but no unknown GET.

## Root Cause Hypothesis

**Confidence: high.** This is a missing specific exception mapping in the global advice, not a failure inside the backlog service. Under the repository's default MVC resource configuration, the exact expected exception flow is:

1. DispatcherServlet dispatches GET `/api/does-not-exist`. RequestMappingHandlerMapping finds no controller method.
2. The default static-resource `/**` mapping selects ResourceHttpRequestHandler. Thus a handler exists even though no controller or resource exists.
3. ResourceHttpRequestHandler.handleRequest calls getResource; no resource is found, so it throws `org.springframework.web.servlet.resource.NoResourceFoundException`, carrying GET and the resource path (normally `api/does-not-exist`).
4. DispatcherServlet sends that exception through its HandlerExceptionResolver chain. ExceptionHandlerExceptionResolver runs before ResponseStatusExceptionResolver and DefaultHandlerExceptionResolver.
5. There is no specific advice handler for NoResourceFoundException. It extends ServletException, hence Exception, so the global `@ExceptionHandler(Exception.class)` selects handleUnexpectedException.
6. That method logs `Unexpected application error.` and returns the explicit 500 ResponseEntity via response(...). Resolution is complete; DefaultHandlerExceptionResolver never gets to apply the exception's native 404 status.

Local `javap -c -p` inspection of spring-webmvc 6.2.9 confirmed the missing-resource throw in ResourceHttpRequestHandler, NoResourceFoundException's ErrorResponse implementation and NOT_FOUND status, and the default resolver order in WebMvcConfigurationSupport. Source inspection found no custom MVC configuration changing this flow. No runtime stack trace was supplied, so this flow is code-grounded rather than independently captured from the running server.

`NoHandlerFoundException` is a separate flow used when no handler mapping matches at all; it is not the expected exception for this request with the default resource fallback enabled. Handling only that exception would leave the reported bug unresolved.

## Proposed Remediation

**Preferred**: Add a dedicated `@ExceptionHandler(NoResourceFoundException.class)` method to GlobalExceptionHandler. Return the existing response helper with HttpStatus.NOT_FOUND, error `Not Found`, a stable safe message such as `The requested resource was not found.`, and null validationErrors. Keep the catch-all for genuine unexpected errors. More-specific exception matching ensures the new handler wins without reordering methods or changing MVC configuration. Avoid logging routine missing-resource requests as unexpected server failures.

This is the smallest safe production change: one import and one specific handler in one class. ApiErrorResponse and application.yml need no changes. Retain timestamp as part of the existing envelope, although the pasted response omits it.

**Alternatives**:

- Also handle NoHandlerFoundException with the same 404 envelope if supporting deployments that disable or restrict resource mappings is an explicit requirement. This is optional hardening, not necessary for the current default resource flow.
- A broader ErrorResponse-aware strategy or extending ResponseEntityExceptionHandler could preserve other framework status codes, but changes more behavior and may change response bodies. Treat that as separate work. Removing the catch-all or disabling resource mappings is broader than necessary and does not reliably preserve the existing envelope.

**Files likely to change**:

- `src/main/java/com/fde/backlogassistant/exception/GlobalExceptionHandler.java`
- `src/test/java/com/fde/backlogassistant/controller/BacklogControllerTest.java`

**Tests to add or update**:

- Add a MockMvc GET `/api/does-not-exist` in the existing WebMvcTest with default resource mappings enabled. Assert HTTP 404, JSON content type, status 404, error `Not Found`, the agreed safe message, nonempty timestamp, and an explicitly null validationErrors field. Assert resolvedException is NoResourceFoundException to prove the actual resource fallback is exercised. A directly invoked advice unit test alone would miss mapping/resolver behavior.
- Add GET `/api/backlog/does-not-exist` to cover a missing child of the API prefix and confirm no backlog/PDF/rate-limit service calls occur for these unknown GETs.
- Mock an unexpected RuntimeException from backlogAssistantService.generateBacklog on an otherwise valid POST and assert HTTP/body status 500 with the existing generic error/message and no leaked exception details.
- Retain and run the existing successful POST, validation 400, and rate-limit 429 tests. The focused verification command after implementation is `mvn -Dtest=BacklogControllerTest test`.
- If optional NoHandlerFoundException support is added, test it separately with resource mappings disabled; do not substitute that test for the default resource-fallback case.

## Risks & Considerations

- Medium severity: unknown routes are misreported as server failures, misleading clients and potentially inflating server-error alerts/retries. No evidence of data corruption or a critical outage.
- The specific handler also gives missing static resources a 404 ApiErrorResponse because the advice is global. Confirm JSON is acceptable for those clients; no static configuration change is needed.
- Preserve the existing error DTO, including timestamp and null validationErrors. Avoid exposing raw exception messages or resource internals.
- Other framework client-error exceptions may also be caught by the generic handler; assessing all such status mappings is outside this narrowly scoped remediation.
- No migration or meaningful performance impact is expected. Regression age is unknown; no history claim is made.

## Open Questions

- No clarification is required to implement the preferred narrow fix.
- Runtime stack trace and independent HTTP reproduction remain uncollected. An implementation-stage MockMvc regression should confirm the resource fallback and response under the actual application configuration.
