# Smart Backlog Assistant Constitution

## Core Principles

### I. AI-Generated Implementation, Human Review

Coding agents MAY generate implementation code, tests, specifications, and documentation.
Generated changes MUST receive human review before acceptance. The reviewer MUST understand
and validate the implementation, its assumptions, failure paths, and verification evidence;
generation or a passing build alone MUST NOT count as acceptance. Review feedback MUST be
resolved or explicitly dispositioned before acceptance. Humans remain accountable for the
correctness of accepted work.

### II. Test-Driven Verification

Behavioral changes and bug fixes MUST include automated regression tests that assert observable
behavior. For bug fixes, the failing behavior MUST be reproduced before the production fix where
practical; if impractical, the reason and available evidence MUST be recorded. Tests MUST cover
the reported failure and relevant existing behavior. The full Maven suite, `mvn clean test`,
MUST pass before a change is considered complete. Focused tests accelerate feedback but MUST NOT
replace this completion gate. Verification records MUST distinguish executed checks, supplied
evidence, skipped checks, and unresolved limitations.

### III. Security and Privacy

API keys and secrets MUST NOT be committed to source control, fixtures, specifications, or
verification artifacts. Runtime credentials MUST come from environment configuration or an
appropriate external secret source. User requirement text and PDF contents MUST be treated as
untrusted input: validation and resource limits MUST apply before expensive processing, and
request content MUST NOT override application-controlled system instructions.

Prompts and complete requirement or PDF content MUST NOT be exposed through application logs,
traces, observation payloads, or metrics. Diagnostic output MUST exclude credentials and redact
sensitive content, including content embedded in provider errors. Observability MUST use safe
metadata and measurements rather than user content. These controls protect user confidentiality
without sacrificing operational diagnosis.

### IV. API Stability and Error Handling

Existing REST endpoints, request and response schemas, and documented behavior MUST remain
backward compatible unless an explicit specification authorizes the change. Authorized breaking
changes MUST document client impact and migration requirements. HTTP status codes MUST accurately
represent the failure condition, including missing endpoints, invalid input, rate limiting,
provider failures, and unexpected application errors.

Error responses MUST use the existing `ApiErrorResponse` structure wherever applicable:
`timestamp`, `status`, `error`, `message`, and `validationErrors`. An exception to that structure
MUST have a documented compatibility or framework constraint. Genuine unexpected failures MUST
retain sanitized generic server-error responses; stack traces, credentials, provider internals,
and sensitive request content MUST NOT be returned to clients. Regression tests MUST protect
both corrected status mappings and generic HTTP 500 handling.

### V. AI Reliability and Cost Protection

Permanent AI failures, including authentication and other non-retryable failures, MUST fail fast.
Transient failures MAY be retried only through a configured finite attempt limit and exponential
backoff. Retry classification and exhaustion behavior MUST be tested. Provider and application
retry layers MUST NOT combine into an uncontrolled multiplication of attempts; preserve the
existing application-owned retry policy with provider retry constrained accordingly.

Application rate limits MUST protect access to expensive model calls. Provider throttling and
retry guidance MUST be respected within the configured retry budget. Token usage, model identity,
call duration, attempts, and outcomes MUST remain observable through privacy-preserving metrics
or safe diagnostic metadata. Reliability mechanisms MUST bound cost rather than conceal repeated
failures.

### VI. Bounded Input Processing

PDF uploads MUST enforce configured upload-size, page-count, and extracted-text limits. Invalid
or over-limit documents MUST be rejected before model calls and before avoidable expensive work.
Large accepted documents MUST use bounded chunk processing with configured chunk size and
overlap. Extraction, chunk construction, and model-call orchestration MUST NOT permit unbounded
memory use, processing work, or call growth from unbounded input.

Limits MUST remain configurable and enforced at the relevant boundaries. Changes to those limits
or to chunk processing MUST include boundary and rejection tests that demonstrate protection
against resource exhaustion. Accepted large inputs MUST retain bounded processing throughout the
workflow, not merely pass an initial upload check.

### VII. Spec-Driven Changes

Requirements or concrete bug evidence MUST be established before implementation. A feature
specification or bug assessment MUST identify expected behavior, scope, risks, and acceptance
criteria. AI-generated assessments, plans, tests, and implementation MUST receive human review at
appropriate checkpoints before downstream work is accepted. Material changes in scope or root
cause MUST be documented and reviewed before implementation continues against revised assumptions.

Verification MUST demonstrate that the result satisfies the original specification or bug report,
including its reproduction where applicable. Automated equivalents MAY support verification, but
reports MUST state their limits and MUST NOT claim unperformed runtime checks. Traceable evidence
connects the requested behavior, reviewed implementation, and final acceptance.

## Technology and Architecture Constraints

The project MUST preserve its existing Java 17, Spring Boot, Maven, Spring AI with OpenAI,
Apache PDFBox, Jakarta Bean Validation, JUnit 5, Mockito, MockMvc, Actuator, and Micrometer
technology choices unless a separately reviewed specification authorizes a change. This
constitution does not mandate dependency upgrades, provider changes, or additional infrastructure.

The existing separation of REST controllers, service orchestration, prompt construction,
AI-client policy, PDF extraction, chunking, rate limiting, configuration, models, and centralized
exception handling MUST be maintained. New work MUST fit these responsibilities or justify an
architectural change explicitly. Preserve the current application-owned selective retry policy,
in-memory rate limiter, and bounded document pipeline; distributed storage, OCR, retrieval,
provider fallback, and parallel processing require their own requirements and review.

Existing configuration remains the source of operational limits and model settings. Governance
MUST NOT be interpreted as approval to rewrite application code, alter REST contracts, or change
production settings without a scoped change request.

## Development and Review Workflow

1. Establish a feature specification or bug assessment with expected behavior and acceptance
   criteria. Review the evidence, intended scope, API impact, privacy, and resource constraints.
2. Prepare and review a proportionate implementation plan and regression tests. For a bug,
   demonstrate the failing test before the production fix where practical; record any exception.
3. Implement the smallest change that satisfies the reviewed requirements. Obtain review at
   agreed checkpoints and honor explicit requests to review a proposed diff before editing.
4. Run focused checks, then `mvn clean test`. Verify the original reproduction or document the
   automated equivalent and any outstanding runtime verification. Record commands, results,
   evidence provenance, and residual risks without exposing secrets or user content.
5. Obtain human acceptance after the reviewer validates implementation and evidence against the
   specification or bug report and this constitution. A failing full suite or unresolved critical
   verification gap MUST prevent completion claims.

Reviews MUST check compatibility, accurate HTTP statuses, sanitized errors, secret handling,
content-free observability, retry bounds, rate limits, and input-processing bounds as applicable.
A check that does not apply MUST have a short rationale rather than a fabricated result.
Documentation-only changes do not require new behavioral tests, but the full-suite completion
gate still applies; existing passing evidence MAY be reused when executable inputs are unchanged
and its provenance and applicability are recorded.

## Governance

This constitution governs project engineering decisions and acceptance criteria. Specifications,
plans, implementation, and reviews MUST demonstrate compliance. It does not override explicit
user authorization requirements or higher-priority execution and safety constraints.

Amendments MUST state the reason, affected principles, compatibility impact, and any transition
work. A human reviewer MUST approve the amendment before it is accepted. Preserve the original
ratification date, update the last-amended date, and record the semantic version change and
rationale. A specification cannot silently waive a principle: conflicts MUST be resolved through
a reviewed amendment or a compliant design before acceptance.

Constitution versions use MAJOR.MINOR.PATCH: increment MAJOR for incompatible principle removals
or redefinitions, MINOR for new principles or materially expanded guidance, and PATCH for
non-semantic clarification or corrections. Version 1.0.0 is the initial adoption of the seven
project principles. The initial ratification date is 2026-10-15.

Every change review MUST assess applicable constitutional requirements and record unresolved
issues. Compliance claims MUST be supported by review and verification evidence; adoption of this
document alone does not establish that all existing code has been audited. Existing gaps MUST be
captured as scoped follow-up work rather than silently implemented during governance updates.
Dependent templates and commands consume this document at runtime and MUST NOT be rewritten as
part of this constitution-only update.

**Version**: 1.0.0 | **Ratified**: 2026-10-15 | **Last Amended**: 2026-10-15
