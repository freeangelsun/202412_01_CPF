# CPF 외부 표준·기술 Baseline Crosswalk

외부 표준은 CPF 정본을 대체하지 않는다. Final/Stable 문서는 누락 Gap과 검증 기준으로 사용하고, Draft/Experimental은 watch 또는 명시적 opt-in으로만 사용한다.

## STD-NIST-SSDF — NIST SP 800-218 SSDF

| 항목 | 값 |
|---|---|
| 확인 버전 | Version 1.1 Final |
| CPF 적용 상태 | `NORMATIVE_GAP_DETECTOR` |
| 적용 목적 | Secure development lifecycle, vulnerability prevention, root-cause recurrence prevention, evidence and supplier communication. |
| 공식 출처 | https://csrc.nist.gov/pubs/sp/800/218/final |
| 주의 | SP 800-218 Rev.1 / SSDF 1.2 is draft; monitor only until final. |

---
## STD-OWASP-ASVS — OWASP ASVS

| 항목 | 값 |
|---|---|
| 확인 버전 | 5.0.0 Stable |
| CPF 적용 상태 | `NORMATIVE_GAP_DETECTOR` |
| 적용 목적 | Web/API security verification requirements and negative test coverage. |
| 공식 출처 | https://owasp.org/www-project-application-security-verification-standard/ |
| 주의 | Map relevant ASVS IDs to security and web/API work packages. |

---
## STD-OWASP-API — OWASP API Security Top 10

| 항목 | 값 |
|---|---|
| 확인 버전 | 2023 Edition |
| CPF 적용 상태 | `NORMATIVE_GAP_DETECTOR` |
| 적용 목적 | Object/property/function authorization, resource limits, inventory, unsafe API consumption. |
| 공식 출처 | https://owasp.org/API-Security/editions/2023/en/0x11-t10/ |
| 주의 | Use as threat/negative scenario inventory, not as a replacement for CPF security contracts. |

---
## STD-OAS — OpenAPI Specification

| 항목 | 값 |
|---|---|
| 확인 버전 | 3.2.0 latest published; 3.1.2 current 3.1 patch |
| CPF 적용 상태 | `SUPPORTED_PROFILE_DECISION_REQUIRED` |
| 적용 목적 | Pin a CPF-supported OAS profile based on generator/tool compatibility; evaluate 3.2.0 without forcing immediate migration. |
| 공식 출처 | https://spec.openapis.org/oas/ |
| 주의 | Specification text is authoritative over schemas. Sanitize Markdown/HTML and handle reference cycles. |

---
## STD-ASYNCAPI — AsyncAPI Specification

| 항목 | 값 |
|---|---|
| 확인 버전 | 3.1.0 |
| CPF 적용 상태 | `PROMOTE_TO_PRODUCT_GAP` |
| 적용 목적 | Machine-readable event/message contracts, channels, operations, bindings, correlation and examples. |
| 공식 출처 | https://www.asyncapi.com/docs/reference/specification/v3.1.0 |
| 주의 | Adopt only where tooling and provider contracts are verified. |

---
## STD-RFC9457 — IETF RFC 9457

| 항목 | 값 |
|---|---|
| 확인 버전 | Proposed Standard |
| CPF 적용 상태 | `NORMATIVE_API_ERROR_PROFILE` |
| 적용 목적 | Machine-readable HTTP problem details; type/title/status/instance and structured validation extensions. |
| 공식 출처 | https://www.rfc-editor.org/info/rfc9457/ |
| 주의 | Obsoletes RFC 7807. Avoid leaking debugging or sensitive data in detail. |

---
## STD-WCAG — WCAG

| 항목 | 값 |
|---|---|
| 확인 버전 | 2.2 W3C Recommendation |
| CPF 적용 상태 | `NORMATIVE_FRONTEND_PROFILE` |
| 적용 목적 | CPF ADM/BZA target WCAG 2.2 AA with keyboard, focus, labels, error identification and accessible authentication. |
| 공식 출처 | https://www.w3.org/TR/WCAG22/ |
| 주의 | Use testable success criteria and browser/accessibility evidence. |

---
## STD-SLSA — SLSA

| 항목 | 값 |
|---|---|
| 확인 버전 | 1.2 Approved |
| CPF 적용 상태 | `NORMATIVE_SUPPLY_CHAIN_PROFILE` |
| 적용 목적 | Source and Build tracks, provenance generation/distribution/verification and protected build platform. |
| 공식 출처 | https://slsa.dev/spec/v1.2/ |
| 주의 | Do not claim a SLSA level without satisfying the exact versioned track/level requirements. |

---
## STD-CYCLONEDX — CycloneDX

| 항목 | 값 |
|---|---|
| 확인 버전 | 1.7 / ECMA-424 2nd Edition |
| CPF 적용 상태 | `NORMATIVE_BOM_PROFILE` |
| 적용 목적 | Final-artifact SBOM plus services, cryptographic assets, data provenance, licenses and attestations. |
| 공식 출처 | https://cyclonedx.org/specification/overview/ |
| 주의 | Generate from final artifacts and verify dependency graph completeness. |

---
## STD-OTEL — OpenTelemetry Semantic Conventions

| 항목 | 값 |
|---|---|
| 확인 버전 | 1.43.0 |
| CPF 적용 상태 | `STABILITY_AWARE_PROFILE` |
| 적용 목적 | Use stable groups by default; unstable groups require explicit opt-in, version pinning and migration/dual-emit plan. |
| 공식 출처 | https://opentelemetry.io/docs/specs/semconv/ |
| 주의 | Avoid unbounded cardinality and do not silently break existing telemetry. |

---
## STD-TRACE — W3C Trace Context

| 항목 | 값 |
|---|---|
| 확인 버전 | W3C Recommendation |
| CPF 적용 상태 | `NORMATIVE_TRACE_PROFILE` |
| 적용 목적 | traceparent/tracestate validation, trust boundaries and propagation across local/remote/async/batch. |
| 공식 출처 | https://www.w3.org/TR/trace-context/ |
| 주의 | CPF transactionId remains the business execution identity and is correlated, not replaced. |

---
## STD-SCRUM — NIST SP 800-161 Rev.1 Update 1 and SP 1326

| 항목 | 값 |
|---|---|
| 확인 버전 | Final / July 2026 Quick-Start Guide |
| CPF 적용 상태 | `NORMATIVE_SUPPLIER_DUE_DILIGENCE_PROFILE` |
| 적용 목적 | Supplier/product provenance, resilience, foundational practices, supply-chain tiers and acquisition due diligence. |
| 공식 출처 | https://csrc.nist.gov/pubs/sp/1326/final |
| 주의 | Apply to OSS, build tools, registries, providers and commercial dependencies. |

---
## STD-CISA-SBD — CISA Secure by Design / Product Security Bad Practices

| 항목 | 값 |
|---|---|
| 확인 버전 | Updated January 2025 |
| CPF 적용 상태 | `NORMATIVE_BAD_PRACTICE_GAP_DETECTOR` |
| 적용 목적 | Eliminate preventable defect classes, default credentials and delayed KEV remediation; make secure defaults customer-safe. |
| 공식 출처 | https://www.cisa.gov/news-events/alerts/2025/01/17/cisa-and-fbi-release-updated-guidance-product-security-bad-practices |
| 주의 | Use as product manufacturer review criteria; preserve CPF architecture and applicable legal constraints. |

---
## STD-OPENFEATURE — OpenFeature Specification

| 항목 | 값 |
|---|---|
| 확인 버전 | Current published specification |
| CPF 적용 상태 | `PROMOTE_TO_PRODUCT_GAP` |
| 적용 목적 | Vendor-neutral evaluation API, provider lifecycle, context, hooks, events and tracking with section stability awareness. |
| 공식 출처 | https://openfeature.dev/specification/ |
| 주의 | Only stable/hardening sections are production defaults; experimental features require opt-in. |

---
## STD-CLOUDEVENTS — CloudEvents

| 항목 | 값 |
|---|---|
| 확인 버전 | 1.0 |
| CPF 적용 상태 | `EVENT_ENVELOPE_COMPATIBILITY_REFERENCE` |
| 적용 목적 | Evaluate compatibility for external/interoperable event envelope use without replacing CPF mandatory business metadata. |
| 공식 출처 | https://cloudevents.io/ |
| 주의 | Use as an interoperability profile, not an automatic mandatory envelope for every internal message. |

---
## STD-SPRING — Spring Boot / Spring Cloud

| 항목 | 값 |
|---|---|
| 확인 버전 | Spring Boot 4.1.0; Spring Cloud 2025.1.2 compatible |
| CPF 적용 상태 | `TECHNOLOGY_COMPATIBILITY_BASELINE` |
| 적용 목적 | Pin an approved stack matrix and fail closed on unsupported combinations. |
| 공식 출처 | https://docs.spring.io/spring-boot/system-requirements.html |
| 주의 | Spring Cloud 2025.1.2 adds Boot 4.1.0 compatibility. |

---
## STD-GRADLE-JAVA — Java 25 / Gradle

| 항목 | 값 |
|---|---|
| 확인 버전 | Java 25 target; Gradle 9.1 supports Java 25; current Gradle 9.x is newer |
| CPF 적용 상태 | `PINNED_BASELINE_WITH_SHADOW_UPGRADE` |
| 적용 목적 | Keep the repository-pinned wrapper as source of truth; evaluate later compatible 9.x minors in shadow builds before any upgrade. |
| 공식 출처 | https://docs.gradle.org/9.1.0/release-notes.html |
| 주의 | Use Java Toolchains. Security-patched JDK builds are required without forcing one JDK vendor. |

---
