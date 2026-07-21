# CPF EDU Guide

## 1. Purpose

EDU는 CPF의 기능을 실제 업무 시나리오로 학습하고 검증하기 위한 Reference implementation입니다.

EDU는 장식용 Sample이 아니며, 제품 API와 확장 방식의 실제 Consumer 역할을 합니다.

## 2. Reference Domains

- `cpf-member`: 회원 등록, 조회, 개인정보 마스킹과 권한
- `cpf-account`: 계좌 CRUD, 잔액, 이체와 Generator lifecycle
- `cpf-reference`: 검색, Paging, Sorting, Validation과 기준정보
- `cpf-external`: REST, 전문, 파일, 메시징과 결과 불명 복구
- `cpf-batch`: 동기·비동기 Batch, Worker와 Center-Cut

## 3. Recommended Learning Path

1. 표준 Header와 오류
2. Reference CRUD
3. Member 개인정보와 권한
4. Account Transaction과 멱등성
5. Local/Remote 호출
6. External timeout과 retry
7. Outbox/Inbox와 DLQ
8. Batch와 Worker
9. ADM 거래 조회
10. 장애 복구와 Reconciliation

## 4. CRUD Scenario

포함 항목:

- 등록
- 단건 조회
- 수정
- 삭제 또는 상태 전환
- 목록
- Offset paging
- Keyset paging
- 검색 조건
- 허용 정렬
- Validation
- 권한
- Audit

## 5. Service Call Scenario

동일 기능을 다음 두 방식으로 호출합니다.

- Modular Monolith Local Facade
- Microservice Remote Adapter

응답, 오류, Header, Audit와 Idempotency가 일치하는지 비교합니다.

## 6. External Integration Scenario

- 정상 REST
- 응답 지연
- Timeout
- Target down
- 5xx
- 잘못된 전문
- 중복 응답
- 결과 불명
- 결과 재조회
- Reconciliation
- 수동 복구

## 7. Messaging Scenario

- Outbox 저장
- Broker 발행
- Inbox 중복 방지
- Consumer 실패
- Retry
- DLQ
- Replay
- Poison message 격리
- 순서와 파티션

## 8. File Scenario

- 파일 생성
- 압축
- 암호화
- SFTP 전송
- 수신 확인
- 재전송
- checksum
- 부분 실패
- 보존과 삭제

## 9. Batch Scenario

- 단순 Job
- Multi-step
- Parameter
- Dependency
- Partition
- Checkpoint
- Restart
- Retry와 Skip
- 2개 Worker
- Drain과 Takeover
- Center-Cut

## 10. Evidence

각 EDU 시나리오는 다음을 제공합니다.

- 실행 명령
- Profile
- 테스트 데이터
- 기대 결과
- HTTP 또는 메시지
- DB 조회
- 파일 로그
- ADM 조회 경로
- 오류와 복구 절차

Sample test 통과만으로 제품 기능 완료를 주장하지 않습니다.
