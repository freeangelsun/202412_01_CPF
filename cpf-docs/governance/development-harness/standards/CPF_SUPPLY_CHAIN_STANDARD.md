> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF OSS 라이선스·공급망 표준

> Canonical path: `cpf-docs/governance/development-harness/standards/CPF_SUPPLY_CHAIN_STANDARD.md`
> Final Target synchronization: `2026-08-08`
> Central currentization basis: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)

## 1. 목적

CPF가 도입·빌드·배포하는 OSS와 외부 Binary의 License, Notice, Source 의무, 취약점, 무결성과 provenance를 **실제 최종 배포 Artifact** 기준으로 통제한다.

Project 이름이나 Source dependency 목록만으로 승인하지 않는다.

## 2. 적용 범위

- Gradle 직접·전이 의존성
- Included Build, Plugin, BOM과 Convention Plugin
- npm direct/dev/optional/peer/transitive dependency
- generated OpenAPI client와 code generator
- shaded/fat JAR, embedded JAR/WAR library
- Frontend bundle과 bundled asset/font/icon
- Native binary, OS package, Script tool
- Docker image와 filesystem layer
- Offline Maven/npm bundle
- Agent/Worker/Gateway/ADM/BZA 최종 Artifact
- License text, NOTICE, attribution과 source offer
- Build·Scan 도구 자체

## 3. 정책 등급

| 등급 | 기본 처리 | 예시 |
|---|---|---|
| A | 조건 확인 후 자동 허용 가능 | MIT, Apache-2.0, BSD-2-Clause, BSD-3-Clause, ISC |
| B | 배포·수정·link·network·source 제공 방식별 서면 승인 | MPL-2.0, EPL-2.0, LGPL 계열 |
| C | 기본 거부 | GPL, AGPL, SSPL, RSAL, BSL/BUSL, Source-available/Custom, Unknown, NOASSERTION |

A 등급도 정확한 Artifact와 의무 확인 없이 자동 허용하지 않는다.

예외 승인 필수 필드:

- component PURL/coordinate
- exact version/range
- license expression
- 사용·수정·link·배포 방식
- 대상 Edition/Artifact
- Owner
- 법무/보안 승인자
- 시작·만료일
- NOTICE/source 의무
- 대체·제거 계획

## 4. 승인 Component 정본

`cpf-tools/supply-chain/approved-primary-oss.csv` 또는 후속 정본은 최소 다음을 가진다.

- component
- ecosystem
- PURL
- group/name
- exact version
- license expression
- source URL
- binary/source hash
- intended module/consumer
- distribution form
- approval class
- exception reference
- owner
- review date/expiry

SBOM의 모든 Production Component가 승인 정본에 존재해야 하며 승인 정본의 Product Component도 최종 Artifact에서 발견되거나 선택 기능 제외 근거가 있어야 한다.

## 5. 도구별 역할

| 도구 | 역할 | 완료에 필요한 결과 |
|---|---|---|
| CycloneDX | Gradle/npm resolved graph SBOM | exact Source SHA·lock 기반 dependency graph |
| ORT | analyzer, scanner/curation, evaluator, reporter | policy result, attribution, NOTICE/source obligation |
| Syft | 최종 JAR/WAR/ZIP/static bundle/container/filesystem package inventory | 배포 대상 Artifact별 SBOM |
| Grype | Syft SBOM 또는 최종 Artifact vulnerability scan | CVE policy와 exception 결과 |
| Secret/Integrity Gate | credential, private key, token, tamper 검사 | sanitized report |

`ORT analyze`만 실행하거나 `syft dir:.`만 실행한 결과는 Release Evidence가 아니다.

## 6. 동일 입력 정체성

모든 Supply-chain Evidence는 다음을 공유해야 한다.

- repository
- source SHA
- clean tree
- build invocation
- final artifact path
- final artifact SHA-256
- dependency lock hash
- npm lock hash
- tool name/version/binary hash
- policy/config hash
- startedAt/finishedAt
- report hash

CycloneDX·ORT·Syft·Grype가 서로 다른 Build나 Artifact를 검사하면 fail-closed한다.

## 7. 최종 Artifact 단위 검사

각 배포 단위를 별도 검사한다.

- `cpf-core`/`cpf-common` Public JAR
- Starter JAR
- `cpf-gateway.jar`
- ADM/BZA Backend Artifact
- ADM/BZA Static Bundle
- Batch Control/Worker/Agent/Runner Artifact
- Generated Domain BootJar/BootWar
- Offline Maven Bundle
- Container Image가 지원될 경우 Image Digest

Source directory 전체 Scan은 보조 정보일 뿐 최종 Artifact Scan을 대체하지 않는다.

## 8. 양방향 대조 Gate

Fail-closed 검사:

- CycloneDX에는 없지만 Syft final artifact에는 있는 package
- resolved graph에는 있지만 final artifact에 누락된 필수 runtime package
- 승인 정본에 없는 component/version
- 승인됐지만 다른 hash/PURL/source
- ORT Unknown/NOASSERTION
- 금지 license
- 조건부 license의 승인·NOTICE/source 의무 누락
- shaded/fat JAR와 Frontend bundle의 숨은 package
- duplicate/multiple version과 dependency convergence 위반
- dev/test package의 Production 포함
- 금지 Server Binary 또는 Commercial Artifact
- License/NOTICE text 누락
- Critical/High CVE 미승인 잔존
- End-of-life component
- generated client/tool의 license 누락
- SBOM serial/source/artifact identity 불일치

## 9. 취약점 정책

취약점 판정은 CVSS 숫자만으로 자동 결정하지 않는다.

필수:

- severity
- exploitability
- reachable code
- deployment exposure
- fixed version
- compensating control
- exception owner/expiry
- 재검토 일정

기본 정책:

- Critical: Release 차단
- High: Release 차단, 서면 기한부 예외만 허용
- Medium/Low: 위험 기반 정책과 기한
- KEV/실제 악용: severity와 무관하게 긴급 차단 검토

Scan DB와 tool version을 Evidence에 기록한다.

## 10. 승인 Stack 특칙

- UI: Element Plus + TanStack Table. PrimeVue 최신/유료 Asset 기본 금지.
- Gateway: Spring Cloud Gateway Server Web MVC. WebFlux/Netty Gateway Artifact 현재 금지.
- Messaging: Apache Kafka Product Primary. AMQP/RabbitMQ/Artemis Primary 병행 금지.
- Session: Spring Session JDBC. Redis Server 기본 번들 금지.
- Distributed Cache: Valkey-compatible 선택 Provider. Server Binary 번들 금지.
- DB Migration: Flyway OSS Core와 승인 OSS module만. Teams/Enterprise 금지.
- Secret: CPF SecretProvider SPI로 고객 관리 Service 연결. Vault Server 번들 금지.
- Workflow: Flowable OSS는 별도 ADR threshold 통과 시에만.
- Observability: OTel SDK/exporter는 Starter가 소유하며 `cpf-core` Public API에 노출 금지.

정확한 version은 `gradle/cpf-stack.properties`, BOM, npm lock와 approved component 정본을 따른다.

## 11. Build·Repository 정책

- Maven Central/Plugin Portal/npm registry 접근은 승인 mode와 lock으로 통제한다.
- `REMOTE`/`OFFLINE`이 실패해도 local developer repository로 fallback하지 않는다.
- Wrapper distribution과 checksum을 pin한다.
- Lockfile 없는 dynamic/range/SNAPSHOT Production dependency를 금지한다.
- Generated Domain도 동일 BOM/Plugin/Lock/SBOM 정책을 사용한다.
- Build cache가 다른 dependency graph를 숨기지 않도록 clean/fresh 검증을 수행한다.
- Repository credential과 token을 log/evidence에 출력하지 않는다.

## 12. NOTICE·Source 의무

Release Artifact마다 다음을 생성·검증한다.

- THIRD_PARTY_NOTICES
- License text
- Copyright/attribution
- Source URL
- 수정 여부
- 조건부 license 의무
- source offer/archive 또는 제공 절차
- component와 Notice 양방향 연결

Notice에 없는 Production Component 또는 component가 없는 Notice 항목은 검토 대상으로 실패한다.

## 13. Release Evidence

- exact Source SHA
- final Artifact 목록과 SHA-256
- lockfile/BOM/POM hash
- CycloneDX SBOM
- ORT analyzer/evaluator/reporter
- Syft Artifact별 SBOM
- Grype report
- approved component reconciliation
- exception register
- THIRD_PARTY_NOTICES와 License text
- source obligation 결과
- signature/provenance
- tool/config/binary hash
- sanitized scan
- 최종 판정

## 14. 완료 금지

- dependency file만 검토
- Source directory만 Syft scan
- ORT analyze만 실행
- `license` 문자열만 비교
- direct dependency만 검사
- final artifact hash 없음
- 서로 다른 Build 결과 조합
- conditional license 승인 누락
- Unknown/NOASSERTION을 무시
- NOTICE/source 의무 미검증
- CVE exception의 Owner/만료일 없음
- 실행하지 않은 Tool을 PASS로 기록

## 15. 관련 Canonical Requirement

- `RULE-SEC`
- `RULE-QUALITY`
- `REL-BUILD`
- `REL-DEPLOY`
- `REL-COMPAT`
- `SEC-APP`
- `SEC-SECRET`
- `PROD-EDITION`
- `PROD-PACKAGE`
- `TEST-EVIDENCE`
