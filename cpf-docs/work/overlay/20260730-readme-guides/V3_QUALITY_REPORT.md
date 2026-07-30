# CPF README·가이드 V3 품질 검토 보고

## 기준

- Branch: `master`
- Commit: `b7c6146e952c10b885952fa2bc6b6786f4611d86`
- 목적: README를 제품 브로셔 역할로 고정하고, 가이드를 실제 작업 가능한 전문 매뉴얼로 보강

## README 검토 결과

### 정보 위계

1. Hero
2. CPF 전체 구조
3. 책임이 분명한 제품 구성
4. 핵심 가치
5. 설계·생성·실행·운영·변화 생명주기
6. 배포 구성
7. 실패와 복구
8. ADM/BZA·Gateway·Batch 운영
9. 생성 업무영역 확장
10. 역할별 문서 경로
11. 접이식 빠른 시작

### 보호한 요소

- Hero와 기존 전체 구조도
- Desktop/Mobile 전용 이미지 전환
- 배포 구성, 실패 복구, 운영, 생성기와 문서 지도
- 상세 내용은 가이드로 이동시키는 브로셔 원칙

### 추가한 요소

- `cpf-value-pillars-*`: CPF의 4가지 핵심 가치
- `cpf-lifecycle-*`: 설계부터 복구·변화·Evidence까지의 제품 생명주기
- README 시각 자료 10개 `<picture>` 구성
- 총 40개 PNG/SVG 시각 자산

## 가이드 검토 결과

### 공통 보강

- 상세 가이드 25개에 `## 0. 문서 계약` 추가
- 기준 Commit, Owner, 완료 결과, 적용 범위, 독자와 완료 판정 명시
- 상세 가이드 25개에 `## 부록 Z. 구현 추적 시작점` 추가
- Source·Controller·Port·Config·SQL·Test·Generator 추적 경로 제공
- 완료·부분 구현·미구현·미검증·실패·재확인 필요 상태 기준 통일

### 실행 절차 집중 보강

- Architecture와 Local/Remote 설계
- 개발 Requirement부터 완료까지의 작업 흐름
- ADM 공통 운영 명령
- Gateway API·Server Group·Binding·Partial Apply Runbook
- Service Registry API·Drain·Heartbeat·복귀 판정
- Batch Definition·Projection·Executor·Attempt Ledger·Remote File
- Scheduler Misfire·Restart/Rerun/Reprocess·Takeover
- Observability 거래 분석·Capture Policy·감사된 Log Export
- Database Canonical·Vendor Pack·Fresh Install·Upgrade/Rollback
- 설치·Rolling Upgrade·Expand/Contract·Rollback/Forward Fix
- Generator Dry-run·재실행·업그레이드·수용 시험
- Security Threat/Control·Secret·DR·Retention
- Test Scenario·Evidence Bundle·부분 실패 예
- ADM/BZA UI Package·상태 모델·위험 조치·접근성
- BZA 사용자·권한·결재 Snapshot·첨부·감사
- Outbox/Inbox·Replay·Compensation
- Config 우선순위·Publish/ACK·Override·Drift
- Artifact Manifest·승격·폐쇄망·Rollback
- Foundation/Public API 호환성과 오류·시간·Paging 규칙
- Tool 안전 계약·Exit Code·자동화
- DB Profile·Account·Read Replica·Multi-datasource

## 최신 개발 내용 반영

- Gateway 수신 `pathPattern`과 소유 시스템 `targetPath()` 분리 및 안전한 경로 재작성
- Gateway `GATEWAY_E2E` 연결시험, Probe 실행기와 인스턴스별 적용·ACK·정본 불일치
- Method·Target·Content-Type·Body Hash·Caller·Operator·Timestamp·Nonce·Audience·Key ID Canonical HMAC
- 다중 인스턴스 공용 Nonce Claim과 보안 감사의 Fail-closed 경계
- Gateway 승인·활성·차단·폐기 및 Server Group·Binding 폐기의 Approval Owner 강제
- `CpfServiceRegistryCatalog` 기반 Service·Endpoint·환경 Code 정본과 `STG` 지원
- `CpfServiceCallAttempt`를 이용한 Retry·Failover 시도별 원장
- `/adm/api/log-exports`의 재귀 마스킹, 15분 TTL, 5MB 상한, 소유자 전용 ADM DB Artifact
- `FILE_PROCESS`의 `PROCESSOR:` Reference, 필수 Path Parameter와 `FileProcessHandler` SPI
- 승인 Shell의 Detached Signature·공개키/X.509 Chain 검증과 Hash-only 자동 하향 금지
- V81 Gateway 대상 경로·Nonce, ADM Log Export, BAT Attempt 상세의 3개 DB Vendor Migration·Rollback
- Reference Catalog의 Parent Reset, 오래된 응답 차단과 Provider 미구성 Fail-closed

## 정적 검증 결과

- Root README + 문서 홈 + 상세 가이드: 27개
- README PNG/SVG 시각 자산: 40개
- README 반응형 `<picture>`: 10개
- Markdown·HTML 내부 링크와 이미지 참조: 283건 확인
- UTF-8, 코드 블록 짝, trailing whitespace, 금지 표현: 통과
- 상세 가이드 25개의 문서 계약·기준 Commit·구현 추적 부록: 통과
- JSON 예제 구문: 통과
- 최신 Commit 전용 계약 토큰과 구현 추적 경로: 통과
- ZIP CRC와 추출 후 동일 검증: 패키징 단계에서 별도 확인

## 완료 판정

- README 브로셔 구조: 완료
- Desktop/Mobile 시각 자산: 완료
- 가이드 공통 구조와 추적성: 완료
- 주요 영역 실행 레시피: 완료
- Source/API 정합성: 최신 Master의 대표 Controller·Port와 대조
- 실제 Runtime·Browser·3 Vendor DB 실행: 이 패키지 작업 환경에서는 미검증

Runtime·Browser·DB 실행 결과는 별도 Evidence 없이 성공으로 기록하지 않는다.
