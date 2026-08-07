# CPF 중앙 통합 협업·의견교환 정책

## 원칙

Developer GPT, QA A, QA B는 서로의 결과를 자동 승계하지 않는다. 각자는 actual Source와 Evidence를 독립 검증하고 의견을 남긴다. 중앙 통합자는 세 결과와 최상위 Requirement를 비교해 다음 Requirement를 만든다.

## 공통 자율 발견 의무

QA/개발 지침에 적힌 항목만 처리하지 않는다.

분석·개발·QA 과정에서 추가 결함, 누락, 잘못된 Ownership, Consumer 단절, False-Green, Dead/Stale Source, 중복 정본, Security/Recovery/Logging/DB/Frontend/Generator/Artifact 문제를 발견하면:
- QA는 신규 Finding으로 기록
- 개발GPT는 자신의 권한 범위에서 즉시 구현·보완·검증
- 사용자 승인 필요한 Git 쓰기/삭제/보호경로 변경은 실행하지 않고 요청으로 기록
한다.

## 의견 필드

각 Report는 다음을 포함한다.
- WORKER_OPINION
- DISAGREEMENT
- ARCHITECTURE_DECISION_REQUIRED
- ADDITIONAL_QA_REQUIRED
- ADDITIONAL_DEVELOPMENT_REQUIRED
- NEXT_ACTION

## QA A/B 순환

QA A와 QA B의 Primary 영역은 고정하지 않는다.
동일 QA의 습관성 False-Green을 줄이기 위해 회차별로 Architecture/Runtime/Security/Frontend/DB/EDU/Logging 영역을 순환한다.

고위험 항목은 양쪽이 독립검수한다.
- Release/Evidence provenance
- Security/Approval
- Transaction/Logging
- EDU/ADM Architecture
- P0 Finding
- 2회 이상 재개발된 Requirement

## Cross Review

1. A/B 독립 1차
2. 중앙 Finding Merge
3. 중앙이 상대 QA의 이견/누락/반론을 각 QA에 전달
4. 필요한 항목만 Cross Review 2차
5. 중앙 최종 판정
6. 다음 개발GPT Requirement 작성

## 결과 전달

GPT는 Commit/Push하지 않는다.
각 GPT는 Root-relative ZIP + SHA-256 + Handover를 사용자에게 전달한다.
사용자가 적용·Push한 뒤 새 exact master SHA에서 다음 작업자가 다시 검증한다.
