# Execution Gate — REV-003

## Gate A: 개발GPT 직접 수행

FDEV-001~FDEV-003, FDEV-007~FDEV-016, FDEV-018~FDEV-025는 개발GPT가 실제 구현·실행·자체검수한다.

## Gate B: 개발GPT 대체검증

FDEV-004, FDEV-005, FDEV-006, FDEV-017은 다음 Gate를 통과하면 개발GPT 역할 완료다.

- 구현·Config·Script 완성
- 현재 환경에서 가능한 검증 전부 수행
- Target Runtime Preflight 성공
- exact rerun command
- 환경·권한·Secret 요구
- 성공·실패 기준
- Evidence Template
- 한계와 미검증 범위 기록

## Gate C: Codex Target Runtime

Codex가 실제 Java25/Gradle9.1, DB3, Browser, Multi-process를 실행한다.

Codex 환경 부재 시 QA/인프라/DBA로 이관하며 개발GPT에 동일 실행을 되돌리지 않는다.

## Gate D: QA

QA는 최신 master에서 실제 구현과 실행 Evidence를 검수한다. QA 통과 전 전체 완료 금지.

## Gate 0 and Gate E: Canonical Integration

### Gate 0 — 변경 전

모든 Requirement는 제품 변경 전에 `CANONICAL_INTEGRATION_CONTROL.md`의 사전 통합 검증을 통과해야 한다.

### Gate E — 변경 후

각 변경 완료 후 통합 원장 append/upsert, Consumer/Request/Evidence 연결, Index/Part Hash, orphan/duplicate 검증을 다시 수행한다.

Gate E가 실패하면 해당 Requirement의 개발GPT 역할 완료를 인정하지 않는다.

- `FDEV-025`: `python cpf-tools/verification/verify_starter_catalog.py --root .` 및 BOM exact equality Gate
