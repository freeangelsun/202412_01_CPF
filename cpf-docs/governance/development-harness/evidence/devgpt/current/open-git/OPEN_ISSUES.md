# CPF Open Git Release — Open Issues

## OG-INT-001 — cpf-core sources.jar 정책 불일치

- 상태: `OPEN / INTEGRATION_BLOCKER`
- 현재: Artifact Catalog의 `cpf-core`가 `publishSources=true`.
- Requirement: Core 내부 구현은 Open Git Source disclosure 금지, binary-only가 기본.
- 처리: Codex Final Source에서 Catalog/Publication 계약을 다시 확인하고 binary-only로 맞춘 뒤 Open Git Artifact Gate 재실행.
- 본 Overlay에서는 Codex가 수정할 가능성이 높은 Catalog/Gradle을 변경하지 않는다.

## OG-INT-002 — cpf-admin Public Binary Publication 계약 부재

- 상태: `OPEN / INTEGRATION_BLOCKER`
- 현재: `cpf-admin`에 Public `publicationClass`, `publicGroupId`가 Canonical Artifact Catalog에 없다.
- Requirement: ADM Source/sources.jar를 공개하지 않고 필요한 Runtime은 Binary로 사용 가능해야 한다.
- 금지: 임의 Maven 좌표 생성, ADM Source 공개로 우회.
- 처리: Codex 종료 후 실제 Owner/Publication 계약에 맞춰 Canonical Catalog와 Publication을 정식 연결.

## OG-INT-003 — cpf-gateway Public Binary Publication 계약 부재

- 상태: `OPEN / INTEGRATION_BLOCKER`
- 현재: `cpf-gateway`에 Public `publicationClass`, `publicGroupId`가 없다.
- Requirement: Gateway Source/sources.jar 비공개 + 필요한 Binary 제공.
- 처리: OG-INT-002와 같은 Canonical Artifact Publication closure 수행.

## OG-ENV-001 — Java25 / Windows / 실제 Open Git Remote Final Gate

- 상태: `NOT_EXECUTED`
- 현재 환경: Java 21, PowerShell Runtime 없음.
- 필요 검증: Java25 Root Gradle, Binary Publication, Windows PowerShell entrypoint, 실제 Open Git Remote fresh clone, Open Git workspace build/test, staged diff.
- 성공조건: `READY_TO_COMMIT`, fail 0, forbidden source/source-jar leakage 0.
- commit/push는 성공조건에 포함하지 않으며 사용자만 수행한다.
