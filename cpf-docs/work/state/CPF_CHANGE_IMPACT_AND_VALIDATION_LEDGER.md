# CPF Change Impact and Validation Ledger

이 문서는 작업마다 초기화하지 않는 누적 변경/검증 Ledger다.

## 상태 정의

- `구현`: Source 변경 완료
- `정적 검토`: 파일/계약 수준 확인
- `검증 완료`: 실제 명령 실행 Evidence 존재
- `부분 검증`: 일부만 실행
- `미검증`: 실행하지 않음
- `재검증 필요`: 이후 변경으로 기존 Evidence 영향 가능

## CHG-20260727-ARTIFACT-001 — Local Artifact Federation

| 항목 | 내용 |
|---|---|
| Requirement | CPF-BUILD-LOCAL-001, BUILD-001, 독립 Domain Build/Artifact Federation |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | Root Build / `cpf-tools/build` / `cpf-tools/generator` |
| 문제 | 독립 Generated Domain은 `CPF_ARTIFACT_REPOSITORY_URL`이 없으면 Maven Central만 사용하여 로컬에서 막 빌드한 CPF Core/Common/BOM/Plugin을 자동 소비할 수 없었음 |
| 목표 | 성공한 CPF build → shared local Maven repo → Generator/독립 Domain → bootJar/bootWar까지 동일 public artifact 자동 전파 |
| 변경 | shared local repo, explicit local publish task, 성공 build 확인 후 auto-sync wrapper, Generator pre-publish, standalone repository local resolution, convention plugin 적용, bootJar/bootWar package guard |
| 직접 파일 | `build.gradle`, `cpf-batch/build.gradle`, `cpf-tools/build/*`, `cpf-tools/generator/*`, `cpf-tools/scripts/verify-local-artifact-propagation.ps1` |
| Consumer | Generated Domain, 독립 Domain Repository, BAT public contract consumer, external WAS WAR consumer |
| DB 영향 | 없음 |
| Generator 영향 | 있음 — published-artifact 생성/Export 경로 변경 |
| 배포 영향 | 있음 — bootJar/bootWar public dependency 포함 Gate 강화 |
| 회귀 위험 | publication task 이름, included-build task wiring, local/remote repository precedence |
| ChatGPT 검증 | 정적 문자열/괄호/파일 구조 검토만 수행 |
| 미실행 | Gradle publish, Generated Domain standalone build, bootJar/bootWar ZIP 실제 확인 |
| Codex 집중검증 | `verifyCpfLocalArtifactPropagation`, PAY/INS 또는 임시 2 Domain 생성/독립 build/package |
| 상태 | 구현 / 실행 미검증 |

## CHG-20260727-CONTACT-001 — ADM/BZA Contact Semantics

| 항목 | 내용 |
|---|---|
| Requirement | ADM-003, BZA-001, BZA-006, UI 공통, 사용자 추가 연락처 요구 |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | `cpf-admin`, `cpf-biz-admin`, MariaDB canonical DB |
| 문제 | BZA mobile은 존재하나 UI 노출/명칭 부족, 내부 전화번호 없음. ADM 운영자는 연락처 모델 자체 없음 |
| 목표 | `연락처(휴대폰)`과 `내부 전화번호` 분리, 문자열 저장, 선택값 null, ADM 인증 Identity와 Directory Profile 저장 책임 분리 |
| 변경 | ADM DTO/API projection + `adm_operator_profile` persistence + UI, BZA employee service/repository/UI, generic table label, 계약 회귀 Test, canonical schema, V59/rollback |
| DB 영향 | `adm_operator_profile.MOBILE_NO`, `adm_operator_profile.OFFICE_PHONE_NO`, `bza_employee.office_phone_no`; `adm_operator` Identity에는 연락처 미저장 |
| Migration | `V59__admin_contact_model.sql` |
| Rollback | `V59__admin_contact_model_rollback.sql` |
| Generator 영향 | Generated Domain 없음. Platform DB canonical artifact sync 영향 있음 |
| Vendor 영향 | MariaDB canonical 변경. Candidate 4 Vendor 실DB source는 현재 완결되지 않아 임의 SQL 복제 금지 |
| 보안 영향 | 연락처 개인정보. 로그/Evidence 원문 노출 금지, 후속 masking/download 권한 검증 필요 |
| 역호환 | Java record에 구 생성자 호환 overload 유지 |
| ChatGPT 검증 | 정적 parity gate 작성, Source/SQL 구조 검토 |
| 미실행 | Java compile/test, Frontend build, DB sync, MariaDB V59 lifecycle |
| Codex 집중검증 | ADM/BZA module test + frontend + V59 upgrade/rollback/reapply |
| 상태 | 구현 / 실행 미검증 |

## CHG-20260727-BZA-DEFAULT-001 — Employee Safe Default

| 항목 | 내용 |
|---|---|
| Requirement | BZA-005, DEF-003 |
| 기준 SHA | `fb95e15f90856adcff39040a50b128aa40f5ef43` |
| Owner | `cpf-biz-admin`, MariaDB canonical DB |
| 문제 | QA는 직원 재직 기본값 `EMPLOYED`를 요구하지만 Backend/DDL은 `ACTIVE`였음 |
| 목표 | 기존 Row는 유지하고 신규 미입력 Default만 `EMPLOYED`로 정렬 |
| 변경 | Service default, canonical DDL default, V60 forward/rollback, static gate |
| DB 영향 | `bza_employee.employment_status` default만 변경; 기존 데이터 update 없음 |
| Migration | `V60__bza_employee_safe_defaults.sql` |
| Rollback | `V60__bza_employee_safe_defaults_rollback.sql` |
| ChatGPT 검증 | Source/checksum 정적 검토 |
| 미실행 | BZA compile/test, DB sync, MariaDB V60 lifecycle |
| Codex 집중검증 | `check-bza-safe-defaults.ps1`, BZA test, V60 upgrade/rollback/reapply |
| 상태 | 구현 / 실행 미검증 |

## REV-20260727-CODEX-001 — 이전 Codex 결과 재분류

| 항목 | 판정 |
|---|---|
| BAT 158 query pack | 이전 실제 PREPARE 완료, 이번 변경 영향 없음 |
| V58 lifecycle | 이전 실제 upgrade/rollback/reapply 완료, 이번 V59/V60과 별개 |
| MBR/ADM/BZA/REF/ACC/GWY 단일 Runtime | 이전 실제 boot/probe 기록 존재 |
| Build tooling relocation | Source/경로 이동 확인됨, 이번 artifact federation 변경 때문에 해당 부분만 재검증 |
| ADM MBR 결합 제거 | 최신 master에서 삭제 확인 |
| REF MBR service-call 제거 | Echo 대체 파일 확인, dependency-0 focused gate 필요 |
| MBR/ACC Golden parity | 실패/미완료 유지 |
| Browser/Multi-instance | 미검증 유지 |

## Codex 판정 기록 규칙

Codex는 위 Change ID별로 `확인`, `보완`, `반려` 중 하나를 기록한다.
전체 Repository를 다시 처음부터 조사하지 말고, 변경 파일의 실제 Consumer가 Ledger보다 넓을 때만 범위를 확장한다.
