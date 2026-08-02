# Guide·산출물 Starter 반영 요청

## 목적

Core 경량화와 Starter 세분화 결과를 고객·개발자·운영자·설치 담당자가 실제로 사용할 수 있도록 역할별 정본에 반영한다.
현재 Source보다 먼저 확정 문구를 만들지 않으며, 다음 QA의 실제 Artifact·설정·Generator 결과에 맞춰 갱신한다.

## 필수 내용

- Starter의 정의와 독립 Library JAR 성격
- 어떤 기능을 사용할 때 어떤 Starter를 연결하는지
- Product JAR/WAR에 포함되는 방식
- 필요한 Starter만 선택하고 미선택 기능은 제외하는 원칙
- Core/Common/Product/Starter/Customer Plugin의 Ownership 경계
- Starter별 Gradle Dependency, 필수/선택 Properties, secure default
- 정상·실패·재시도·복구·Readiness·운영 확인 방법
- Generator Capability 선택과 Domain Manifest
- BOM·Version·Compatibility·Install·Upgrade·Rollback
- RabbitMQ 공식 지원 여부와 Kafka Primary 정책
- Starter 세분화 변경 시 Migration Guide

## 작성 방식

- 기존 역할별 Guide를 갱신하고 별도 날짜 문서를 누적하지 않는다.
- Source에 없는 Starter 이름이나 설정을 미리 확정하지 않는다.
- Sample Dependency 한 줄로 완료 처리하지 않는다.
- Consumer·Runtime·운영 Evidence가 없는 기능은 `부분 구현` 또는 `미검증`으로 표시한다.
- `STARTER_USAGE_AND_SELECTION_GUIDE_DRAFT.md`와 `STARTER_DOCUMENT_AND_DELIVERABLE_UPDATE_MATRIX.csv`를 입력으로 사용한다.

## 검증

- Guide Dependency 좌표와 BOM Artifact 일치
- Property 이름과 Configuration Metadata 일치
- Generator 선택과 생성 결과 일치
- JAR/WAR 포함/제외 예제 실제 검증
- Link·OpenAPI·JavaDoc·EDU drift 0

## Profile·Bundle 필수 반영

역할별 Guide에는 다음 차이를 같은 용어로 설명한다.

- Leaf Starter: 실제 기술 구현 JAR
- Capability Profile: Generator가 Leaf Starter 목록으로 확장하는 사용 사례 선언
- Aggregate Starter: 필요 시 한 Dependency로 제공하는 편의 Artifact
- Platform BOM: Version 정렬 전용

개발자 매뉴얼과 Generator Guide에는 다음 예제를 포함한다.

1. Starter 개별 선택
2. Profile 선택과 해석된 Leaf 목록
3. Aggregate Starter 사용 시 전이 Dependency 확인
4. BOM만 사용했을 때 Runtime이 활성화되지 않는 예
5. 상호 배타 Provider 조합 실패 예
6. Profile Upgrade·Rollback과 Manifest Diff

Guide는 Profile 이름만 나열하지 않고 실제 Source Artifact, Config, Consumer, Packaging 결과와 연결해야 한다.

## Core·Base Starter 필수 설명

모든 역할별 Guide는 다음을 동일하게 설명한다.

- `cpf-core`: Spring Boot 없는 독립 계약 Library
- `cpf-starter-base`: 일반 CPF Boot Runtime의 최소 진입점 후보
- `cpf-common`: 실제 고객 업무 공통이 필요한 경우만 선택
- Leaf Starter: 선택 기술 Runtime
- Profile/Aggregate/BOM: 선택 편의와 Version 정렬 계층

개발자·Generator Guide에는 `MINIMAL_CONTRACT_CONSUMER`와 `MINIMAL_BOOT_DOMAIN`의 Build·Manifest·Classpath 차이를 실제 예제로 제공한다.
