# Starter Profile·Bundle 최종 검토

## 질문에 대한 답

대표 Starter A를 추가했을 때 의존 Starter B를 자동 등록하는 것은 가능하다.

### 권장 기본

Generator Capability Profile이 A를 승인된 Leaf Starter 목록으로 펼친다.
Build에는 Leaf Dependency가 명시되고 Domain Manifest에는 Profile Version과 `resolvedStarters`가 저장된다.

장점:

- 기존 Domain이 Profile 변경으로 자동 변하지 않음
- 어떤 JAR과 Bean이 들어왔는지 명확함
- upgrade/rollback diff 가능
- 상호 배타 Provider를 생성 단계에서 차단

### 보조 방식

안정 조합은 Aggregate Starter POM으로 전이 Dependency를 제공할 수 있다.
Aggregate는 고유 Bean·AutoConfiguration을 만들지 않는다.

### 금지

- all/full/everything Starter
- 숨겨진 DB/Secret/Web Runtime 포함
- Provider 자동 선택
- 업무 정책 포함
- Consumer survey 없이 Aggregate 증가

## 우선 Profile

- MINIMAL_CONTRACT_CONSUMER
- MINIMAL_BOOT_DOMAIN
- DOMAIN_WEB_API
- SECURE_RESOURCE_API
- BROWSER_BFF_SESSION
- PERSISTENCE_MYBATIS
- EVENT_KAFKA
- EVENT_JMS_IBM_MQ
- EVENT_RABBITMQ
- INTEGRATION_TCP
- OBSERVABLE_RESILIENT_SERVICE
