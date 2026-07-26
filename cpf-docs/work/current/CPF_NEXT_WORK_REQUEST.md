# CPF Next Work Request

다음 작업은 **R14 적용 후 최신 master 전수 통합검증 + 잔존 P0/P1 Closure**다.

Codex/ChatGPT는 이전 대화 없이 다음 파일을 먼저 읽는다.
1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`
3. `cpf-docs/work/state/CPF_R14_HANDOVER.md`
4. `cpf-docs/review/CPF_MASTER_FULL_DEFECT_AUDIT_20260726.md`
5. `cpf-docs/work/current/CPF_CODEX_R14_INTEGRATED_VERIFICATION_REQUEST.md`

작업자는 QA 항목을 기계적으로 체크하지 말고 실제 Owner/Consumer/Runtime 계약을 따라 원인을 묶어 수정한다. 검증 중 새 결함을 발견하면 같은 작업에서 고치는 것을 기본으로 한다.

우선순위는 보안/False Success/결과불명/동시성/데이터 유실/감사/DB Drift/Release Gate → 운영성/성능 → 사용성 순이다. 새로운 거대 기능은 고객가치가 명확한 경우만 추가한다.
