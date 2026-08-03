# CPF R3 실행 Dependency DAG

## P00 — 정본·안전·상태 초기화
- 선행 Phase: `없음`
- 진입 조건: 최신 origin/master SHA 확인
- 종료 Gate: `CPF-GATE-00`
- 종료 조건: 활성 정본·단일 원장·역할 상태·보호 경로·Git/삭제 안전 규칙이 하나로 고정됨
- 이 순서의 이유: 기준과 상태가 흔들리면 이후 구현·Evidence를 전부 다시 작성하게 됨

## P01 — Repository 전체 Inventory·양방향 추적
- 선행 Phase: `P00`
- 진입 조건: P00 Gate 통과
- 종료 Gate: `CPF-GATE-01`
- 종료 조건: Module·Package·API·SPI·Controller·SQL·DB·Config·Frontend·Generator·Script·Test·Doc·Evidence 미등록 0건
- 이 순서의 이유: 실제 Source 표면을 모르고 설계하면 누락·중복·잘못된 Owner 때문에 반복 개발이 발생함

## P02 — Architecture Ownership·Public API/SPI/Internal 경계
- 선행 Phase: `P01`
- 진입 조건: 전체 Inventory와 Consumer Graph 확보
- 종료 Gate: `CPF-GATE-02`
- 종료 조건: Owner·의존방향·Public API·SPI·Internal·DB Ownership·Topology 계약 확정 및 위반 Gate 동작
- 이 순서의 이유: 경계를 먼저 고정하지 않으면 구현 후 Module 이동과 Consumer 재이관이 반복됨

## P03 — 공통 계약·표준 타입·보안·오류·Context
- 선행 Phase: `P02`
- 진입 조건: Architecture Gate 통과
- 종료 Gate: `CPF-GATE-03`
- 종료 조건: Header·Context·ID·Clock·Money·Error·Validation·State·Idempotency·Auth·Masking 계약과 Test Kit 확정
- 이 순서의 이유: 공통 계약이 늦게 바뀌면 DB·API·Frontend·Provider를 모두 다시 수정하게 됨

## P04 — Canonical Data·DB Schema·Migration·Runtime Query
- 선행 Phase: `P03`
- 진입 조건: 상태·DTO·오류·시간·ID 계약 확정
- 종료 Gate: `CPF-GATE-04`
- 종료 조건: Canonical Metadata→3 Vendor DDL/Migration/Query/Index/Seed/Install/Upgrade/Rollback 정합
- 이 순서의 이유: DB 계약이 안정돼야 Repository·API·관리 화면을 중복 수정하지 않음

## P05 — Starter·Capability·Provider 기본 구현
- 선행 Phase: `P03;P04`
- 진입 조건: Core Contract와 필요한 DB Contract 확정
- 종료 Gate: `CPF-GATE-05`
- 종료 조건: Public API/SPI·Internal Provider·AutoConfiguration·Health·Operations·Consumer·Publication 완성
- 이 순서의 이유: Provider별 기능을 먼저 표준화해야 Product Runtime과 Generated Domain이 동일 계약을 사용함

## P06 — Generator·Profile·Generated Domain·Reference
- 선행 Phase: `P05`
- 진입 조건: 공개 Profile/Capability/Provider 선택면 확정
- 종료 Gate: `CPF-GATE-06`
- 종료 조건: Fresh Generate·Build·Run·Upgrade·Remove·Regenerate·cpf-member parity와 resolved lock 통과
- 이 순서의 이유: 제품 Consumer 개발 전에 생성 규칙을 고정해야 수동 이관과 재생성 충돌을 방지함

## P07 — Gateway·Batch·Messaging·외부연계·운영 Runtime
- 선행 Phase: `P05;P06`
- 진입 조건: Capability와 Generated Consumer 계약 통과
- 종료 Gate: `CPF-GATE-07`
- 종료 조건: 실제 Runtime의 정상·오류·다중 Instance·부분 실패·UNKNOWN·복구 구현 완료
- 이 순서의 이유: 운영 Backend/UI는 실제 Owner Runtime Command·Query 계약 위에 구축해야 함

## P08 — ADM/BZA Backend·권한·승인·감사·운영 Command
- 선행 Phase: `P04;P07`
- 진입 조건: Owner Runtime Query/Command와 DB 계약 확정
- 종료 Gate: `CPF-GATE-08`
- 종료 조건: ADM/BZA API·Service·Repository·권한·Data Scope·Approval·Audit·Operation Ledger 완성
- 이 순서의 이유: Frontend보다 Backend 계약을 먼저 완성해야 Mock 화면과 API 재생성을 반복하지 않음

## P09 — ADM/BZA 상용 Frontend
- 선행 Phase: `P08`
- 진입 조건: OpenAPI·Generated Client·Permission Manifest·DB Projection 확정
- 종료 Gate: `CPF-GATE-09`
- 종료 조건: 검색·Paging·상세·상태·위험조치·복구·접근성·반응형·3 Browser 기능 완성
- 이 순서의 이유: 안정된 Backend/OpenAPI 이후 UI를 연결해야 화면 재작성과 임시 JSON UI를 방지함

## P10 — 통합 Runtime·Fault·Process Kill·UNKNOWN 대사
- 선행 Phase: `P07;P08;P09`
- 진입 조건: 전체 수직 기능 구현과 운영 UI 연결
- 종료 Gate: `CPF-GATE-10`
- 종료 조건: DB/Broker/Network/Disk/Process/ACK·응답 유실·다중 Instance Fault Matrix 통과
- 이 순서의 이유: 정상 기능 완료 직후 실패 복구를 검증해야 뒤늦은 상태기계·DB 변경을 줄임

## P11 — Security·Performance·Capacity·Soak·DR
- 선행 Phase: `P10`
- 진입 조건: 기능·복구 경로 안정화
- 종료 Gate: `CPF-GATE-11`
- 종료 조건: Threat Model·Negative Security·Load/Soak·Resource Leak·RTO/RPO·DR Drill 통과
- 이 순서의 이유: 기능과 복구가 안정된 뒤 실제 규모와 보안 경계를 검증해야 측정 결과가 유효함

## P12 — Migration·Upgrade·Rollback·Publication·Supply Chain
- 선행 Phase: `P11`
- 진입 조건: 지원 기능·성능·보안 범위 확정
- 종료 Gate: `CPF-GATE-12`
- 종료 조건: Artifact/POM/BOM/SBOM/License/CVE/Signature·Install/Upgrade/Rollback·혼합 버전 통과
- 이 순서의 이유: 제품 구성이 고정된 뒤 배포 산출물을 만들면 Artifact 재패키징을 줄임

## P13 — Fresh Clone·다른 PC·고객 인수·문서 정합
- 선행 Phase: `P12`
- 진입 조건: 최종 후보 Artifact와 Migration Pack 확보
- 종료 Gate: `CPF-GATE-13`
- 종료 조건: 빈 Cache/다른 PC에서 Clone→Generate→Build→Install→Run→Operate→Upgrade→Rollback 재현
- 이 순서의 이유: Repository 밖의 숨은 로컬 의존과 Stale 문서를 Release 전에 제거함

## P14 — Codex 독립 전수검수·보완
- 선행 Phase: `P13`
- 진입 조건: 개발GPT 구현·자체검수·Evidence 제출 완료
- 종료 Gate: `CPF-GATE-14`
- 종료 조건: Codex 독립 Source/Runtime 검수와 보완 개발 완료
- 이 순서의 이유: QA 전에 독립 보완을 끝내 QA가 동일 결함을 반복 등록하는 일을 줄임

## P15 — QA 최종 전수검수·재개발 순환·정본 종료
- 선행 Phase: `P14`
- 진입 조건: Codex 완료 또는 타당한 해당 없음
- 종료 Gate: `CPF-GATE-15`
- 종료 조건: 최신 master 전체 재Inventory·미등록 0·모든 Requirement/Scenario QA 통과·정본/Evidence/ZIP 일치
- 이 순서의 이유: 최종 종료는 QA 통과로만 가능하며 새 결함은 같은 ID 또는 신규 ID로 다시 순환함

## 재개방 규칙

- 선행 Public API·State·DB·Permission·Profile·Artifact 계약이 변경되면 해당 Gate와 모든 Downstream Phase를 재개방한다.
- 부분 수정 후 문서만 갱신해 Gate를 유지할 수 없다.
- QA가 결함을 발견하면 동일 Requirement ID를 재개발/재검수 요청으로 되돌린다.