# ADR-20260726 — BAT Standalone Runtime / Domain Federation

## 결정

`Source Repository != Build Unit != Deployment Cell != Runtime Instance`를 Framework 원칙으로 확정한다.

BAT는 같은 CPF Repository 안에서 5개 Standalone 실행 Artifact와 3개 Library로 분리한다.
업무 Domain은 별도 Repository가 기본이며 게시된 Public API/SPI/BOM을 사용한다.

## 이유

- Scheduler/Worker/Center-Cut/Agent 독립 Scale/Failover/Upgrade/Rollback
- 특정 Domain 변경이 플랫폼 전체 Build/배포를 강제하는 결합 제거
- Source 권한과 Release 권한 분리
- Instance 단위 Health/Version/Capability/Fencing/Deployment 추적
- 동일 JVM과 분리 WAS에서 같은 Owner Contract 유지

## 원격 운영 결정

ADM은 Host에 SSH 자유 명령을 실행하지 않는다.
`ADM Approval -> BAT Control Server -> Host Agent Approved Catalog`만 허용한다.
Artifact는 checksum/signature 검증 후 immutable release directory에 설치한다.

## 회귀 보호

기존 `CpfBatchOperationsPort`와 `/bat/internal/operations` 계약은 Control Server에 호환 구현을 제공한다.
기존 BAT Retention도 Control Server Owner로 이관한다.
따라서 최종 Overlay 적용 후 Legacy `cpf-batch/src`를 제거해도 ADM 운영 계약을 유지한다.
