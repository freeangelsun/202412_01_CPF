# SELF:SELF-QA-011 Developer Closure Evidence

- QA source: `SELF`
- Finding ID: `SELF-QA-011`
- Severity: `P1`
- Category: `Generated Domain / Canonical Source`
- Title: Generated Domain root cpf-domain.yaml 단일 정본 정책과 Tool-side byte-identical 복제 정본/Guide가 충돌
- Developer closure state: `CLOSED`
- Development status: `완료`
- Verification status: `완료`
- Runtime status: `해당 없음/계약 검증 완료`
- Source identity: `c927382e9bd2b559a306e3ccf33183492190a83fbf11255b3a81f0a72c131f3a`

## Original acceptance

Current logical definition은 cpf-<domain>/cpf-domain.yaml exactly one. tool definitions current consumer 0. MBR(batch=true) batch 존재, EXS(batch=false) batch 없음, EXS의 domainDependencies.member client만 선택적으로 존재. diff clean=true, normalized parity PASS, empty dir 0.

## Current source / consumer scope

cpf-member/cpf-domain.yaml; cpf-external/cpf-domain.yaml; cpf-tools/generator/definitions/member/*; cpf-tools/generator/definitions/external/*; cpf-docs/development/GENERATOR_GUIDE.md; cpf-tools/README.md

## Current verification evidence

- `cpf-docs/work/evidence/development-22/logs/CANONICAL_STATIC_VERIFIERS.json`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_DB.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_GENERATOR.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_RELEASE.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_RUNTIME_SECURITY_VERIFICATION.log`
- `cpf-docs/work/evidence/development-22/logs/PYTEST_TESTING_TOOLS.log`
- `cpf-docs/work/evidence/development-22/logs/FRONTEND_CONTRACTS.log`
- `cpf-docs/work/evidence/development-22/logs/FRESH_REPLAY_GATES.log`

## QA-source evidence reference

QA package matrix/report 참조
