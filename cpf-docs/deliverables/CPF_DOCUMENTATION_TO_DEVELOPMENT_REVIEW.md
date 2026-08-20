# CPF 문서 검토 중 발견한 개발 검토 요청

기준 Source: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

이 문서는 사용자용 공식 매뉴얼을 실제 Source와 대조하는 과정에서 발견한 **구현 경계 오류 가능성 및 개발자 사용성 개선 후보**를 개발 쪽에서 독립 검토하기 위한 요청서다. 문서 정본을 Source에 억지로 맞추지 않고, 제품 설계 의도와 구현이 충돌하는 경우 개발 검토 대상으로 분리한다.

## DEV-DOC-001 — Generated Domain Generator가 Batch 업무 모듈/샘플을 생성하는 경계 재검토

**우선순위:** 최상

### 사용자/문서 측 확정 개념

- 생성형은 **업무 개발 Domain 생성**을 뜻한다.
- Batch는 Generated Domain의 하위 생성 종류가 아니라, 프로젝트 초기 구성 시 사용할지 선택하는 **Framework Capability / Runtime** 축이다.
- Batch를 사용하는 프로젝트는 설치/구성 단계에서 `cpf-batch`와 필요한 Starter/Config/Runtime wiring을 포함한다.
- Batch를 사용하지 않는 프로젝트는 Batch Runtime/Starter/설정을 포함하지 않는다.
- 실제 Batch 업무 Job/Step은 설치된 Batch Framework의 `@CpfBatchJob`, `@CpfBatchStep`, Base/Public API를 이용해 개발하는 모델이 자연스럽다.

### 현재 Source 근거

`cpf-tools/generator/engine/cpf_domain_generator.py`

- L226~227: `modules.online`, `modules.batch`를 Generated Domain 입력 계약으로 요구하고 둘 중 하나 이상을 true로 강제한다.
- L1599~1616: `d.batch`가 true이면 Generated Domain 내부에 다음을 직접 생성한다.
  - `batch/build.gradle`
  - `*BatchApplication.java`
  - Batch `application.yml` 및 profile 설정
  - Batch Domain Call adapter/sample
  - `SampleBatchService`
  - `SampleBatchJob`
  - `SampleBatchJobContractTest`

관련 Gate도 이 구조를 정본으로 강제한다.

- `cpf-tools/verification/nxt3/verify_root_generated_domain_prefix.py`
- `cpf-tools/verification/nxt3/verify_generator_cross_platform.py`
- `cpf-tools/verification/nxt3/cpf_nxt3_generator_gate.py`

### 문제

현재 구현은 사용자가 이해해야 하는 두 축을 하나의 Generator 입력/출력으로 합친다.

1. 업무 Domain 생성
2. Batch Capability 설치/사용 여부

그 결과 README/Generator Guide/Developer Guide에서 Generated Domain을 설명할 때 `online/batch/domain` 물리 구조까지 함께 설명해야 Source와 맞게 되고, 사용자는 "Batch도 Domain Generator가 만드는 기능"으로 오해하기 쉽다.

### 개발 검토 요청

다음 중 어떤 모델이 CPF 최종 계약인지 정본 기준으로 확정하고 Source/Gate/Generator/문서를 함께 정리한다.

**권고안 A — 축 분리(문서 검토 기준 권고)**

- Domain Generator: `cpf-<domain>` 업무 Domain 골격 생성에 집중.
- Batch Capability: 프로젝트 구성/Installer/Profile에서 선택.
- Batch Job/Step: Generator가 자동으로 업무소스를 만들지 않고 개발자가 Public Batch API와 Template/Sample을 참고해 구현.
- Batch 예제는 `cpf-education` 또는 별도 Sample/Template에 둔다.

**대안 B — Generator가 runtime-specific skeleton도 생성**

이 모델을 유지한다면 “Generated Domain은 업무 Domain을 생성하되 선택한 Runtime용 소스 골격을 함께 생성한다”는 계약을 정본에 명시해야 한다. 현재 사용자가 확정한 개념과는 충돌하므로 사용자 결정 없이 문서에서 이 모델을 확정하면 안 된다.

### 영향 범위

- Generator schema/engine/templates
- NXT3 Generated Domain gates
- `cpf-member`, `cpf-external`
- `settings.gradle`/generated module wiring
- Batch Starter/Profile/Runtime 구성
- Education/Sample
- README / Developer Guide / Batch Developer Guide / Specification

---

## DEV-DOC-002 — Generator preset/옵션 UX를 “개발 목적” 기준으로 단순화 검토

**우선순위:** 높음

### 현재 Source 근거

`cpf-tools/generator/engine/cpf_domain_generator.py` L229~249

- `minimal`, `standard-enterprise`, `full-enterprise`, `custom` preset이 존재한다.
- `standard-enterprise`와 `full-enterprise`는 현재 `mybatis + httpClient + resilience + sampleTransaction=true` Golden Path를 강제한다.
- 별도로 `modules.online/batch`, `persistence`, `cache`, `messaging`, `objectStorage`, `securityProfile` 등 많은 선택축이 존재한다.

### 사용성 문제

신규 개발자가 실제로 먼저 판단하는 질문은 보통 다음이다.

- REST/API가 필요한가?
- DB 접근이 필요한가? 어떤 방식인가?
- 외부 HTTP 연계가 필요한가?
- Cache/Messaging이 필요한가?
- Batch Runtime을 사용할 것인가?
- 인증/인가 유형은 무엇인가?

현재 `preset` 이름과 세부 feature/module 조합이 함께 노출되면 사용자는 "무슨 preset을 골라야 하는지"와 "그 뒤 다시 어떤 기능을 골라야 하는지"를 이중으로 판단해야 한다.

### 개발 검토 요청

- CLI/Generator가 **목적 기반 질문 → 추천 Profile/Starter/Provider 조합**을 제시하는 흐름을 제공할 수 있는지 검토.
- preset은 고급 사용자용 shortcut으로 두고, 기본 UX는 capability 질문형으로 제공하는 방안 검토.
- 선택 결과를 dry-run에서 `포함되는 Starter / Provider / Runtime / 생성 파일 / 필수 설정`으로 사람이 읽기 쉽게 보여주는 기능 검토.

---

## DEV-DOC-003 — Public Starter/Provider 선택 정보를 단일 Canonical 사용자 계약으로 노출

**우선순위:** 높음

### 문제

Repository에는 Starter Catalog, Profile, Provider, BOM, Generator 설정이 존재하지만 일반 개발자가 “내가 원하는 기능에 어떤 공개 Starter를 선택해야 하는가”를 한 군데에서 파악하기 어렵다. 문서가 이를 재구성하고 있으나, Source가 제공하는 machine-readable catalog와 사용자용 선택 모델의 간극이 크다.

### 개발 검토 요청

Canonical Catalog에서 다음 필드를 안정적인 공개 metadata로 제공할 수 있는지 검토한다.

- 사용자 목적/기능명
- Public Profile/Starter
- 선택 Provider
- 필수/선택 관계
- 상호배타/충돌 조합
- 기본 설정 prefix
- Native escape hatch
- 대표 Operations/Facade
- Runtime 의존 여부

이 정보가 안정되면 README/Developer Guide/Generator가 같은 원천에서 기능 선택표를 만들 수 있다.

---

## DEV-DOC-004 — Generated Domain 입력 항목 중 내부 구현 노출 최소화 검토

**우선순위:** 중간

### 사용성 문제

`modules`, `features`, `generation`, `runtime` 등은 구현상 유용하지만, 신규 개발자가 처음 Domain을 만들 때 전부 알아야 하는 항목으로 노출되면 CPF의 진입 난도가 올라간다.

### 개발 검토 요청

- 필수 입력을 `domain name / system code / base package / DB 사용 여부·Vendor / 필요한 capability` 수준으로 축소할 수 있는지 검토.
- 고급 옵션은 advanced config로 분리.
- 생성 전에 선택 결과를 사람이 읽는 Summary로 보여주고, 잘못된 조합은 원인과 권장 대안을 함께 fail-fast 하도록 개선 검토.

---

## DEV-DOC-005 — Sample Transaction/Domain Call 예제의 사용자 가치와 위치 재검토

**우선순위:** 중간

### 문제

현재 Generated Domain은 설정에 따라 Sample Transaction과 Domain Call sample/adapter까지 생성한다. 실제 업무 개발자가 처음 생성한 Domain에서 샘플 파일을 삭제해야 하는 구조라면 초기 Surface가 불필요하게 커질 수 있다.

### 개발 검토 요청

- 생성 결과는 실제 수정할 최소 업무 골격만 두고,
- 다양한 Domain Call/외부연계/Transaction 예제는 `cpf-education` 또는 선택형 `--with-samples`로 이동하는 방안 검토.
- Sample을 생성하는 경우 생성 파일 목록/삭제 가능 여부/업무 코드와의 경계를 명확히 표시.

---

# 개발 쪽 판정 요청

각 항목은 다음 중 하나로 판정해 문서팀에 회신하면 된다.

- `수용 - 개발 반영`
- `부분 수용 - 대안 반영`
- `현행 유지 - 정본 계약 근거 제공`
- `보류 - 추가 결정 필요`

특히 **DEV-DOC-001은 README/Developer/Batch/Generator/Specification의 개념 경계를 결정하므로 우선 판정이 필요**하다.

---

## DEV-DOC-006 — 이름이 같은 `CpfBatchJob` 공개 Annotation 2종의 개발자 혼동 가능성

**우선순위:** 높음

### 문서 검수 중 발견한 Source 사실

현재 `cpf-batch/api`에는 단순 이름이 같은 공개 Annotation이 두 종류 존재한다.

1. `com.cpf.batch.api.CpfBatchJob`
   - Target: `METHOD`, `TYPE`
   - 속성: `id`, `name`, `ownerDomain`
   - `cpf-education`의 Spring Batch `Job` Bean 예제가 사용한다.
2. `com.cpf.batch.api.annotation.CpfBatchJob`
   - Target: `TYPE`
   - 속성: `value`, `restartable`, `maxConcurrentExecutions`
   - `cpf-member`, `cpf-external`의 Runtime Handler형 Sample이 사용한다.

또한 `com.cpf.batch.api.annotation.CpfBatchStep`은 Method 단위 `value`, `order`, `idempotent` 정책을 제공한다.

### 사용성 문제

IDE 자동완성이나 단순 문서 표기에서 `@CpfBatchJob`만 보이면 신규 개발자가 서로 다른 두 계약을 혼동하기 쉽다. 실제로 문서 검수 과정에서도 package를 생략한 예제가 잘못된 속성/Target과 결합될 가능성이 확인됐다. 이는 단순 문서 문제가 아니라 Public API 명명 Surface의 사용성 문제다.

### 개발 검토 요청

- 두 Annotation이 모두 장기 Public API여야 하는지 검토한다.
- 하나가 Legacy/호환 목적이면 Deprecated 및 Migration 경로를 명확히 한다.
- 둘 다 필요하다면 역할이 드러나는 이름으로 분리하거나, Catalog/IDE 문서에서 FQCN·Consumer·사용 Scenario를 강제 노출한다.
- `cpf-education`, Generated regression domain, Batch Guide, Specification, Test가 같은 선택 규칙을 사용하도록 맞춘다.

### 문서 측 선조치

현재 배치 개발자 가이드와 Specification은 두 계약을 FQCN과 속성 기준으로 구분해 설명하고, package 없는 모호한 예제를 제거했다. Source 계약 통합 여부는 개발 판정 후 다시 현행화한다.
