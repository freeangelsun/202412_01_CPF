# CPF 20260728_01 Validation Ledger

| 구분 | 상태 | 현재 세션 실제 수행 | 최신 master 적용 후 필수 |
|---|---|---|---|
| Baseline master 확인 | 완료 | `ca3cf8a12290903cc482b5e092cdb43e6bf8f1eb` GitHub 정본 대조 | 적용 직전 HEAD 재확인 |
| QA 수량 | 완료 | 1,214 requirements + 201 scenarios | 없음 |
| Overlay path | 완료 | Root-relative 구조 구성 | unzip 후 `git diff` 확인 |
| Canonical JSON | 완료 | JSON parse, 162 tables 확인 | canonical/generator parity gate |
| Java syntax smoke | 완료 | JDK21 dependency-less parse smoke | Java25 Gradle clean test assemble |
| PowerShell scripts | 미검증 | 현재 환경에 `pwsh` 없음 | Windows pwsh 실행 |
| MariaDB | 미검증 | DB runtime 없음 | install/migrate/runtime/rollback/reapply |
| PostgreSQL | 미검증 | DB runtime 없음 | install/migrate/runtime/rollback/reapply |
| Oracle | 미검증 | DB runtime 없음 | install/migrate/runtime/rollback/reapply |
| ADM/BZA Frontend | 미검증 | Browser/Node full repo 없음 | build/test/browser |
| Runtime multi-instance | 미검증 | 다중 WAS 환경 없음 | 2+ instance ACK/fencing/drift |
| BAT distributed | 미검증 | standalone runtime 미실행 | lease/takeover/checkpoint |
| Generated Domain | 미검증 | full generator lifecycle 미실행 | create/build/db/runtime/remove/regenerate |
| Final QA 1,415 PASS | 재확인 필요 | 구현 Change Set 작성, 전체 실행검증 아님 | Codex full validation + repair |

`완료`는 실제 수행한 범위만 표시한다. `미검증`을 PASS로 승계하지 않는다.
