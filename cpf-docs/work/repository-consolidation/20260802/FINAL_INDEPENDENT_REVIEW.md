
# CPF Repository·Core·Starter 통합 작업 최종 독립 리뷰

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 최신 원격 재확인: 2026-08-02 KST
- Git Commit·Push·삭제: 본 패키지 생성 과정에서는 수행하지 않음

## 2. 최종 Architecture 판정

현재 목표에 가장 적합한 구조는 다음이다.

```text
cpf-core
  독립 초경량 계약 JAR

cpf-starter-base
  일반 Spring Boot Runtime의 최소 조립 후보

cpf-common
  선택형 고객 업무 공통

cpf-starter-*
  필요한 기술 Runtime만 선택
```

Core 자체를 Starter에 흡수하지 않는다.
Core에서 선택 Runtime을 제거하고, 일반 Boot Domain의 편의를 Base Starter로 제공하는 방식이 장기 확장성과 Dependency 최소성을 함께 만족한다.

## 3. 확인한 정합성

- Starter Root를 고정 Product Container로 정의
- 현재 7개 Starter 상태와 Consumer 검토
- Framework 전체 29개 Capability Starter 후보 검토
- Core/Common Runtime Dependency 이관 후보 기록
- Kafka Primary와 RabbitMQ ADR 요구
- Leaf/Profile/Aggregate/BOM 선택 계층 정의
- Core/Base/Common Dependency 모델 추가
- 다음 QA 개발요건 45개
- Guide·Deliverable 갱신 요청
- 50개 삭제 후보의 정확한 Manifest
- Codex 검수 Package와 Hash Manifest
- 단일 적용·삭제·Commit·Push Script

## 4. 완료 상태

### 완료

- 작업 전 리뷰
- 자체 개발요건
- Architecture 정책과 결정 문서
- Repository·문서·Gate 통합 계획
- Starter 전수 검토 기준
- Core 경량화·Base Starter·Common 재분류 요건
- Profile·Bundle·BOM 설계
- 다음 QA 요청서와 45개 개발요건
- Guide·Deliverable 갱신 요청
- Delete Manifest
- Codex 검수 Package
- Root Overlay ZIP과 내부 Hash

### 부분 구현

- 최상위 목표 정본은 Amendment 제안 상태
- 기존 Guide 원본은 갱신 요청 상태
- 실제 Source·Build·BOM·Generator 변경은 다음 QA 범위

### 미구현

- Core Runtime 실제 이관
- Base Starter Artifact
- 세분화 Leaf Starter
- Generator Profile
- Aggregate Starter
- RabbitMQ 공식 Adapter 여부 결정
- BOM·Gate 실제 변경

### 미검증

- Java Build·Test
- JAR/WAR 포함·제외
- Runtime Broker·Cache·Security·OTel
- Multi-instance·Process Kill
- PowerShell 실제 실행
- Commit·Push 성공

## 5. 완벽성 판정

문서 패키지와 다음 QA 기준선으로는 현재 확인 가능한 범위에서 최선의 구조다.
그러나 Source 이관과 Runtime 검증을 실행하지 않았으므로 제품 완료나 완벽한 구현으로 판정하지 않는다.

```text
development_status = 부분 구현
verification_status = 미검증
```

## 6. 일괄 적용 Script 안전성

Script는 다음을 수행한다.

1. Repository·Branch·Remote 확인
2. `origin/master` Fetch와 비선행 상태 확인
3. Package 내부 SHA-256 검증
4. Codex와 Overlay 동일 경로 충돌 검사
5. 삭제 후보 상태 검사 — 없는 파일은 정상 건너뛰고, 존재하면서 수정된 파일만 충돌 처리
6. 대상 파일만 임시 Backup
7. Overlay 적용
8. Delete Manifest의 정확한 파일만 삭제
9. Secret/대용량 파일 기본 검사
10. 현재 Working Tree 전체를 Commit
11. 일반 Push

금지:

- force push
- reset
- restore
- stash
- git clean
- wildcard 삭제
- 자동 충돌 덮어쓰기


## 7. 이미 삭제된 후보의 멱등 처리

Delete Manifest 경로가 다른 셸에서 선행 삭제된 경우는 오류가 아니다.
Script는 이를 `SKIP_MISSING`으로 기록하고 계속한다.
존재하는 후보가 수정된 경우만 충돌로 중단한다.
