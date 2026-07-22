# CPF EDU Guide

## 목적

EDU는 CPF Public API·SPI·Generator·Runtime 운영 방식을 실제 Source와 Runtime으로 학습하는 정식 참조 경로다. Sample이 제품 구현을 대체하지 않으며, 기능 상태는 Source·Test·Evidence로 판정한다.

## 기본 원칙

- 교육·Reference Data는 운영 Product Seed와 분리한다.
- EDU Profile에서만 Sample Menu·API·Data를 활성화할 수 있다.
- 공식 Sample은 `cpf-core` Public API와 `cpf-common` 고객 확장 경로를 실제 사용한다.
- 직접 만든 날짜·Paging·Header·Error·Retry·Cursor 구현으로 CPF 표준을 우회하지 않는다.
- MBR·ACC·EXS는 최소 Transaction 검증 Domain이며 추정성 업무 원장을 제공하지 않는다.
- `cpf-reference`는 Reference·EDU Owner다.

## 필수 실습 흐름

1. Empty DB Install과 EDU Seed 분리 확인
2. `cmnDB` 최소 Sample CRUD·Validation·Transaction
3. List·Offset Page·Slice·Cursor와 Max+1 제한
4. Local/Remote Domain Call과 TransactionGlobalId
5. Timeout·Retry·Idempotency·Unknown Result
6. 고정길이 전문 Encode·Decode·Validation·Masking
7. File·Attachment·Compression·External Adapter
8. Batch Job·Worker·Checkpoint
9. Center-Cut Job·Item·Attempt·Failed-only Reprocess
10. ADM/BZA 조회·권한·승인·감사
11. Generator Create·Build·Run·Remove·Recreate

## 완료 조건

각 실습은 명령, Source 경로, API, SQL, 예상 결과, 오류·경계 Case와 최신 Runtime Evidence를 가진다. 문서만 존재하거나 정적 Test만 통과하면 완료가 아니다.
