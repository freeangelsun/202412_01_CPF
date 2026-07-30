# CPF 관측·장애대응·복구 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 관제 담당자, 사고 대응자, 서비스 책임자
> **목적**: 로그·추적·지표·경보를 거래와 연결하고 사고 분류·완화·복구·사후 분석을 수행한다.
> **관련 문서**: [플랫폼 운영자](CPF_ADMIN_OPERATOR_GUIDE.md) · [테스트와 검증 증적](CPF_TEST_AND_EVIDENCE_GUIDE.md)

---

## 1. 목적

CPF의 관측 기능은 로그를 많이 남기는 것이 목적이 아니다. 운영자가 거래, 인스턴스, 외부 호출, 배치 실행과 운영 조치를 연결하여 원인을 찾고 안전하게 복구하도록 한다.

## 2. 관측 신호

- 구조화 로그
- 추적
- 지표
- 사건
- 감사
- 상태 점검
- 등록부
- 시간선
- 사고
- 검증 증적

## 3. 공통 식별자

- environment
- cellId
- systemCode
- moduleId
- serviceId
- serverInstanceId
- transactionId
- traceId
- segmentId
- operationId
- executionId
- jobId
- eventId

모든 저장 방식에서 같은 의미를 사용한다.

## 4. 구조화 로그

### 수집 모드와 보호 장치

쿼리, 요청·응답 헤더, 요청·응답 본문과 오류 스택은 하나의 Boolean 설정으로 켜지 않는다. 항목별 수집 모드, 허용 목록, 최대 Byte, 마스킹 정책과 적용 기간을 결정한다.

수집 보호 장치는 다음 순서로 적용한다.

```text
수집 정책 결정
→ 허용 대상 확인
→ 최대 크기 절단
→ 구조화 자료 재귀 마스킹
→ 비밀값·인증정보 제거
→ 저장 및 감사
```

보호 Port가 없거나 마스킹 정책을 해석할 수 없으면 민감 페이로드 수집을 안전 차단한다.

### 감사된 반출

조회 권한과 반출 권한을 분리한다. 클립보드와 내려받기는 서버가 다시 마스킹한 산출물을 만들고 `exportId`, 행위자, 사유, 대상 로그, 형식, 생성·만료 시각과 결과를 기록한다. 내려받기 주소는 짧은 유효시간을 가지며 재사용과 권한 범위를 제한한다.


필드:

- timestamp
- level
- logger
- messageCode
- transactionId
- segmentId
- module
- instance
- execution
- status
- duration
- failureCode
- maskedContext

금지:

- 비밀값
- 전체 요청/응답
- 무제한 스택 추적
- 인증정보 URL
- 원문 개인정보

## 5. 파일 로그

경로는 Environment/서비스/인스턴스를 구분한다.

Rotation:

- 일자
- 크기
- 압축
- maxHistory
- totalSizeCap
- 디스크 상한
- 삭제 이력

## 6. DB 로그

DB 로그 장애가 원 업무에 미치는 정책을 정의한다.

- 필수 감사: 안전 차단
- 운영 보조 로그: 로컬 Spool
- 재전송
- 유실 탐지
- 경보

## 7. 추적

추적은 Segment와 원격 시도를 표현한다.

```text
Inbound
├─ Validation
├─ DB
├─ Remote Attempt 1
├─ Remote Attempt 2
└─ Result
```

Sampling과 추적 Boost 정책을 둔다.

## 8. 지표

Golden Signal:

- Traffic
- 오류
- Latency
- Saturation

추가:

- 회로 차단기
- 재시도
- Unknown
- 큐 Lag
- 작업자 임대
- 게이트웨이 적용
- DB Pool
- 파일 Scan
- 보상

## 9. SLO

SLO 구성:

- 대상 서비스
- 측정 지표
- 목표
- Window
- 제외 조건
- 오류 허용량
- 경보 정책
- 소유자

단일 평균만 사용하지 않고 Percentile과 실패율을 사용한다.

## 10. 경보

경보는 Actionable해야 한다.

- 대상
- 조건
- 지속 시간
- Severity
- Dedup Key
- Suppression
- 소유자
- 운영 절차서
- 사고 정책

## 11. 사고

상태:

```text
OPEN
→ ACKNOWLEDGED
→ INVESTIGATING
→ MITIGATED
→ RESOLVED
→ CLOSED
```

필드:

- incidentId
- severity
- service
- impact
- startedAt
- detectedAt
- owner
- timeline
- related transaction
- deployment/config
- rootCause
- action
- resolvedAt

## 12. 운영 절차서

운영 절차서는 다음을 포함한다.

- 증상
- 확인 조회
- 안전한 진단
- 영향도 판단
- 자동 조치
- 수동 조치
- 금지 조치
- 되돌리기
- 복구 확인
- Escalation
- 검증 증적

## 13. 자동 복구

자동 조치는 제한을 갖는다.

- 조건
- 최대 횟수
- Cooldown
- 대상 허용 목록
- 실패 시 중단
- 반복 실패 차단
- 감사
- 운영자 통지

## 14. 결과 불명 복구

```text
Unknown 탐지
→ Downstream 상태 조회
→ 중복 영향 판단
→ 자동 확정 가능 여부
→ 운영자 Case
→ 재처리/보상
→ 최종 상태
```

## 15. 게이트웨이 장애

확인 순서:

1. 경로/바인딩 버전
2. 적용 상태
3. 대상 상태 점검
4. 시도
5. TLS/Auth
6. 호출량 제한
7. 시간 제한
8. Unknown
9. 정본 불일치
10. 되돌리기

## 16. 배치 장애

1. Definition 버전
2. 트리거
3. 실행
4. 작업자 임대
5. Spring 배치 메타데이터
6. 체크포인트
7. 에이전트
8. Unknown
9. Restart/Reprocess
10. SLA

## 17. DB 장애

- 생존 상태/준비 상태
- Pool
- 잠금
- Slow 조회
- Replica
- 이관 정본 불일치
- Disk
- 백업
- Failover
- 트랜잭션 결과 불명

## 18. Messaging 장애

- 생산자
- 송신함
- 메시지 중개 시스템
- Lag
- 소비자
- 수신함
- 재시도
- DLQ
- Poison
- 재생

## 19. Change Correlation

사고는 최근 변경과 연결한다.

- 배포
- 설정
- 실행 정책
- DB 이관
- 게이트웨이 바인딩
- 인증서
- 비밀값 Rotation
- Feature Flag

## 20. Capacity

- CPU
- Memory
- 스레드
- Connection
- 큐
- Disk
- Network
- 작업자 Slot
- 에이전트 Capacity

Trend와 Forecast를 제공한다.

## 21. Maintenance

- 신규 유입 차단
- Drain
- In-flight
- 배치 Window
- 큐
- 상태 점검
- 복귀
- 감사

## 22. 검증 증적 수집

- 소스/산출물 버전
- 환경
- 시각
- Dashboard 스냅샷
- 조회
- 추적
- 로그
- 변경 이력
- 조치
- 결과
- 민감정보 제거

## 23. 사후 분석

- 영향
- 시간선
- 탐지
- 근본 원인
- 기여 요인
- 대응
- 잘된 점
- 개선
- 소유자
- Due Date
- 재발 방지 테스트

## 24. 테스트

- 경보 Dedup
- 사고 연결
- 운영 절차서
- Auto Recovery 상한
- 로그 Store 장애
- 추적 Sampling
- Multi-instance
- 결과 불명
- Change Correlation
- DR

## 25. 체크리스트

- [ ] 거래와 실행이 동일 식별자로 연결된다.
- [ ] 로그에 민감정보가 없다.
- [ ] 경보에 소유자와 운영 절차서가 있다.
- [ ] 사고가 변경 이력과 연결된다.
- [ ] 자동 복구에 상한과 감사가 있다.
- [ ] 결과 불명을 대사한다.
- [ ] 조치 후 복구 확인이 있다.

## 부록 A. 공통 로그 필드

```json
{
  "timestamp": "2026-07-30T08:20:10.123Z",
  "level": "ERROR",
  "systemCode": "PAY",
  "moduleId": "payment-api",
  "instanceId": "pay-03",
  "transactionId": "TX-...",
  "segmentId": "SEG-...",
  "operationId": "OP-...",
  "event": "REMOTE_CALL_FAILED",
  "errorCode": "TARGET_DOWN",
  "message": "대상 서비스에 연결할 수 없습니다.",
  "durationMs": 301,
  "retryAttempt": 1
}
```

원문 요청·응답, 비밀값, 인증 토큰과 개인정보를 통째로 기록하지 않는다.

## 부록 B. 경보 등급

| 등급 | 예 | 기대 대응 |
|---|---|---|
| 긴급 | 전체 거래 중단, 데이터 손상, 보안 침해 | 즉시 호출·확산 차단·사고 지휘 |
| 높음 | 주요 업무 오류율 급증, 결과 불명 누적 | 수분 내 조사·완화 |
| 중간 | 일부 인스턴스 저하, 정본 불일치 | 근무 시간 내 복구·원인 분석 |
| 낮음 | 용량 추세, 인증서 만료 예정 | 계획 작업 생성 |

## 부록 C. 결과 불명 누적 대응

1. 업무·대상기관·시간대·오류 단계별 건수를 집계한다.
2. 자동 재시도를 중단하고 추가 중복을 방지한다.
3. 상대 결과 조회·대사 자료의 가용성을 확인한다.
4. 확정 성공·실패·여전히 불명으로 분류한다.
5. 재처리·보상·수동 확정 권한과 승인 범위를 정한다.
6. 사용자·대외기관 통지와 회계·원장 영향을 확인한다.

## 부록 D. 사후 분석

사고 요약, 영향, 시간선, 탐지 경로, 직접 원인, 구조적 원인, 잘 작동한 통제, 실패한 통제, 재발 방지 항목, 담당자·기한과 검증 방법을 기록한다. 개인 비난 대신 시스템과 절차의 개선에 집중한다.
