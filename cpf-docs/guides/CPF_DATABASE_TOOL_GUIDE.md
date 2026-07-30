# CPF 데이터베이스 도구 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 데이터베이스 담당자, 이관 개발자, 설치 담당자
> **목적**: 세 데이터베이스의 설치·이관·업그레이드·되돌리기·백업·복구를 같은 생명주기로 수행한다.
> **관련 문서**: [데이터베이스 프로필과 업무영역 DB](DATABASE_PROFILE_AND_DOMAIN_DB_GUIDE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

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
