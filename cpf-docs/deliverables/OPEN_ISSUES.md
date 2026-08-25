# CPF Developer GPT Open Issues — 2026-08-25

## VERIFICATION_PENDING — 필수 로컬 Runtime

Source 개발과 실행 가능한 정적 Gate는 완료했다. 다만 Java25/Docker/DB3/Browser 물리 Runtime을 assistant 환경에서 실행하지 못했으므로 전체 QA Closure는 아직 `VERIFICATION_PENDING`이다.

완료 조건은 `run-cpf-required-full-runtime-validation.ps1` ExitCode 0, Final PASS, Oracle/PostgreSQL/MariaDB 전체 Fresh/Upgrade/Rollback-Recovery + 실제 거래, Batch/CEC 장애복구, Approval/Browser/Open Git Fresh Consumer를 모두 통과하는 것이다.

## Codex 독립 재검수 PENDING

Codex 전용 문서/원장/Evidence는 변경하지 않았다. Codex 재개 시 Developer GPT 수정영역을 최신 Source에서 독립 cross-check해야 한다.

## 보호정책과 Path Length 충돌

Root-relative 200자 초과 파일 47개가 모두 기존 `cpf-docs/work/evidence/codex/current/**`의 Codex 보호 Evidence다. Developer GPT 보호규칙 때문에 rename/delete하지 않았다. 일반 신규/변경 Source와 Fresh projection에는 200자 초과를 추가하지 않는다. Codex 재개 시 Evidence path currentization을 별도 cross-check한다.
