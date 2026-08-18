# CPF Current Steering Interpretation / Design Decisions

## 원칙

Steering의 목적은 보존하되 문구를 기계적으로 구현해 Architecture를 악화시키지 않는다. Spring/OSS 의미, Owner, dependency direction, runtime trust boundary, 개발자 사용성을 함께 최적화한다.

## 이번 주요 설계 판단

1. **`CpfHttpHeaders` 단일화**: 새 Header API와 구 `CpfHeaders`를 병존시키지 않는다. Consumer를 새 정본으로 이동하고 구형을 삭제했다.
2. **`CpfContexts`를 만능 객체로 만들지 않음**: Core transaction/operation은 `CpfContexts`, Web client/locale은 `CpfWebContexts`, 인증 주체는 `CpfSecurityContext`, WAS runtime identity는 `CpfInstanceIdentity`가 소유한다. Core가 Web/Security/Operations로 역의존하지 않는다.
3. **불필요한 `CpfRestClient` 중복 생성 안 함**: 기존 `CpfTypedHttpClient`를 외부 HTTP Golden Path로 현행화했다. CPF 내부 canonical six는 외부로 내보내지 않는다.
4. **거래 호출 Identity는 Channel Vocabulary**: Canonical six는 `X-Transaction-Id`, `X-Original-Channel`, `X-Current-Channel`, `X-Caller-Channel`, `X-Target-Channel`, `X-Target-Operation-Id`다. 실제 Runtime System/Host/Application/Domain 개념까지 기계적으로 rename하지 않는다.
5. **Header 자체를 trust source로 사용 금지**: Current Channel은 Receiver Runtime이 자동 확정하고 inbound 값과 비교 검증한다. Caller Channel도 trusted ingress identity와 검증하며 Header 문자열만으로 신뢰하지 않는다.
6. **Canonical six setter 비공개**: Public custom Header API가 transaction/original/current/caller/target/operation protected 값을 변경할 수 없다.
7. **instanceId 단일 의미**: WAS instance ID. 명시값이 있으면 사용하고 없으면 실제 Hostname. Domain명, `local`, role synthetic fallback은 사용하지 않는다.
8. **operationId와 executionId 분리**: operationId는 안정적인 API/Handler 계약, 실행 UUID는 executionId다.
9. **같은 의미는 같은 이름**: 거래 호출 Context/Header/API에는 `originalChannel/currentChannel/callerChannel/targetChannel/targetOperationId`를 사용하고 구 System 거래 명칭을 병행하지 않는다. 실제 Runtime System Metadata와 instanceId는 독립 개념으로 유지한다.
10. **transactionId issuer와 Original Channel 분리**: 34자리 transactionId의 3자리 발급자 토큰은 내부 issuer metadata이며 Original Channel이 아니다. `X-Original-Channel`과 비교 검증하지 않는다.
10. **과거 Migration 불변**: 현재 이름을 맞추려고 historical migration을 수정하지 않고 신규 V119/V120/refDB V95로 upgrade/rollback을 제공한다.
11. **외부 Inbound 5개 + Receiver Current 자동설정**: 외부 시스템이 CPF Business Domain을 직접 호출할 때는 Transaction/Original/Caller/Target/Target-Operation 5개를 필수 입력으로 사용하고 `X-Current-Channel`은 요구하지 않는다. Receiver가 Generated Domain canonical `systemCode` 값을 그대로 `currentChannel`로 ingress 즉시 확정한다. 외부가 Current Header를 보내도 신뢰하지 않고 비교 검증만 한다. 내부 Domain Call은 업무 코드 Header 수동 작성 0개다.
