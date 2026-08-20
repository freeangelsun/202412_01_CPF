# CPF 문서 검토 중 발견한 개발자 사용성 개선 의견

기준 Source: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

공식 사용자 문서를 실제 Source와 대조하면서 “기능은 존재하지만 신규 개발자가 쓰기 어렵거나 선택이 불명확한 지점”을 별도로 정리한다. 구현 결함으로 확정하지 않고 개발/UX 검토 대상으로 전달한다.

## UX-001 Starter/Profile/Provider 선택을 목적 기반으로 안내
- 현재 Catalog는 정교하지만 신규 개발자가 내부 artifact 이름을 먼저 이해해야 한다.
- CLI/Generator 첫 질문을 `API / DB / Cache / Messaging / External / Security / Batch 사용 여부`처럼 업무 목적 기준으로 제공하는 방안을 권고한다.
- 선택 완료 화면에는 실제 포함되는 Public Profile/Provider, 필수 설정, 충돌 조건을 사람이 읽을 수 있게 보여주는 것이 좋다.

## UX-002 `minimal` preset의 의미를 실제 최소 구성과 일치
- 문서 검토 중 probe에서 minimal 선택 시 기대보다 많은 feature가 활성화되는 동작이 확인되었다.
- minimal은 정말 최소 Surface를 만들거나, 이름/설명을 실제 동작에 맞게 바꾸는 편이 신규 사용자 혼란을 줄인다.

## UX-003 Generated Domain 생성 결과 최소화
- 신규 업무 Domain 생성 직후 삭제해야 하는 sample/adapter가 많으면 첫 경험이 복잡해진다.
- 실제 수정할 최소 업무 골격만 기본 생성하고, 풍부한 예제는 `cpf-education` 또는 명시적 sample 옵션으로 분리하는 것을 권고한다.

## UX-004 Public Capability Catalog를 사용자용 Summary로 노출
- `공통코드/파라미터/영업일/Transaction/Persistence/Cache/Messaging/...`를 “무엇을 할 수 있는가 → 언제 선택하는가 → 대표 API”로 보여주는 canonical metadata가 있으면 README/Generator/문서가 같은 사실을 사용할 수 있다.

## UX-005 오류 메시지를 해결 행동까지 연결
- Generator/CLI validation 오류는 잘못된 field만 말하기보다 `현재 선택`, `충돌 이유`, `권장 조합`, `수정할 YAML 경로`를 함께 보여주는 것이 좋다.

## UX-006 Internal Starter를 사용자 Surface에서 숨김
- internal leaf는 build.gradle 추천/자동완성/문서 front-page에 나오지 않도록 하고 Public Profile/Provider 중심으로 유지한다.

## UX-007 Batch 선택과 업무 Domain 생성을 분리된 질문으로 표현
- 사용자에게는 `업무 Domain 생성`과 `이 프로젝트가 Batch Runtime을 사용하는가`를 서로 다른 축으로 보여주는 것이 직관적이다.
- Source가 runtime-specific skeleton을 유지하더라도 UI/문서에서는 두 개념이 한 개념처럼 보이지 않게 설계할 필요가 있다.
