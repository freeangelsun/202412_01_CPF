# CPF Review / Progress / Completion Guide

## 1. 판정 상태

허용 상태는 `완료 / 부분 구현 / 미구현 / 미검증 / 실패 / 재확인 필요`뿐이다.

## 2. 완료 최소 조건

Requirement는 Source/Class가 있다는 이유로 완료하지 않는다. 적용 가능한 범위에서 다음을 함께 확인한다.

`Requirement → Decision → Owner → Source/API/SPI → Consumer → SQL/Config/Migration → Test → Runtime/Browser → Evidence → Guide`

그리고 역방향으로 구현물이 어떤 Requirement/Owner/Consumer를 위해 존재하는지 확인한다.

## 3. Requirement 연속성

- Canonical Requirement ID는 Mapping 없이 삭제/rename하지 않는다.
- 기준 Catalog는 162 canonical + 8 legacy alias다.
- Alias는 완료율에 중복 집계하지 않는다.
- Merge/Split/Supersede는 `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`에 기록한다.
- Count 감소는 명시적 폐기 승인 또는 Mapping 없이는 허용하지 않는다.

## 4. Codex 구현 보고

주요 Requirement마다 기존 상태, 유지/보완/확장/교체/제거 판정, Architecture, Owner, 실제 Consumer, SQL/API/Config 영향, 선택 이유와 대안, 호환성/Migration/Rollback, 실제 실행 검증, Evidence, 잔여 Gap, 변경 파일을 보고한다.

## 5. Cross-PC 검수

HOME/COMPANY의 Local DB/Runtime 상태를 분리한다. 다른 PC 또는 이전 Commit의 Evidence를 현재 성공으로 승계하지 않는다. Continuity State에 없는 과거 구현도 Git/요청서/Evidence에서 발견하면 복구한다.

## 6. 실패를 숨기지 않는다

삭제한 잘못된 fallback 때문에 Test/Runtime이 깨지면 해당 Consumer를 Gap으로 기록하고 최종 Architecture로 고친다. 삭제 파일을 복원해 Green 상태를 만드는 것은 완료가 아니다.
