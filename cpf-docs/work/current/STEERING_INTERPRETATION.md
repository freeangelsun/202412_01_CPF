# CPF Current Steering Interpretation / Design Decisions

## 원칙

Steering의 목적은 보존하되 문구를 기계적으로 구현해 Architecture를 악화시키지 않는다. Spring/OSS 의미, Owner, dependency direction, runtime trust boundary, 개발자 사용성을 함께 최적화한다.

## 이번 주요 설계 판단

1. **`CpfHttpHeaders` 단일화**: 새 Header API와 구 `CpfHeaders`를 병존시키지 않는다. Consumer를 새 정본으로 이동하고 구형을 삭제했다.
2. **`CpfContexts`를 만능 객체로 만들지 않음**: Core transaction/operation은 `CpfContexts`, Web client/locale은 `CpfWebContexts`, 인증 주체는 `CpfSecurityContext`, WAS runtime identity는 `CpfInstanceIdentity`가 소유한다. Core가 Web/Security/Operations로 역의존하지 않는다.
3. **불필요한 `CpfRestClient` 중복 생성 안 함**: 기존 `CpfTypedHttpClient`를 외부 HTTP Golden Path로 현행화했다. CPF 내부 canonical six는 외부로 내보내지 않는다.
4. **모든 Channel rename 금지**: System 의미로 잘못 사용된 Channel만 제거한다. Notification/template/channel registry처럼 진짜 업무 Channel은 유지한다.
5. **Header 자체를 trust source로 사용 금지**: internal caller는 Security가 검증한 attribute 또는 peer mapping으로만 확정한다.
6. **Canonical six setter 비공개**: Public custom Header API가 transaction/original/system/caller/target/operation protected 값을 변경할 수 없다.
7. **instanceId 단일 의미**: WAS instance ID. 명시값이 있으면 사용하고 없으면 실제 Hostname. Domain명, `local`, role synthetic fallback은 사용하지 않는다.
8. **operationId와 executionId 분리**: operationId는 안정적인 API/Handler 계약, 실행 UUID는 executionId다.
9. **같은 의미는 같은 이름**: 거래 추적 Java/DB/MyBatis/ADM/Frontend에 `clientId/originalSystemCode/systemCode/callerSystemCode/targetSystemCode/targetOperationId/instanceId`를 사용한다.
10. **과거 Migration 불변**: 현재 이름을 맞추려고 historical migration을 수정하지 않고 신규 V119/V120/refDB V95로 upgrade/rollback을 제공한다.
