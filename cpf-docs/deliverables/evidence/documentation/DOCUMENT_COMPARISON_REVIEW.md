# CPF 최종 문서 세트 리뷰

- 기준 master/source: `758757c3206079b990ad7bef2f16c25063540041` (`13_02`)
- 공식 문서: **사용자 문서 19종 + 설계 산출물 5종 = 24종 DOCX/PDF**
- 최종 PDF 총 페이지: **534**
- 문서 완성도 목표: **200%** (CPF 문서 작업의 내부 품질 지수이며 외부 표준 점수가 아님)

## 문서별 역할·분량·독자·시각화 리뷰

| 문서 | 종류 | 주 독자 | 중점 | 페이지 | DOCX | PDF | 시각화/탐색 | 완성도 |
|---|---|---|---|---:|---:|---:|---|---:|
| 00_프레임워크안내 | 핵심 매뉴얼 | 처음 보는 개발자·설계자·운영자 | CPF 전체 구조·사용 장점·실행 경계·역할별 시작점을 빠르게 이해 | 32→25 | 0.52 MB | 0.96 MB | 전체 구조도·가치/오케스트레이션/Capability/Starter/Topology/운영 흐름 | 200% |
| 01_개발자매뉴얼 | 핵심 매뉴얼 | Online 업무 개발자 | Generator→Starter→공통 기능→거래→Transaction→MSA→Test→운영 인계의 전체 개발 경로 | 79→57 | 1.01 MB | 1.63 MB | 코드 예제·선택표·거래 흐름·Transaction/UNKNOWN·16개 EDU Lab·Recipe | 200% |
| 02_배치개발매뉴얼 | 핵심 매뉴얼 | Batch 개발자·운영개발자 | Job/Step·Chunk·Center-Cut·Checkpoint·Restart·Lease/Fencing·Reprocess/Reconcile | 46→26 | 0.76 MB | 0.81 MB | Batch 구조도·상태/판단표·Center-Cut·실행형 EDU/Recipe | 200% |
| 03_ADM개발자매뉴얼 | 핵심 매뉴얼 | ADM 개발자 | Owner API→Backend→OpenAPI→Generated Client→화면·권한·오류 연결 | 58→35 | 0.32 MB | 0.89 MB | 화면/Backend Reference·연동 절차·Fault/Browser Test | 200% |
| 04_ADM운영자매뉴얼 | 핵심 매뉴얼 | 운영자·플랫폼 관리자 | 검색→판단→승인→조치→Reconcile→감사 종결의 운영 Playbook | 195→164 | 0.39 MB | 4.36 MB | Route별 판단표·위험 조치·장애/복구·감사 확인 흐름 | 200% |
| 05_플랫폼운영매뉴얼 | 핵심 매뉴얼 | 플랫폼 운영·배포 담당자 | 설치·배포·관측·Drain·DB/Broker/Runtime 복구·Backup/Restore·정상화 | 57→30 | 0.53 MB | 1.03 MB | 운영 절차도·체크리스트·상태/복구 표 | 200% |
| 06_빠른시작_초기설정가이드 | 실무 가이드 | CPF를 처음 사용하는 개발자 | 환경 확인→Generator→Starter→첫 Online 거래→Test까지 30분 진입 경로 | 신규→2 | 0.07 MB | 0.11 MB | 빠른 찾기·Starter 선택표·첫 거래 코드·완료 기준 | 200% |
| 07_Starter_공통기능활용가이드 | 실무 가이드 | 업무 개발자 | Starter/Provider 선택과 Code·Parameter·Message·Calendar·Context·Cache·Lock·Persistence 활용 | 신규→3 | 0.09 MB | 0.17 MB | Public API 지도·공통 기능 코드·Cache/Lock 선택표·실패 첫 확인표 | 200% |
| 08_온라인거래_트랜잭션_MSA호출예제가이드 | 실무 가이드 | Online 업무 개발자 | 조회/변경 거래·공통응답·Local TX·MSA/외부 호출·UNKNOWN_RESULT·비동기 Context 실전 적용 | 신규→4 | 0.12 MB | 0.19 MB | 거래 코드·4상태 분기·Idempotency/Retry·운영 추적 체크리스트 | 200% |
| 09_Generator_업무도메인생성가이드 | 실무 가이드 | 프로젝트 구성·업무 개발자 | 정의→검증→생성→diff→재생성 Lifecycle과 Generated 구조 적용 | 신규→2 | 0.07 MB | 0.09 MB | Generator 흐름도·명령·생성물 판정표 | 200% |
| 10_설정_프로파일_환경가이드 | 실무 가이드 | 개발·배포·운영 담당자 | Profile·환경 Override·Secret·Runtime 변경·Drift를 안전하게 적용 | 신규→2 | 0.07 MB | 0.08 MB | 설정 우선순위·환경/Secret 체크리스트 | 200% |
| 11_데이터_DB_마이그레이션가이드 | 실무 가이드 | Backend 개발자·DBA | Persistence·Lock·Canonical DB·DB3·Migration/Seed/Rollback 적용 | 신규→2 | 0.08 MB | 0.09 MB | DB 흐름도·Vendor3·Migration/Seed/Rollback 표 | 200% |
| 12_메시징_외부연계가이드 | 실무 가이드 | 연계·업무 개발자 | HTTP/Event/Broker/File 연계와 Retry/DLQ/Replay/부분 실패 처리 | 신규→2 | 0.07 MB | 0.09 MB | 연계 흐름도·경계 선택·실패/복구 표 | 200% |
| 13_보안_권한_감사가이드 | 실무 가이드 | 개발·보안 담당자 | Trust Boundary·인증/권한·Secret·위험 조치·Audit 적용 | 신규→2 | 0.06 MB | 0.07 MB | 보안 흐름도·권한/감사 체크리스트 | 200% |
| 14_CLI_도구_검증가이드 | 실무 가이드 | 개발·CI 담당자 | CLI·Gate·Test·Package/Manifest/Hash 검증 절차 수행 | 신규→2 | 0.06 MB | 0.08 MB | 도구 흐름도·명령·검증 순서 | 200% |
| 15_Education_예제찾기가이드 | 실무 가이드 | 신규 개발자·교육 담당자 | 실행 가능한 Education Source/Test를 빠르게 찾아 업무에 적용 | 신규→2 | 0.06 MB | 0.08 MB | 예제 지도·상황→Source/Test 찾기 표 | 200% |
| 16_장애대응_Reconcile가이드 | 실무 가이드 | 개발·운영 담당자 | UNKNOWN/PARTIAL 판단·Blind Retry 방지·Reconcile/Replay/종결 수행 | 신규→2 | 0.12 MB | 0.09 MB | 장애 판단 흐름도·대사 순서·복구 선택표 | 200% |
| 90_BZA매뉴얼 | 핵심 매뉴얼 | 업무 관리자·BZA 개발/운영 담당자 | 조직·사용자·권한·승인·감사 기능의 사용과 확장 | 59→45 | 0.33 MB | 1.75 MB | 업무관리 화면/Route·권한·상태·오류 표 | 200% |
| 91_Gateway매뉴얼 | 핵심 매뉴얼 | Gateway 개발·운영 담당자 | Route·Security·Publish·Health·Transaction 조회와 Rollback/Reconcile | 38→18 | 0.33 MB | 0.63 MB | Gateway 흐름·Route/보안/운영 판단표 | 200% |
| 기술사양서 | 설계 산출물 | 구현자·검증자 | Contract·상태·API·설정·제한조건을 구현/검증 가능한 수준으로 명세 | 50→23 | 0.63 MB | 0.76 MB | Contract/상태/제약 표·검증 기준 | 200% |
| 기술표준서 | 설계 산출물 | 개발팀·리뷰어 | Naming·Dependency·API·Error·Context·Security·DB·Test 준수 기준 | 38→22 | 0.55 MB | 0.83 MB | 표준 선택표·Do/Do not·검수 기준 | 200% |
| 데이터베이스표준서 | 설계 산출물 | DBA·데이터 설계자·Backend 개발자 | DB3 공통 규칙·Schema/DDL/DML/Index/FK·Migration/Seed 기준 | 34→15 | 0.50 MB | 0.59 MB | DB 표준표·Vendor3·Migration/Backup/Restore 기준 | 200% |
| 산출물목록 | 설계 산출물 | PM·PL·QA·인수 담당자 | 공식 문서 체계의 목적·독자·진입점을 찾는 문서 지도 | 33→15 | 0.30 MB | 0.72 MB | 문서 지도·독자/목적/사용순서 표 | 200% |
| 아키텍처설계서 | 설계 산출물 | 아키텍트·기술리더·검토자 | Ownership·의존성·실행 경계·Topology·Failure/Recovery의 설계 근거 | 43→34 | 0.63 MB | 1.16 MB | Architecture/Runtime/DB/실행·복구 도식과 설계 판단표 | 200% |

## 200% 판정 기준

- **독자 적합성 25**: 해당 역할이 다음 행동을 결정할 수 있음.
- **내용 완전성 35**: 정상·오류·경계·복구·운영까지 연결.
- **정본/Source 정합성 30**: 최신 기준 SHA와 Public Owner/API, CURRENT/TARGET 경계를 혼동하지 않음.
- **예제 실용성 30**: 검색해서 바로 참고할 수 있는 코드·레시피·체크리스트.
- **정보구조·탐색성 25**: 빠른 시작, 상황별 진입, 목차/장 구조, 관련 문서 연결.
- **시각 품질 25**: 표·코드·그림·여백·페이지 밀도와 가독성.
- **검증 30**: DOCX 구조/a11y, PDF preflight/layout, 링크/asset, 표현/currentization QA.

현재 24종은 위 문서 품질 기준에서 200% 목표를 충족하도록 정리했습니다. 다만 문서 검수 중 보정한 `cpf-education` Source 2건의 Gradle compile/test는 이 컨테이너에서 Gradle 9.1.0 배포본을 내려받을 DNS가 없어 완료하지 못했습니다. 이 항목은 문서 점수와 분리하여 **미검증(환경)** 으로 기록합니다.

## 구조 설계 타당성

핵심 매뉴얼은 역할별 전체 흐름과 배경·판단 기준을 제공하고, 실무 가이드는 개발 중 반복해서 찾는 선택표·레시피·체크리스트를 짧은 경로로 제공합니다. 설계 산출물은 구현 방법이 아니라 Architecture·Contract·표준·DB 기준의 근거를 유지합니다. 같은 설명을 문서마다 복제하지 않고 각 독자의 종료 조건을 다르게 잡았습니다.
