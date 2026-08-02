
# CPF Starter Profile·Bundle 설계 검토

- 기준 SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- 목적: Domain이 필요한 Starter를 선택할 때 개별 등록과 그룹 등록을 모두 지원하되 Core 경량화와 Optional성을 보존한다.
- 현재 상태: `development_status = 미구현`, `verification_status = 미검증`

## 1. 결론

단일 방식으로 고정하지 않는다.

```text
정밀 제어가 필요함
  → Leaf Starter 개별 선택

표준 사용 사례를 빠르게 생성함
  → Generator Capability Profile 선택
  → Build와 Manifest에는 해석된 Leaf Starter 목록 기록

외부 Consumer가 하나의 Dependency를 요구함
  → 필요성이 입증된 경우 Aggregate Starter 선택 제공

버전만 맞춤
  → Platform BOM
```

권장 우선순위는 **Generator Profile → Leaf Starter 명시 확장**이다.
Aggregate Starter는 편하지만 불필요 Dependency와 묵시적 변경 위험이 있으므로 보조 수단으로만 검토한다.

## 2. 왜 BOM만으로는 부족한가

BOM은 버전을 정렬할 뿐 Runtime Dependency를 추가하지 않는다.

```gradle
implementation platform("com.cpf.platform:cpf-platform-bom:<version>")
implementation "com.cpf.starter:cpf-starter-webmvc"
```

따라서 BOM은 반드시 필요하지만 Starter 묶음 등록 기능 자체는 아니다.

## 3. Generator Profile 방식

사용자는 다음처럼 사용 사례를 선택할 수 있다.

```text
DomainName = payment
Profiles = DOMAIN_WEB_API, DOMAIN_EVENT_KAFKA
SecurityMode = RESOURCE_SERVER
Persistence = MYBATIS
Cache = NONE
```

Generator는 다음을 원자적으로 만든다.

- `build.gradle`의 Leaf Starter Dependency
- Profile과 Resolved Starter 목록을 가진 Domain Manifest
- 필요한 Config만 생성
- 조합별 Test
- 설치·운영 Guide
- Provider 충돌 검증
- 사용자 수정 영역 보호

예시 해석 결과:

```text
DOMAIN_WEB_API
  → cpf-starter-webmvc
  → cpf-starter-openapi-webmvc

DOMAIN_EVENT_KAFKA
  → cpf-starter-messaging-kafka
  → cpf-starter-observability

RESOURCE_SERVER
  → cpf-starter-security-resource-server

MYBATIS
  → cpf-starter-persistence-mybatis
```

## 3.1 Core와 Base Starter Profile

Profile Catalog는 다음 최소 Profile을 구분한다.

```text
MINIMAL_CONTRACT_CONSUMER
  cpf-core 직접 소비
  Spring Boot Runtime 없음

MINIMAL_BOOT_DOMAIN
  cpf-starter-base
  Web·DB·Security·Messaging·Cache 없음
```

`DOMAIN_WEB_API`, `DOMAIN_EVENT_KAFKA` 같은 Boot Profile은 기본적으로 `cpf-starter-base`를 포함할 수 있다.
다만 Aggregate Starter와 Generator Profile 양쪽에서 Base를 중복 정본화하지 않고 Catalog 하나에서 해석한다.

## 4. Aggregate Starter 방식

필요성이 입증되면 다음과 같은 빈 조립 Artifact를 둘 수 있다.

```text
cpf-starter-profile-domain-web-api
cpf-starter-profile-domain-event-kafka
cpf-starter-profile-browser-session-runtime
```

이 Artifact는 Source 구현이나 AutoConfiguration을 두지 않고 Leaf Starter Dependency만 가진다.

```gradle
dependencies {
    api project(":cpf-starter-webmvc")
    api project(":cpf-starter-openapi-webmvc")
}
```

단, 실제 Artifact 이름과 조합은 다음 QA에서 확정한다.

## 5. 금지 조합

- Kafka와 RabbitMQ를 하나의 일반 Profile에서 동시 활성화
- Caffeine과 Redis를 같은 Provider Profile로 동시 활성화
- Session JDBC와 Resource Server를 기본 Security Profile에 동시 포함
- 모든 Starter를 포함하는 `cpf-starter-all`
- ADM/BZA 권한·메뉴·승인 로직을 범용 Profile에 포함
- Batch Job·Scheduler·Lease를 Starter Profile에 포함
- Profile Version 변경으로 기존 Domain Dependency를 무통보 변경

## 6. Version·Drift 관리

Domain Manifest 예시:

```json
{
  "starterSelection": {
    "profiles": [
      {"id": "DOMAIN_WEB_API", "version": "1.0"}
    ],
    "resolvedStarters": [
      {"artifact": "cpf-starter-webmvc", "version": "1.0.0"},
      {"artifact": "cpf-starter-openapi-webmvc", "version": "1.0.0"}
    ]
  }
}
```

검증 Gate:

- Profile Catalog와 Generator 해석 결과 일치
- Aggregate Starter POM과 Profile Catalog의 Leaf 목록 일치
- BOM에 모든 Leaf/Aggregate Artifact 버전 존재
- 미선택 Starter의 전이 Dependency 0
- 상호 배타 Provider 동시 선택 0
- Profile Upgrade 전후 Diff·Rollback Evidence
- JAR/WAR 실제 포함 목록과 Manifest 일치

## 7. 다음 QA 판정 항목

1. Generator Profile만 제공할지
2. Aggregate Starter도 제공할지
3. 초기 공식 Profile Catalog
4. Profile 합성·Override 규칙
5. Profile Version과 Compatibility
6. 외부 고객 프로젝트의 Gradle/Maven 사용법
7. Minimal Domain Footprint 예산
8. Guide·EDU·Deliverable 반영 범위
