# CPF 상태 점검과 서비스 등록부 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 실행 환경 개발자, 운영자, 경로 선택 담당자
> **목적**: 생존·준비·진단 상태와 서비스 등록·심박·만료·배수 상태를 일관되게 제공한다.
> **관련 문서**: [게이트웨이 운영](CPF_GATEWAY_OPERATIONS_GUIDE.md) · [관측·장애대응·복구](CPF_OBSERVABILITY_INCIDENT_AND_RECOVERY_GUIDE.md)

---

## 1. 목적

CPF 상태 점검은 단순한 HTTP 200 확인이 아니다. 프로세스 생존, 신규 요청 수신 가능 여부, 서비스 전체 상태와 경로 선택 가능성을 분리해 판단한다.

## 2. 상태 점검 단계

### 생존 상태

현재 프로세스가 살아 있고 기본 실행 루프가 응답하는지 확인한다.

포함:

- JVM/프로세스
- 이벤트 루프와 기본 스레드 상태
- 치명적 내부 오류

제외:

- 원격 서비스 연쇄 호출
- 전체 DB 조회
- 느린 외부 호출

### 준비 상태

현재 인스턴스가 신규 요청을 받을 수 있는지 판단한다.

포함:

- 필수 로컬 DB
- 리스너
- 실행 환경 초기화
- 필수 비밀값/인증서
- 배수·점검 모드
- 핵심 큐·저장소

### 상세 진단

원격 소유자, 외부 기관, 메시지 중개 시스템 등 상세 의존 대상을 별도로 진단한다. 상세 진단 실패가 항상 현재 인스턴스 준비 상태를 내리는 것은 아니다.

## 3. 등록부 모델

### 조회와 제어 계약 분리

서비스 등록부는 조회용 `CpfServiceRegistryQueryPort`와 상태 변경용 `CpfServiceRegistryControlPort`를 분리한다.

- 조회 계약: 서비스·인스턴스·엔드포인트·상태·버전·용량·점검 정보
- 제어 계약: 등록, 갱신, 심박, 배수, 점검모드, 해제와 만료 처리

ADM은 제어 파사드를 통해 명령하고 등록부 저장소가 상태 전이와 버전을 최종 검증한다. 화면의 사용자 입력 `requestedBy`보다 인증 주체(Principal)을 신뢰한다.


서비스:

- serviceId
- systemCode
- moduleId
- owner
- version
- protocol
- endpoint
- visibility
- capability

인스턴스:

- serverInstanceId
- host
- processId
- zone
- cell
- profile
- startedAt
- heartbeatAt
- liveness
- readiness
- drain
- maintenance
- capacity
- metadata

## 4. 등록

```text
Runtime 시작
→ Identity 생성
→ Local Validation
→ Registry 등록
→ Heartbeat
→ Readiness UP
```

중복 인스턴스 ID를 거부한다.

## 5. 심박

- 주기
- TTL
- Jitter
- 배치 Update
- Network 시간 제한
- Stale 판단
- 복구

심박 Store 장애 시 로컬 실행 환경을 불필요하게 종료하지 않되 경로 선택 안전성을 위해 등록부 상태를 명확히 한다.

## 6. 상태

- UP
- DEGRADED
- DRAINING
- MAINTENANCE
- DOWN
- STALE
- UNKNOWN

상태 전이와 원인을 기록한다.

## 7. 히스테리시스

한 번의 실패로 즉시 DOWN/UP을 반복하지 않는다.

- 연속 실패 수
- 연속 성공 수
- 최소 유지시간
- Passive 오류
- Active Probe

## 8. 경로 선택

신규 요청 대상:

- readiness UP
- drain 아님
- maintenance 아님
- circuit 허용
- zone 정책
- capacity

## 9. 배수

```text
DRAIN_REQUESTED
→ 신규 요청 제외
→ In-flight 감소
→ DRAINED
```

최대 대기시간 후 강제 정책을 정의한다.

## 10. 점검 모드

- 사유
- 시작/종료
- 소유자
- 승인
- 신규 유입 차단
- 상태 점검 표시
- 자동 복귀
- 감사

## 11. 서비스 그룹

등록부는 서버 그룹과 연결된다.

- Member
- Weight
- Priority
- Zone
- 상태 점검
- 버전

## 12. 의존 대상

의존 대상 Graph:

- caller
- target
- protocol
- criticality
- timeout
- fallback
- owner

사고 영향 분석에 사용한다.

## 13. 버전

순차 교체 중 버전 혼재를 표시한다.

- compatible
- deprecated
- minimum peer version
- protocol/schema version

## 14. 보안

상태 점검 응답에 다음을 노출하지 않는다.

- Password
- DB URL 인증정보
- 비밀값
- Private IP 정책상 금지 정보
- 스택 추적
- 개인정보

상세 진단은 운영 권한을 요구한다.

## 15. 지표

- instance count
- healthy count
- stale count
- readiness duration
- heartbeat lag
- state transition
- drain duration
- registry error
- probe latency

## 16. 운영 절차

### 인스턴스 DOWN

1. 등록부 시각
2. 생존 상태
3. 호스트/프로세스
4. 최근 배포
5. 로그
6. Traffic 영향
7. 자동 복구
8. 재기동
9. 복귀
10. 사고

### 준비 상태 DOWN

1. 사유
2. 로컬 의존 대상
3. Pool/리스너
4. 비밀값/인증서
5. 배수·점검 모드
6. 복구 후 경로 선택

## 17. 테스트

- 인스턴스 2개
- 서로 다른 ID
- DB 중단
- 심박 중단
- Network Partition
- Stale
- 히스테리시스
- Drain
- Maintenance
- 순차 교체 버전
- 등록부 Store 장애
- 민감정보

## 18. 체크리스트

- [ ] 생존 상태와 준비 상태를 분리한다.
- [ ] 원격 Fan-out을 생존 상태에 넣지 않는다.
- [ ] 인스턴스 Identity가 로그/추적과 같다.
- [ ] 심박 TTL과 Stale 정책이 있다.
- [ ] 배수·점검 모드가 경로 선택에 반영된다.
- [ ] 상태 전이 원인을 기록한다.
- [ ] 상태 점검에 민감정보가 없다.

## 부록 A. 상태 응답 예

```json
{
  "status": "DEGRADED",
  "systemCode": "PAY",
  "moduleId": "payment-api",
  "instanceId": "pay-03",
  "version": "1.8.0",
  "checkedAt": "2026-07-30T08:10:20Z",
  "reasons": ["PAY_DB_REPLICA_LAG"],
  "dependencies": [
    {"name": "owner-db", "status": "UP", "latencyMs": 8},
    {"name": "read-replica", "status": "DEGRADED", "lagSeconds": 15}
  ]
}
```

민감한 호스트·계정·오류 원문은 공개 상태 응답에 포함하지 않는다.

## 부록 B. 등록 만료

- 심박 간격과 만료 시간은 네트워크 지연보다 충분히 길게 설정한다.
- 일시 누락은 히스테리시스로 흡수한다.
- 만료 인스턴스는 즉시 삭제하지 않고 묘비 상태와 마지막 정보를 보존한다.
- 재등록 시 인스턴스 세대와 버전을 비교한다.

## 부록 C. 배수

`DRAINING` 전환 → 신규 배정 중단 → 진행 요청 수와 최대 종료 시각 확인 → 0건 또는 시간 초과 → 중지·교체 → 준비 상태 확인 → `UP` 복귀
