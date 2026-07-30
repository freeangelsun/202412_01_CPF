# CPF OSS 라이선스·공급망 표준

## 1. 기본 원칙

- OSS 도입은 정확한 Coordinate, Version, License, Source URL과 Distribution 형태를 기준으로 승인한다.
- 프로젝트 이름만 보고 라이선스를 추정하지 않는다.
- 직접 의존성뿐 아니라 전이 의존성, npm optional/peer dependency, shaded/bundled binary, OS package, generated client를 포함한다.
- Release Artifact에 실제 포함된 구성은 Source Dependency Graph와 별도로 검증한다.

## 2. 정책 등급

| 등급 | 기본 처리 | 예시 |
|---|---|---|
| A | 자동 허용 가능 | MIT, Apache-2.0, BSD-2-Clause, BSD-3-Clause, ISC |
| B | 배포·수정·Link 방식별 별도 승인 | MPL-2.0, EPL-2.0, LGPL 계열 |
| C | 기본 거부 | GPL, AGPL, SSPL, RSAL, BSL, Source-available/Custom, Unknown, NOASSERTION |

법무 또는 Governance가 승인한 예외는 Artifact, Version 범위, 사용 방식, 만료일, Owner를 기록한다.

## 3. 도구별 역할

- CycloneDX: Gradle/npm의 resolved dependency graph SBOM
- ORT: License detection, policy, curation, attribution/NOTICE, source/compliance
- Syft: 최종 JAR/ZIP/Container/Filesystem 실제 package inventory
- Grype: 최종 Artifact vulnerability scan

어느 한 도구의 PASS만으로 Release를 허용하지 않는다.

## 4. 상호 대조 Gate

다음을 Fail-closed로 검사한다.

- CycloneDX에는 없지만 Syft Final Artifact에는 있는 Package
- ORT가 Unknown/NOASSERTION으로 판정한 Package
- 금지 License 또는 승인 범위를 벗어난 Version
- Shaded/Fat JAR/Frontend Bundle에 숨은 Package
- NOTICE와 License Text 누락
- Source URL·PURL·CPE·Version 불일치
- Critical/High CVE의 미승인 잔존
- 개발 전용 Package가 Production Artifact에 포함
- 금지된 Server Binary 또는 Commercial Artifact 번들

## 5. 승인 Stack 특칙

- PrimeVue 최신 및 유료 PrimeVue 자산은 기본 금지한다.
- Kafka는 Apache Kafka OSS Artifact와 승인 Client만 허용한다.
- Redis Server는 기본 번들하지 않는다. Valkey는 선택 Provider로만 취급한다.
- Flyway는 OSS Core와 승인된 OSS DB Module만 허용하고 Teams/Enterprise Artifact는 차단한다.
- HashiCorp Vault Server를 기본 번들하지 않는다. SecretProvider SPI로 고객 환경에 연결한다.
- Flowable은 Apache-2.0 Engine이더라도 실제 Workflow 요구 ADR 없이는 도입하지 않는다.
- Spring Cloud Gateway는 Server Web MVC만 승인한다. WebFlux/Netty Artifact는 현재 금지한다.

## 6. Version 관리

- Gradle/npm/Node/Java/PowerShell Toolchain을 exact 또는 승인 범위로 고정한다.
- Dependency Lock과 npm lockfile을 Release Gate 입력으로 사용한다.
- Major Upgrade는 라이선스 재검토를 필수로 한다.
- License 변경 여부를 자동 탐지하고, 기존 허용 결과를 무기한 승계하지 않는다.

## 7. Release 증적

Release마다 다음을 보관한다.

- exact Source SHA와 Repository Identity
- Dependency Lock과 npm lockfile hash
- CycloneDX SBOM
- ORT Analyzer/Scanner/Evaluator/Reporter 결과
- Syft Final Artifact SBOM
- Grype Report
- THIRD-PARTY-NOTICES 및 License Text
- 예외 승인 목록
- 최종 Artifact Hash·Signature·Provenance
