# CPF Migration Guide

## 1. Purpose

이 문서는 CPF Version Upgrade와 구조 변경을 안전하게 적용하고 Rollback하는 절차를 설명합니다.

## 2. Compatibility Areas

- Java Public API
- SPI
- REST API
- Message
- File format
- DB schema
- Configuration
- Module name
- Package
- SystemCode
- Route
- Queue·Topic
- Admin menu·permission

## 3. Upgrade Planning

1. 현재 Version과 Commit 확인
2. Target Release Notes 검토
3. Breaking change 식별
4. DB·Config·Secret diff
5. Consumer 영향 분석
6. Migration dry-run
7. backup
8. rollback plan
9. 운영 승인
10. staging rehearsal

### cpf-core 시스템 코드 전환

`cpf-core`의 공식 시스템 코드는 `CPF`입니다. 정식 출시 전 기준선을 재확정하면서
DB는 `cpfDB`, 프레임워크 테이블은 `cpf_*`, 메시지와 응답 코드는
`MCPF...`·`SCPF...`·`ECPF...`로 통일했습니다. 이전 개발 DB는 업그레이드
대상으로 간주하지 않고 백업 후 폐기한 다음 현행 설치 SQL로 다시 구성합니다.
정식 출시 이후에는 기준 migration을 수정하지 않고 새 버전의 expand-contract
migration과 rollback을 추가합니다.

## 4. Module and Package Migration

공식 구조:

```text
cpf-core        com.cpf.core
cpf-gateway     com.cpf.gateway
cpf-common      com.cpf.common
cpf-admin       com.cpf.admin
cpf-biz-admin   com.cpf.bizadmin
cpf-batch       com.cpf.batch
cpf-member      com.cpf.member
cpf-account     com.cpf.account
cpf-reference   com.cpf.reference
cpf-external    com.cpf.external
```

문자열 치환만으로 완료하지 않습니다.

- Gradle coordinates
- import
- component scan
- reflection
- serialization
- MyBatis
- config prefix
- route
- SQL
- frontend
- script
- tests

를 함께 검증합니다.

## 5. Database Migration

### Expand and Contract

1. 새 Column·Table 추가
2. 양 버전 호환 Application 배포
3. Backfill
4. 신규 Field 사용 전환
5. 구 Field 읽기 중단
6. 후속 Release에서 제거

### Large Data

- chunk
- checkpoint
- throttling
- lock time
- replication lag
- restart
- progress
- rollback

## 6. Configuration Migration

모든 변경은 다음을 제공합니다.

- old key
- new key
- default
- required 여부
- value conversion
- secret 여부
- removal version

Deprecated key는 경고와 migration 메시지를 제공합니다.

## 7. Message and File Compatibility

- Schema version
- producer-first 또는 consumer-first 순서
- unknown field
- enum 추가
- dual read/write
- replay
- retention

## 8. Runtime Upgrade

1. DB compatible migration
2. control plane
3. business services
4. batch workers
5. gateway
6. admin frontend
7. smoke
8. traffic open

## 9. Rollback Decision

Rollback 기준:

- 핵심 거래 실패
- data corruption 위험
- unknown result 급증
- latency 임계 초과
- security regression
- migration failure
- worker duplicate

DB destructive migration이 적용되었으면 Application rollback보다 forward-fix가 안전할 수 있습니다.

## 10. Verification

- build
- startup
- schema version
- core transactions
- local/remote
- batch/worker
- external
- log/audit
- admin
- compatibility consumer
- rollback smoke

## 11. Migration Evidence

- from/to version
- Commit
- environment
- command
- DB version
- rows affected
- duration
- validation
- rollback point
- result
