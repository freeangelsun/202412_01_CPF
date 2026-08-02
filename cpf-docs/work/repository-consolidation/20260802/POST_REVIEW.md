# 작업 후 독립 리뷰

## 기준 SHA

`1eda8e12fe123281748a4388938c62f11819da1e`

## 실제 완료

- `cpf-starters`를 `FIXED_PRODUCT_CONTAINER`로 정식 관리하는 Governance 작성
- Lightweight Core + Explicit Opt-in Starter Architecture 명시
- 현재 7개 Starter의 구현 근거·Consumer·Gap·검증 요구 작성
- Core/Common/Generated Domain의 Runtime Dependency 경량화 후보 작성
- Framework 전체 Starter 후보·Owner 유지·Plugin 우선 영역 분류
- 다음 QA 요청서와 45개 개발요건 작성
- RabbitMQ 지원 ADR를 다음 QA에 포함
- Starter 설명·선택 Guide 초안과 Guide/Deliverable 갱신 요청 작성
- Starter 그룹 등록을 Leaf/Profile/Aggregate/BOM으로 분리하고 다음 QA 요건에 반영
- 최상위 목표 정본과 기존 Guide가 아직 직접 갱신되지 않았음을 명시
- 기존 50개 삭제 후보와 안전 명령 유지
- ZIP·파일 Hash·Manifest 검증

## 부분 구현

- Starter Governance는 문서 기준선이며 Source·Gate·BOM·Generator 구현은 아직 변경하지 않음
- 최상위 목표 정본과 기존 역할별 Guide는 Amendment/갱신 요청 상태이며 직접 수정하지 않음
- Starter 후보 평가는 정적 Source/Build 분석 기준이며 전체 Consumer Graph Runtime 검증 전
- 가이드 초안은 실제 세분화 Artifact 확정 후 정식 Guide로 갱신해야 함

## 미구현

- Core Runtime Dependency 실제 이관
- Security/Cache 등 Starter 세분화
- RabbitMQ 공식 Adapter
- Generator Capability 선택
- Starter BOM·Publication·Optional variant Gate
- 안정 경로 Final Gate 수정

## 미검증

- Java Build/Test
- JAR/WAR Artifact 포함/제외
- Kafka/RabbitMQ/Redis/OTLP Runtime
- MariaDB/PostgreSQL/Oracle Lifecycle
- Multi-instance·Process Kill·Unknown Result
- Windows PowerShell 삭제 명령 실행

## 재확인 필요

- 최신 Codex Working Tree와 Overlay 충돌 여부
- Feature Flag 실제 Consumer와 공식 Provider
- Core Public API가 Spring/MyBatis/Batch/OTel Type을 노출하는지
- RabbitMQ의 공식 지원 범위
- 각 Starter의 최종 분리 단위와 Artifact 이름

## 사용자 승인 필요

- Overlay 적용
- 50개 과거 문서 삭제 명령 실행
- 다음 QA 착수
- 이후 Commit·Push

## 최종 추가 판정

- `cpf-core`는 독립 초경량 계약 JAR로 유지
- `cpf-starter-base`는 최소 Boot 조립 후보로 다음 QA ADR 등록
- `cpf-common`은 선택형 업무 공통으로 재분류
- 한 줄 적용·삭제·Commit·Push Script 포함
