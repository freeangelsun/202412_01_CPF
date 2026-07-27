# CPF 미검증 시나리오 목록 — 20260727_04

기준 시작 SHA: `702bf83580b9c4db2dbba6482ece233e00842f1b`

이 목록은 실패 목록이 아니라 **실제 실행 Evidence가 아직 없는 항목**이다. Source/정적 검색으로 PASS 처리하지 않는다.

## Stack / Build

- Java 25 + Gradle 9.1 전체 configuration
- `clean test assemble`
- Included BOM/Gradle Plugin `check`
- `aggregateQualityBuild`
- Spring Boot 4.x Migration
- External WAS/Servlet 6.1

## Artifact Local

- staging publication 실제 실행
- POM/module/BOM/plugin marker 검증 실제 실행
- Shared Local promotion
- promotion 중 강제 실패 rollback
- 기존 promoted set 보존
- concurrent publisher
- consumer가 promotion 중 시작하는 race
- current HEAD manifest 재사용
- stale source SHA 재publish

## Artifact Remote

- Nexus/Artifactory authentication
- unauthorized
- timeout/retry
- partial upload
- duplicate immutable release
- Snapshot policy
- server-side staging/promotion
- credential rotation
- Local Repository side effect 0

## Artifact Offline

- Bundle 생성
- 다른 PC/서버 압축 해제
- Standalone Plugin resolution
- CPF BOM/Core/Common resolution
- bootJar/bootWar
- corrupt checksum/manifest failure

## Generated Domain

- 임시 Domain 1 create/export/build/package/remove
- 임시 Domain 2 동일 반복
- Local/Remote/Offline 각 Mode
- bootJar/bootWar exact CPF JAR hash

## 이전 기능 재검증

이번 Build/Generator 변경 영향으로 재개방:

- 전체 compile/test
- ADM/BZA/REF/ACC/GWY package
- Included Build
- Generated Domain build

직접 영향이 없어 즉시 반복하지 않는 항목:

- BAT 158 Query PREPARE
- V58 SQL lifecycle

단 최종 clean/historical migration 범위가 변경되면 다시 연다.

## BAT Runtime

- Scheduler 2개 leader loss/takeover
- Misfire/catch-up
- DST/timezone
- Restart vs Rerun Spring Batch instance identity
- 대량 Schedule dispatch

## 후속 Change Set

- ADM DB transaction/fail-closed
- PII masking/audit
- V59/V60 lifecycle
- BZA Status Catalog/SQL boundary
- Gateway fault
- Browser
- Multi-instance
