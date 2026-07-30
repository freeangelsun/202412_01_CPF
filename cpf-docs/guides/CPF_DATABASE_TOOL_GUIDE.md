# CPF 데이터베이스 도구 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 데이터베이스 담당자, 이관 개발자, 설치 담당자
> **목적**: 세 데이터베이스의 설치·이관·업그레이드·되돌리기·백업·복구를 같은 생명주기로 수행한다.
> **관련 문서**: [데이터베이스 프로필과 업무영역 DB](DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-tools/db` |
| 이 문서로 완료하는 일 | Canonical DB Source에서 3개 Vendor Pack을 생성·검증하고 Fresh Install·Upgrade·Rollback·Reapply·Backup·Restore를 재현한다. |
| 적용 범위 | Canonical Schema/Seed, Vendor Source, Migration, Rollback, Runtime SQL, Checksums |
| 주요 독자 | DBA, DB Tool 개발자, Module Owner, Release 담당자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

이 문서는 CPF가 지원하는 Oracle, PostgreSQL, MariaDB의 설치, Seed, 이관, 되돌리기, Verify, 백업과 복원 도구를 설명한다.

## 2. 정본 구조

```text
cpf-tools/db/
├─ metadata/
├─ generated/
└─ vendor/
   ├─ mariadb/
   │  ├─ source/
   │  ├─ install/
   │  ├─ seed/
   │  ├─ migration/
   │  ├─ rollback/
   │  ├─ verify/
   │  └─ domain-template/
   ├─ postgresql/
   └─ oracle/
```

원칙:

- 공급자별 동일 Directory 구조
- Canonical 메타데이터 우선
- 생성 산출물 수동 수정 금지
- Table, Column, PK, FK, Index, 기본값, Identity, Comment 동등성
- 생성 업무영역 Template 동기화

## 3. 공식 공급자

| 공급자 | 식별자 | 기본 Port |
|---|---|---:|
| MariaDB | `mariadb` | 3306 |
| PostgreSQL | `postgresql` | 5432 |
| Oracle | `oracle` | 1521 |

MySQL, MSSQL, H2는 공식 제품 공급자가 아니다.

## 4. 소스 계획

`database-source-plan.json`은 SQL 역할과 포함 순서를 선언한다.

역할:

- provision
- empty install
- product seed
- optional sample
- test seed
- verify
- migration
- rollback

신규 SQL은 역할과 소유자를 먼저 등록한다.

## 5. Bundle

| 파일 | 목적 |
|---|---|
| `00_provision.sql` | User/스키마/권한 |
| `00_empty_install.sql` | 제품 Object |
| `00_product_seed.sql` | 필수 기준정보 |
| `00_optional_sample_seed.sql` | 선택 Sample |
| `00_test_seed.sql` | 격리 테스트 |
| `00_verify.sql` | Read-only 검증 |
| `00_all_install.sql` | Empty + Product |
| `00_all_install_and_smoke.sql` | 설치 + Verify |

## 6. FK 생성 순서

Canonical 스키마에서 FK 의존 대상 Graph를 만든다.

```text
Parent Table
→ Child Table
→ Index
→ FK
```

위상 정렬로 Table 생성 순서를 결정한다. Cycle 또는 존재하지 않는 Parent는 생성 전에 실패한다.

Spring 배치 메타데이터 Table도 같은 규칙을 적용한다.

## 7. PK 생성 정책

PK Strategy를 메타데이터에 선언한다.

- APPLICATION: 애플리케이션이 ID 생성
- IDENTITY: DB Identity/Auto Increment
- SEQUENCE: Sequence
- NATURAL: 업무 Key
- COMPOSITE: 복합 Key

공급자 변환:

| 논리 정책 | MariaDB | PostgreSQL | Oracle |
|---|---|---|---|
| IDENTITY | AUTO_INCREMENT | GENERATED ... AS IDENTITY | IDENTITY |
| SEQUENCE | Sequence 또는 애플리케이션 정책 | SEQUENCE | SEQUENCE |
| APPLICATION | 일반 Column | 일반 Column | 일반 Column |

저장소 Insert와 DDL 정책을 일치시킨다.

## 8. 문자열과 Null

Oracle은 빈 문자열을 Null로 처리한다. Optional Text의 논리 의미는 다음 중 하나로 정본화한다.

- Null
- 명시 Sentinel
- 별도 상태 Flag

`NOT NULL DEFAULT ''`에 의존하지 않는다. DTO, RowMapper, API와 화면도 같은 의미를 사용한다.

## 9. Bundle 생성

```powershell
pwsh -File .\cpf-tools\scripts\build-all-install-sql.ps1
```

전체 동기화:

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

수행 항목:

1. 이관 체크섬
2. Lifecycle Bundle
3. 스키마 명세서
4. 정본 불일치
5. 프로필
6. 생성 업무영역 동기화
7. 3 공급자 Parity

## 10. Fresh Install

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

절차:

1. 프로필 검증
2. 인증정보 참조 해석
3. Provision
4. Empty Install
5. Product Seed
6. Verify
7. 명세서 비교
8. 검증 증적

일부 Table만 존재하면 Partial Install로 실패한다.

## 11. Platform 이관

사전 계획:

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

적용:

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch `
  -Apply `
  -ConfirmApply `
  -ConfirmApplicationsStopped `
  -ConfirmRollbackReady `
  -ExpectedPlanSha256 <PLAN_SHA256> `
  -BackupManifestPath <MANIFEST_PATH>
```

## 12. 이관 안전장치

- 명시 버전 범위
- 체크섬
- 되돌리기 Pair
- 백업 명세서
- 실행 환경 중지 확인
- Plan 해시
- 정본 불일치 Precheck
- 잠금
- 실행 결과
- 재실행 정책

도구가 설치 버전을 추정하지 않는다.

## 13. 되돌리기

되돌리기는 단순 반대 DDL이 아니다.

검사:

- 데이터 손실
- 신규 Column 사용 여부
- Archive Row
- FK 관계
- 애플리케이션 호환성
- Sequence/Identity
- Seed
- Downstream

위험하면 안전하게 중단하고 수동 이관 계획을 요구한다.

## 14. Upgrade 검증

```text
이전 Version 설치
→ Backup
→ Upgrade Dry Run
→ Apply
→ Verify
→ Application Smoke
→ Rollback Dry Run
→ Rollback
→ Verify
→ Reapply
→ Verify
```

## 15. 정본 불일치

비교 대상:

- Table
- Column Type
- Length/Precision
- Nullability
- 기본값
- PK
- FK
- Index
- Unique
- Identity/Sequence
- Comment
- 소유자

차이가 있으면 Skip하지 않는다.

## 16. 생성 업무영역

생성 업무영역 DB는 자신의 프로필과 공급자를 가진다.

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -SystemCode PAY `
  -Operation bootstrap `
  -Apply
```

중앙 기준 템플릿에서 생성한다.

## 17. 실행 환경 SQL

공급자 선택은 프로필을 따른다.

```text
mybatis/vendor/mariadb/
mybatis/vendor/postgresql/
mybatis/vendor/oracle/
```

다른 공급자 SQL로 Fallback하지 않는다.

## 18. Seed

Product Seed:

- 멱등성
- 소유자 DB 내부
- 고객 데이터 미변경
- 테스트 Fixture 없음
- 비밀값 없음
- 버전
- 감사 필요 여부

Optional/테스트 Seed는 운영 설치에 자동 포함하지 않는다.

## 19. Verify

Read-only 검사:

- Object 존재
- Column/Type
- PK/FK/Index
- Seed
- 메타데이터
- 조회
- Identity Insert
- Comment
- 권한

## 20. 백업

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 `
  -Vendor postgresql `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 5432 `
  -User cpf_backup
```

명세서:

- 공급자
- DB
- 소스 Commit
- 시각
- 파일
- SHA-256
- 민감정보 분류
- 도구 버전

## 21. 복원

```powershell
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 `
  -Vendor postgresql `
  -Database admDB `
  -BackupFile .\backup.dump `
  -ConfirmRestore
```

공급자, DB, 체크섬 불일치를 거부한다.

## 22. DR Verify

```powershell
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 `
  -Database admDB `
  -Host 127.0.0.1 `
  -Port 5432 `
  -User cpf_verify
```

모든 논리 DB 복구 후 Platform Verify를 수행한다.

## 23. 인증정보

Password는 명령 Line, Plan, SQL 임시파일과 검증 증적에 넣지 않는다. OS 인증정보, 프로세스 Environment 또는 비밀값 공급자를 사용한다.

## 24. 장애 처리

| 장애 | 처리 |
|---|---|
| 연결 실패 | 환경/권한 분류 |
| Partial Install | 실패 후 수동 정리/복구 |
| 체크섬 불일치 | 적용 금지 |
| 잠금 실패 | 재시도 또는 운영 확인 |
| DDL 일부 적용 | 트랜잭션 가능 여부와 복구 Script |
| Disk Full | 중단, 공간 확보, 상태 확인 |
| 되돌리기 불가 | 수동 이관 계획 |

## 25. 검증 증적

- 공급자
- DB/스키마
- 도구 버전
- 소스 Commit
- 명령
- Plan 해시
- 백업 명세서
- 시작/종료
- Exit Code
- Verify 조회
- Sanitizing

## 26. 체크리스트

- [ ] Canonical 메타데이터에서 생성한다.
- [ ] FK 순서가 의존 대상 기반이다.
- [ ] PK Strategy가 공급자 간 일치한다.
- [ ] Oracle 빈 문자열 의미가 일치한다.
- [ ] Fresh/Upgrade/되돌리기/Reapply를 검증한다.
- [ ] 생성 업무영역 산출물이 동기화됐다.
- [ ] 정본 불일치를 Skip하지 않는다.
- [ ] 검증 증적이 소스 Commit과 일치한다.

## 부록 A. 공급자별 운영 차이

| 항목 | Oracle | PostgreSQL | MariaDB |
|---|---|---|---|
| 논리 소유 단위 | 사용자·스키마 | 데이터베이스·스키마 | 데이터베이스 |
| 자동 증가 | Identity·Sequence | Identity·Sequence | Auto Increment |
| 현재 시각 | `SYSTIMESTAMP` 등 | `CURRENT_TIMESTAMP` | `CURRENT_TIMESTAMP` |
| 대량 적재 | SQL*Loader·외부 테이블 등 | `COPY` | `LOAD DATA` 등 |
| 잠금·대기 조회 | 동적 성능 뷰 | 통계·잠금 뷰 | 정보 스키마·성능 스키마 |

정본은 논리 의미를 관리하고 공급자 산출물은 문법과 운영 차이를 명시한다.

## 부록 B. 변경 실행 순서

```text
계획 생성
→ 권한·공간·잠금 영향 검사
→ 백업과 복구 지점
→ 정본 불일치 검사
→ 이관 적용
→ 구조·자료·인덱스 검증
→ 애플리케이션 호환 점검
→ 되돌리기 시험
→ 재적용
→ 검증 증적
```

## 부록 C. 오류 분류

| 분류 | 예 | 조치 |
|---|---|---|
| 연결 | DNS, 포트, TLS, 인증 | 연결 단계별 진단 |
| 권한 | 생성·변경·조회 권한 부족 | 최소 권한 목록과 계정 확인 |
| 정본 불일치 | 열·형식·제약·인덱스 차이 | 자동 건너뛰기 금지, 차이 보고 |
| 잠금 | 장시간 DDL 대기 | 영향 세션·점검창·온라인 전략 확인 |
| 공간 | 자료·임시·로그 공간 부족 | 예상 증가량과 여유 확보 |
| 자료 | 변환 실패·중복·참조 무결성 | 사전 정제·격리·재처리 |
| 되돌리기 불가 | 새 자료가 구 구조에 맞지 않음 | 전진 수정 또는 연결 이관 |

## 부록 D. 백업 복구 검증

백업 성공 메시지만으로 완료하지 않는다. 별도 검증 환경에 복구하고 스키마 버전, 대표 원장 건수, 제약·인덱스, 애플리케이션 읽기·쓰기, 메시지 위치, 파일 참조와 배치 체크포인트를 확인한다.

## 31. Canonical Source에서 공급자 산출물까지

1. `cpf-tools/db/canonical/platform-schema.json`과 Seed Model에서 Owner·Column·Key·Index·Comment를 변경한다.
2. 정본 구조 검증으로 존재하지 않는 Column 참조, 중복 이름, 잘못된 Owner와 비호환 Type을 차단한다.
3. 공식 생성 도구로 Oracle·PostgreSQL·MariaDB Source·Migration·Rollback·Runtime SQL을 동기화한다.
4. 수동 공급자별 수정이 필요하면 정본에서 표현할 수 없는 차이를 명시하고 검증 규칙을 추가한다.
5. 공급자 Pack의 파일 목록·Checksum·Version을 갱신한다.
6. Fresh Install, Upgrade, Rollback, Reapply와 Drift 검사를 각 공급자에서 수행한다.
7. Mapper·Repository·DTO·Generator Template과 문서 영향을 함께 확인한다.

생성된 공급자 파일만 직접 고쳐 정본과 분리하지 않는다. 같은 의미의 SQL을 복사·치환한 것만으로 지원 완료로 판정하지 않는다.

## 32. Fresh Install 검증 시나리오

| 단계 | 확인 내용 |
|---|---|
| 빈 환경 | 대상 Schema·계정·권한과 Collation·Timezone 확인 |
| 설치 | Table·Sequence/Identity·Constraint·Index·Comment 생성 |
| Seed | 코드·권한·메뉴·정책의 중복 없는 적용 |
| Verify | 정본 객체 수, 필수 Column, PK/FK/Index와 Seed Version |
| Runtime | 대표 Repository의 Insert·Select·Update·Lock |
| 재실행 | 멱등 설치가 아니라면 명확한 Drift/Already Installed 오류 |
| 정리 | 실패 중간 상태 제거 또는 복구 절차 확인 |

## 33. Upgrade → Rollback → Reapply

1. 운영 Schema Snapshot과 Backup Manifest를 만든다.
2. 현재 Version에서 목표 Version까지의 Migration Plan과 Hash를 확정한다.
3. 정본과 현재 Schema 차이가 계획에 포함됐는지 확인한다.
4. Upgrade를 실행하고 각 Migration의 시작·종료·영향 행·오류를 기록한다.
5. Application 호환 Smoke와 데이터 정합성 Query를 수행한다.
6. Rollback 가능한 범위를 검토하고 승인된 Rollback을 실행한다.
7. 원 Version의 구조·데이터·Application Smoke를 확인한다.
8. 같은 Upgrade를 다시 적용해 재현성과 잔재 유무를 검증한다.
9. Rollback이 데이터 손실을 유발하면 실행하지 않고 Forward Fix 또는 복구 계획으로 전환한다.

## 34. 공급자 차이 검토표

| 항목 | Oracle | PostgreSQL | MariaDB |
|---|---|---|---|
| 자동 증가 | Sequence·Identity 정책 | Identity·Sequence | Auto Increment |
| Boolean | 제품 표준 값으로 매핑 | Native Boolean 가능 | 제품 표준 값 또는 Boolean Alias |
| 시간 | Session Timezone 주의 | `timestamp with time zone` 의미 확인 | Fraction·Timezone 정책 확인 |
| 대용량 문자열 | CLOB | TEXT | LONGTEXT/TEXT |
| Lock·Skip | 지원 문법과 격리 수준 확인 | `SKIP LOCKED` 의미 확인 | Engine·Version별 동작 확인 |
| Upsert | MERGE 정책 | `ON CONFLICT` | `ON DUPLICATE KEY` |

표면 문법만 맞추지 말고 Transaction, Lock, Index 선택도, Identity 재적용과 Rollback 의미를 실제 공급자에서 검증한다.

## 35. V81 실행·운영 정합성 변경

기준 Commit의 V81 Migration은 단일 기능 SQL이 아니라 Gateway·ADM·Batch 실행 계약을 함께 맞춘다.

| Owner DB | 핵심 변경 | 함께 확인할 Source |
|---|---|---|
| CPF/Gateway | 대상 경로 분리, 제어 Nonce·보안 감사 | `CpfGatewayRoute`, `CpfGatewayPathRewriter`, `CpfGatewayControlNoncePort` |
| ADM | 다중 인스턴스 공용 로그 Export Artifact | `AdmLogExportService`, `adm_log_export_artifact` |
| BAT | Attempt 상세·결과 불명·재시도 정보 | `JdbcWorkerExecutionRepository`, `worker-attempt-*.sql` |

Oracle·PostgreSQL·MariaDB의 `V81`과 대응 `R81`을 모두 확인한다. Fresh Install Source에도 같은 최종 구조가 반영돼야 하며 Migration만 존재하거나 Source만 갱신된 상태를 완료로 처리하지 않는다.

```powershell
Get-ChildItem .\cpf-tools\db\vendor -Recurse -File |
  Where-Object Name -Match '^[VR]81__' |
  Select-Object FullName
```

검증은 다음 Drift를 차단해야 한다.

- `target_path` 또는 동등 Column이 Route Provider·Mapper와 불일치
- Nonce Unique Key가 Audience·Key·Caller 범위를 보장하지 못함
- 로그 Export 본문·소유자·만료·상태 Column 누락
- Batch Attempt 완료 SQL이 실패 코드·메시지·결과 불명 정보를 잃음
- 공급자별 Rollback이 새 Table·Column·Index를 일부 남김

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Canonical Schema | `cpf-tools/db/canonical/platform-schema.json` | Table·Column·PK·FK·Index 정본 |
| Canonical Seed | `cpf-tools/db/canonical/seed-model.json` | 제품 Seed 정본 |
| Vendor Pack | `cpf-tools/db/vendor/{oracle,postgresql,mariadb}` | Source·V81 Migration·R81 Rollback·Runtime SQL |
| Generation | `cpf-tools/scripts/generate-official-db-vendor-source.ps1` | 공식 Vendor Source 생성 |
| Static Gate | `check-canonical-db-lifecycle-contract.ps1`, `check-canonical-ddl-safety.ps1` | 생명주기·DDL 안전 검사 |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
