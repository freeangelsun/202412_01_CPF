# CPF 테스트와 검증 증적 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 개발자, 시험 담당자, 검수자, 감사 대응자
> **목적**: 단위부터 장애·브라우저·다중 인스턴스까지 실행하고 기준 Commit과 결과를 증적화한다.
> **관련 문서**: [개발자 가이드](CPF_DEVELOPER_GUIDE.md) · [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---

## 1. 목적

CPF는 테스트 코드 존재가 아니라 실행 결과와 소스 Commit의 일치를 검증한다. 이 문서는 테스트 계층, 환경, 장애 주입, 검증 증적과 완료 판정 기준을 정의한다.

## 2. 테스트 Pyramid

```text
Unit
→ Contract
→ Integration
→ Runtime
→ Multi-instance/Fault
→ Browser
→ Release/DR
```

각 계층은 다른 실패를 검출한다.

## 3. 단위 테스트

- 업무영역 정책
- 검증
- State Transition
- 재시도 Classification
- 마스킹
- Codec
- Mapper
- Utility
- 권한

## 4. Contract 테스트

- 공개 API
- 오류
- 헤더
- 스키마
- 로컬/원격
- Message 버전
- 파일/전문
- SPI 어댑터

## 5. 통합 테스트

- DB
- 트랜잭션
- 송신함/수신함
- 이관
- 보안
- 감사
- 캐시
- 등록부
- 컨트롤러

## 6. 실행 환경 테스트

실제 프로세스를 시작한다.

- 상태 점검
- API
- DB
- Messaging
- 파일
- 게이트웨이
- 배치
- 프런트엔드
- 로그/추적

## 7. Multi-instance

### 운영 제어 통합 시나리오

- 게이트웨이 제어 요청의 정상 서명, 만료 시각, Body 변조와 Nonce 재사용
- 게시된 경로의 인스턴스별 적용, 확인 응답, 부분 적용, 구성 불일치와 되돌리기
- 게이트웨이 연결시험·상태 점검 작업자와 실시간 운영 스트림 재연결
- 로그 수집 모드별 허용 목록, 최대 크기, 재귀 마스킹과 보호 Port 미구성 안전 차단
- 클립보드·내려받기 반출의 권한·사유·감사·만료 주소
- 배치 작업정의의 작성자·승인자 분리와 소유 제어 Port
- 실행 투영과 일정관리기 트리거의 생성·수정·폐기·재기동 대사
- 작업자 실행기 등록부 미지원 유형 거부와 시도 원장
- 승인 파일 원격 전송의 부분 파일, Hash 불일치와 원자 이동
- 서비스 등록부 조회·제어 Port의 권한, 버전 충돌과 배수·점검 상태 전이


- 일정관리기 2개
- 작업자 다수
- 게이트웨이 다수
- 정책 소비자 다수
- 감사 Writer 다수
- 임대/Fencing
- Takeover
- Stale 결과

## 8. 장애 주입

- DB Down
- 메시지 중개 시스템 Down
- 대상 중단
- Network Delay
- 시간 제한
- ACK Loss
- Disk Full
- 프로세스 Kill
- 일부 적용
- Scanner Down
- 서명 실패
- Clock Skew

## 9. 브라우저

- Login/Logout
- 401/403
- Search/페이징
- Detail
- Form
- 409
- 위험 Action
- 승인
- 감사
- Accessibility
- Console 오류

## 10. DB Lifecycle

공급자별:

```text
Fresh Install
→ Verify
→ Upgrade
→ Verify
→ Rollback
→ Verify
→ Reapply
→ Verify
```

## 11. 생성기

- 두 임의 업무영역
- 사전 계획
- 적용
- Build
- DB 산출물
- 충돌
- User Modification
- Remove
- Recreate

## 12. 산출물

- 버전
- BOM
- POM
- 해시
- 서명
- SBOM
- bootJar/War
- Offline Bundle
- 원격 게시

## 13. 정상·오류·경계

최소 세트:

- 정상
- Null/빈 값
- 최대/최소
- Duplicate
- 버전 충돌
- 권한
- 시간 제한
- 재시도
- Unknown
- Partial Failure
- Recovery

## 14. 테스트 Data

- Product Seed와 분리
- 격리
- 자동 정리
- 개인정보 없음
- 반복 가능
- Clock 고정
- Random Seed 기록

## 15. Skip

Skip에는 사유와 조건이 있어야 한다.

- 환경 없음
- 외부 의존 대상
- 운영 전용
- 장시간

최종 `RequireAll`에서는 Skip을 성공으로 인정하지 않는다.

## 16. 검증 증적 구조

```json
{
  "sourceCommit": "...",
  "command": "...",
  "environment": "local",
  "profile": "postgresql",
  "startedAt": "...",
  "finishedAt": "...",
  "exitCode": 0,
  "status": "PASS",
  "requirements": ["DB-INSTALL"],
  "artifacts": [],
  "sanitized": true
}
```

## 17. 필수 필드

- 검증 증적 ID
- 소스 Commit
- Dirty 여부
- 도구 버전
- 명령
- 매개변수
- Environment/프로필
- 시작/종료
- Exit Code
- 상태
- 요구사항
- Expected
- Actual
- 원본 산출물
- Sanitizing

## 18. 소스 Commit

문서 Commit과 실제 검증 소스 Commit을 구분한다. 검증 증적은 실행한 소스 Tree의 정확한 Commit과 Dirty Fingerprint를 기록한다.

## 19. 원본 산출물

- 로그
- JUnit
- 조회 결과
- 브라우저 Report
- Screenshot
- Video
- 추적
- 이관 Plan
- 해시

Screenshot만으로 완료 처리하지 않는다.

## 20. 민감정보 제거

- Password
- Token
- 비밀값
- 개인 키
- 개인정보
- 내부 호스트 정책상 민감값
- DB Dump

원본은 제한 저장소에 두고 검증 증적에는 안전한 참조를 남긴다.

## 21. 요구사항 추적

```text
Requirement
→ Source
→ Test
→ Execution
→ Evidence
```

구현에서 역방향으로 소유자와 요구사항도 확인한다.

## 22. 상태

제품 추적 상태:

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

제품 README/Guide는 완성 제품 계약을 설명하고, 상태는 Work/Review/검증 증적에서 관리한다.

## 23. False Green 방지

금지:

- 테스트 삭제
- Assertion 약화
- 오류를 Skip
- 소스 자동 수정 Gate
- 과거 검증 증적 승계
- 한 공급자 결과를 3개로 복사
- 문자열 언급만으로 완료
- Dirty Tree 결과를 Clean 릴리스로 기록

## 24. Runner

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

## 25. 결과 보고

- PASS/FAIL 요약
- 실행하지 못한 항목
- 소스 결함
- 환경 결함
- 재실행 명령
- 검증 증적 경로
- 민감정보 확인

## 26. 보존

검증 증적 보존:

- 릴리스
- 보안
- 감사
- DB 이관
- DR
- 일반 CI

등급별 기간을 정한다.

## 27. 체크리스트

- [ ] 소스 Commit이 정확하다.
- [ ] 명령과 환경이 있다.
- [ ] 정상·오류·경계 테스트가 있다.
- [ ] 다중 인스턴스와 장애 주입을 검증한다.
- [ ] 3개 공급자 결과를 분리한다.
- [ ] 원본 산출물이 있다.
- [ ] 민감정보를 제거했다.
- [ ] 과거 검증 증적을 현재 성공으로 사용하지 않는다.

## 부록 A. 검증 증적 명세 예

```json
{
  "requirementIds": ["CPF-..."],
  "sourceCommit": "<sha>",
  "startedAt": "2026-07-30T08:00:00Z",
  "finishedAt": "2026-07-30T08:08:10Z",
  "environment": "integration-postgresql",
  "profile": "cpf-int",
  "command": "...",
  "exitCode": 0,
  "result": "PASS",
  "artifacts": [
    {"path": "logs/result.json", "sha256": "..."}
  ],
  "sanitized": true
}
```

## 부록 B. 장애 주입 시나리오

- 대상 연결 거부와 DNS 실패
- 연결 뒤 응답 지연
- 대상 처리 후 응답 유실
- DB Commit 뒤 프로세스 중단
- 메시지 중개 시스템 중단·중복 전달
- 임대 만료와 소유권 인계
- 오래된 세대 토큰의 완료 요청
- 설정·게이트웨이 일부 적용
- 인증서 만료·신뢰 실패
- 디스크 부족·파일 검사 지연

각 시나리오는 기대 공개 오류, 데이터 상태, 재시도 여부, 운영 표시와 복구 절차를 검증한다.

## 부록 C. 검증 금지 사례

- 실행하지 않은 명령을 성공으로 기록
- 다른 장비·과거 Commit의 결과를 현재 결과로 사용
- 실패 로그를 삭제하고 최종 요약만 보존
- 민감정보를 마스킹하지 않은 원본 첨부
- 샘플 API 성공만으로 제품 기능 완료 처리
- 정적 검색으로 다중 인스턴스·복구 성공을 주장

## 부록 D. 재현성

검증에 필요한 컨테이너·데이터·설정·시간대·시계·외부 대역을 기록한다. 무작위 자료는 씨앗을 보존하고, 시간이 결과에 영향을 주는 시험은 주입 가능한 시계를 사용한다.
