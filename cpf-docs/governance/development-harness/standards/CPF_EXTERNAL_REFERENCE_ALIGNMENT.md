# 외부 표준 정렬 — CPF 정본 보조 기준

- 공식 Source 확인일: **2026-08-29**

외부 표준은 CPF Product Contract를 대체하지 않는다. 충돌 시 CPF Architecture/QA Requirement를 우선하고, 외부 표준은 빠진 품질 축을 발견하는 **보조 검수 기준**으로만 사용한다.

- NIST SP 800-218 SSDF v1.1 — secure development practice와 vulnerability root-cause 재발 방지. https://csrc.nist.gov/pubs/sp/800/218/final
- SLSA v1.2 Build — 산출물 digest로 결과를 식별하고 build provenance/격리를 강화. https://slsa.dev/spec/v1.2/build-requirements
- OWASP ASVS 5.0 — Web/API 보안 요구를 검증 가능한 Acceptance 항목으로 사용. https://owasp.org/www-project-application-security-verification-standard/
- Gradle Dependency Verification — dependency checksum/signature와 strict verification. https://docs.gradle.org/current/userguide/dependency_verification.html
- Spring Boot Externalized Configuration — profile-specific configuration과 override precedence. https://docs.spring.io/spring-boot/reference/features/external-config.html
- OpenAPI Specification — API contract/schema 검증은 schema만으로 충분하지 않으며 specification/consumer behavior까지 확인. https://spec.openapis.org/oas/
- W3C WCAG 2.2 — ADM/Backoffice Frontend 접근성 검수의 보조 기준. https://www.w3.org/TR/WCAG22/
- Java Language Specification naming conventions — package/module naming의 충돌 방지와 가독성 보조 기준. https://docs.oracle.com/javase/specs/jls/
- JDK 25 `javadoc`/DocLint — 공개 API 문서 주석의 missing/reference/syntax/html/accessibility 문제를 fail-closed 보조 검수. https://docs.oracle.com/en/java/javase/25/docs/specs/man/javadoc.html

Harness는 위 출처 URL과 확인일을 Registry로 관리하며, 특정 외부 버전이 바뀌었다고 CPF 계약을 자동 변경하지 않는다. 변경 필요 시 Steering/Requirement로 명시적으로 currentize한다.
