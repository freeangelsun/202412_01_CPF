# CPF 개발/QA 세션 인수인계 — 2026-08-17 Session Close

## 1. 기준
- 사용자 제공 최종 입력: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260817_163246.zip`
- Baseline marker: `4b6f96796c3bf26b1c3324cc4d9b701bd9415acd`
- 최신 통합 개발요청/Steering을 과거 중간 정본보다 우선 적용
- Java 공식 기준: **Java 25**. GPT 환경에서만 Java 21 대체 검증
- 사용자 승인 없는 commit/push/branch/reset/restore/clean/history 변경: 없음

## 2. 이번 세션에서 반영한 핵심
- `CpfOnlineTransaction` Canonical 정의 하나로 통합하고 `operationId + name + description` 중심으로 정리
- `operationId = OpenAPI operationId = X-Target-Operation-Id = Domain Client/ADM/Log` 단일 정본 방향 반영
- `CpfRestController`, `CpfTransactional`, `CpfPreAuthorize`, `CpfTimed`, `CpfTimeLimiter`, `CpfRestClient` 등 OSS/Spring naming currentization 및 구 Alias/중복 제거
- ADM/BZA/Gateway 관리 Controller는 업무 Domain Online Transaction이 아니라는 경계 반영; 업무 Domain outbound부터 거래 Context 적용
- Runtime transaction catalog 자동 bootstrap, Catalog/Policy ownership 분리, Source scan이 ADM enabled/Policy를 덮지 않도록 보정
- 신규 Operation YML default caller Seed 최초 1회, ALL semantics 및 Caller→Operation Runtime enforcement 보강
- Channel Policy Store 장애 wildcard ALLOW fallback 제거, LKG/maxStale/fail-close 경로 보강
- instanceId canonical key를 `cpf.runtime.instance-id` / `CPF_RUNTIME_INSTANCE_ID`로 통일
- Generator: online 필수 + batch 선택. member=online+batch, external=online-only 케이스 반영
- Fixed Length/Webhook/RestClient: 업무 Public Contract → Internal/Provider 구현 의존 방향 정리
- EDU: 기존 135 체계를 제거 대상으로 전환하고 `cpf-education/.../online` 20 + `.../batch` 15 = 35로 재구성; 핵심 Transaction/Recovery/Batch Acceptance 보강
- ADM/BZA OpenAPI/Generated Client 소비 경로 및 DLQ typed client drift 보정
- DB3 currentization 및 stale EDU 의존 DB Test를 Owner/Generated Domain 검증으로 이전
- 개발 정본/Architecture/Generator/EDU/Ownership/Visibility 관련 stale 내용을 최신 Steering 기준으로 currentization
- Delete Manifest 단일 정본: `cpf-docs/work/current/DELETE_MANIFEST.txt`

## 3. 이번 세션에서 확인한 검증
- Java source syntax: PASS (`2627` files, 마지막 기록)
- EDU static acceptance: PASS (`online=20`, `batch=15`, `total=35`, errors=0)
- NXT3 final gates: PASS (`22/22`)
- Korean source comment quality: PASS (`781`, failures=0)
- DB basic Python tests: PASS (`82/82`)
- DB verification: 세션 중 `75/75 PASS` 확인
- Generator Python suite: `21 PASS / 10 environment skip / FAIL 0`
- Generated Domain Java21 대체 compile: member 26 source / external 24 source PASS
- ADM/BZA OpenAPI/consumer targeted gates: PASS 후 DLQ/typed-client drift 보정

주의: 위 결과는 **Java25 FullLocal/실 Runtime 최종 PASS를 의미하지 않는다.**

## 4. 다음 세션에서 바로 할 일 — 우선순위
1. Overlay 적용 후 Delete Manifest 한 줄 실행
2. 사용자 Java25 환경에서 `run-cpf-final-local-validation.ps1` 한 줄 실행
3. 실패가 있으면 `SUMMARY.csv` 기준 공통 원인별로 일괄 보정
4. 특히 `OPEN_ISSUES.md`의 8개 Runtime/재확인 항목을 닫는다
5. Java25 Build/Test + Runtime/Multi-WAS/DB3/Browser Evidence가 모두 PASS일 때만 전체 QA 완료 판정

## 5. EDU 다음 세션 체크
- Canonical Source 개수 35 유지
- online 20 / batch 15 물리 package 유지
- 구 EDU-DEV/BAT/ADM/BZA/GW/OPS/Legacy/Reference/Compatibility/Micro Sample 0
- Java25 compile/test 및 대표 Runtime 실행
- 신규 EDU가 실제 CPF Public API를 사용하고 internal/raw 우회가 없는지 최종 재확인

## 6. 절대 승계 금지
- 과거 100% / PASS 문구
- 과거 삭제 75개 Manifest
- 과거 EDU 135 Catalog
- Java21 대체 검증을 Java25 PASS로 해석
- 전체 Verification suite 미재실행 상태를 PASS로 간주

정확한 미완료는 `cpf-docs/work/OPEN_ISSUES.md`, 실행 결과는 `cpf-docs/work/TEST_AND_EVIDENCE.md`를 따른다.
