# CPF QA39 최종 시정 개발 사전 리뷰

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `4aea798c913787e86341809e2cef2b9495cbf7ba`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- QA 우선 정본: `cpf-docs/work/CPF_QA_FINAL_CORRECTIVE_DEVELOPMENT_REQUIREMENTS.md`
- 작업 시작 상태: development_status=`부분 구현`, verification_status=`실패`
- 원격 확인: `master=4aea798c913787e86341809e2cef2b9495cbf7ba`
- 로컬 Working Tree: 이 실행 환경에는 사용자 로컬 Git Working Tree가 연결되지 않아 확인 불가. 적용기는 예상 변경 외 Dirty Tree를 첫 변경 전에 차단한다.

## 통합 Root Cause

1. Canonical/Release Catalog 이중 정본과 Final Catalog 37종 2회 중복.
2. Root Gradle이 `kind=starter`만 조회하여 실제 `starter-profile/internal-starter`를 누락.
3. 평면 Starter 물리 경로와 Catalog Owner Group 불일치.
4. Public BOM에 Profile과 Provider Leaf가 혼합됨.
5. Notification/Broker 계약이 Starter에 노출되고 Batch가 Kafka Provider에 고정됨.
6. 삭제된 얇은 Resilience/Feature Flag를 대체할 상용 Runtime·운영·감사 구현 부재.
7. Generated Client만 직접 수정하면 OpenAPI 재생성 시 Consumer가 소실됨.
8. Evidence 파일 존재/PASS 문자열만으로 완료를 판정하는 False Green 위험.

## QA와 명칭 Steering 충돌 해소

초기 QA 문서의 `notification/core`는 이후 QA 명칭 Steering의 모호한 `core` 금지와 충돌한다. 최신 Steering을 적용해 Owner 경로와 Artifact를 `notification/dispatch`, `cpf-starter-notification-dispatch`로 정본화한다. 기능 의미와 Public API/SPI/Internal 역할이 이름으로 식별된다.

## Owner·경계·Consumer

- `cpf-core`: Notification/Broker/Resilience/Feature Flag topology-independent API/SPI.
- `cpf-starters/<group>/<function>`: Provider와 Runtime internal 구현.
- `cpf-admin`: Owner Command API를 호출하는 운영 Backend와 실제 ADM 화면.
- `cpf-batch`: Provider-neutral Broker 제어 계약만 참조.
- 실제 Consumer: Gateway/HTTP/TCP Resilience, Notification JDBC Outbox, Batch Worker/Scheduler, ADM Feature Flag/Resilience Console, Generator Profile/Binding.

## MSA·동일 JVM·부분 실패

- API/SPI는 Spring/OSS 타입을 노출하지 않아 동일 JVM·분리 WAS 모두 사용 가능.
- Resilience는 operation/revision 단위 shared guard, idempotency, UNKNOWN_RESULT/reconcile을 제공한다.
- Feature Flag는 DB revision 기반 Cache 무효화, secure override, expiry, kill switch, fallback을 제공한다.
- Batch Worker ID는 system/instance/process/restart/lease epoch/fencing token을 포함한다.

## 보안·감사·마스킹

- ADM은 검증된 `adm.operatorId`만 신뢰한다.
- 승인·회수·Kill Switch는 별도 권한과 위험 확인 Header를 요구한다.
- 자기승인을 차단한다.
- Feature Flag 원문 값과 민감 Evaluation Context는 운영 목록·감사에 노출하지 않는다.
- JDBC 감사 실패를 정상 성공으로 삼키지 않는다.

## DB·Migration·Generator

- 공식 Vendor는 Oracle/PostgreSQL/MariaDB 3종만 사용한다.
- Canonical Schema와 3종 Source/Install/V97/Rollback/Verify/Checksum을 함께 변경한다.
- Profile은 `ACTUAL_PROFILE_ARTIFACT` 방식으로 통일한다.
- Provider는 일반 공개 선택 목록이 아니라 binding/resolved lock으로 선택한다.

## 구현 순서

Catalog 단일화 → 원자 이동 적용기 → Settings/Root Gradle → Public/Internal BOM → API/SPI/Internal → Resilience/Feature Flag → Batch Provider 중립화 → 3 DB → ADM/OpenAPI/Generated Client/화면 → Gate/Evidence → 적용 재실행 검증 → 산출물.

## 보호 대상

6개 Public Profile, Runtime Control Owner Applier, Broker UNKNOWN_RESULT/Reconcile, Notification Outbox/Receipt, SFTP Path Policy, 3 DB Canonical Source, Generator resolved lock/exception policy, 기존 Public API/Artifact 좌표 호환 Migration, 네 보호 경로.

## 완료 판정

Source 존재가 아니라 Consumer·Test·SQL·운영·Gate가 연결돼야 한다. Java25 Fresh Build, Frontend 3 Browser, 3 DB Live Lifecycle, 외부 Runtime Fault, Supply-chain exact-SHA Evidence가 모두 exit 0이기 전 전체 QA39 완료를 선언하지 않는다.
