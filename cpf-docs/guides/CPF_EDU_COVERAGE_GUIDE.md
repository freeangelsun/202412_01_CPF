# CPF 교육·예제 범위 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 신규 개발자, 교육 담당자, 예제 검수자
> **목적**: 실제 제품 API로 정상·오류·복구·보안·운영 실습을 수행한다.
> **관련 문서**: [개발자 가이드](CPF_DEVELOPER_GUIDE.md) · [테스트와 검증 증적](CPF_TEST_AND_EVIDENCE_GUIDE.md)

---

## 1. 목적

CPF 교육 자료는 별도 장난감 규격을 만들지 않는다. 실제 제품 공개 API, SPI, 오류, 페이징, 보안과 운영 계약을 그대로 사용한다.

## 2. 교육 원칙

- 실제 Product API 사용
- 내부 패키지 Import 금지
- 정상뿐 아니라 오류·경계·복구
- 로컬/원격 비교
- 권한과 감사
- DB와 실행 환경
- 운영 화면 연결
- 실행 가능한 테스트

## 3. 교육 구조

```text
cpf-reference/
├─ foundation
├─ online
├─ remote-call
├─ messaging
├─ file
├─ telegram
├─ batch
├─ center-cut
├─ security
├─ operations
└─ failure-scenarios
```

## 4. Foundation

주제:

- Strings/Numbers/Decimals
- Date/Time/Clock
- Collection
- 페이지/Slice/Cursor
- 헤더
- transactionId
- 검증
- 마스킹
- 비밀값 참조

각 예제는 입력, 결과, 오류를 제공한다.

## 5. 온라인 거래

- 표준 헤더
- 실행 Annotation
- 트랜잭션 문맥
- 검증
- 오류 매핑
- 감사
- 추적

## 6. 로컬/원격

같은 파사드를 로컬과 원격으로 실행한다.

시나리오:

- 정상
- 4xx
- 5xx
- 시간 제한
- 대상 중단
- 재시도
- 회로 차단기
- Commit 후 응답 유실
- Failover

## 7. 페이징

- Offset 페이지
- Slice
- HMAC Cursor
- Sort 허용 목록
- 대량 검색
- 잘못된 Cursor
- Count 비용

## 8. Messaging

- 송신함
- Publisher
- 수신함
- 중복
- 재시도
- DLQ
- 재생
- 스키마 버전
- Poison

## 9. 파일

- 안전한 Path
- 체크섬
- 업로드
- Scanner
- Quarantine
- 내려받기
- Duplicate
- 인증정보 참조
- 결과 불명

## 10. 전문

- Fixed Length Layout
- Encoding
- Padding
- 검증
- 전문→DTO
- DTO→전문
- 오류 위치
- 민감 필드 마스킹

## 11. 배치

- 작업 묶음
- Definition
- 매개변수
- 일정
- 작업자
- Restart
- Reprocess
- Unknown
- 에이전트
- 서명

## 12. 대량 실행

- 대상 공급자
- Partition
- 점유
- Fencing
- Handler
- Failed
- Unknown
- Reprocess

## 13. 보안

- 인증
- 권한
- 비밀값
- 마스킹
- 감사
- 승인
- 파일 Scan
- 내려받기

## 14. 운영

- 등록부
- 상태 점검
- 트랜잭션
- 로그
- 사고
- 게이트웨이 적용
- 배치 Control
- 설정 정책
- 상태 대사

## 15. 생성 업무영역

생성기로 두 임의 업무영역을 생성해 교육한다.

- 구조
- 공개 API
- DB
- OpenAPI
- 테스트
- 등록부
- Remove

## 16. 오류 예제

- Null
- 빈 값
- 최대 길이
- Duplicate
- 낙관적 잠금
- 시간 제한
- Stale Fencing
- 인증 필요
- 권한 없음
- 호출량 제한
- Unknown
- Poison
- Scanner Down

## 17. 교육 문서 형식

각 주제:

1. 목표
2. 선행지식
3. 구조
4. 코드
5. 실행
6. 정상 결과
7. 오류 결과
8. 운영 조회
9. 테스트
10. 확장 과제

## 18. 실행

교육 예제는 저장소에서 Build/테스트 가능해야 한다.

```powershell
.\gradlew.bat :cpf-reference:clean :cpf-reference:test :cpf-reference:assemble
```

## 19. 브라우저

ADM/BZA 교육은 실제 경로와 권한 계정을 사용한다.

- 조회
- 변경
- 403
- 409
- 위험 조치
- 감사
- 접근성

## 20. 검증 증적

- 소스 Commit
- 명령
- 프로필
- 결과
- Screenshot 보조
- 로그/조회
- 관련 API
- 민감정보 제거

## 21. Coverage Matrix

| Capability | API | Example | 오류 | 테스트 | Operations |
|---|---|---|---|---|---|
| Foundation | O | O | O | O | - |
| 원격 Call | O | O | O | O | O |
| Messaging | O | O | O | O | O |
| 파일 | O | O | O | O | O |
| 배치 | O | O | O | O | O |
| 게이트웨이 | O | O | O | O | O |
| 보안 | O | O | O | O | O |

## 22. 체크리스트

- [ ] 실제 공개 API를 사용한다.
- [ ] 내부 구현 Import가 없다.
- [ ] 오류와 복구 예제가 있다.
- [ ] 운영 화면에서 추적할 수 있다.
- [ ] 생성기 산출물과 규격이 같다.
- [ ] 예제가 Build/테스트 된다.

## 부록 A. 권장 학습 경로

### 1단계 — 기반

표준 헤더, 오류, 검증, 시간·금액, 페이징과 거래 식별자를 사용해 단일 API를 만든다.

### 2단계 — 배포 동등성

같은 업무 파사드를 로컬·원격 어댑터로 실행하고 정상·오류 결과를 비교한다.

### 3단계 — 실패와 복구

시간 초과, 응답 유실, 중복 메시지, 임대 만료를 주입하고 결과 불명·대사·세대 토큰을 확인한다.

### 4단계 — 운영

ADM에서 거래·로그·추적·상태를 연결하고 권한·사유·승인을 가진 운영 명령을 수행한다.

### 5단계 — 공급

업무영역을 생성하고 세 DB 설치, 패키징, 폐쇄망 묶음과 업그레이드·되돌리기를 검증한다.

## 부록 B. 실습 완료 기준

각 실습은 실행 명령, 기대 출력, 오류 주입 방법, 복구 확인, 정리 명령과 검증 증적 위치를 제공한다. 정상 화면 캡처만으로 완료하지 않는다.
