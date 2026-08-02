# CPF Current Work Request — Post-QA37 Integrated Remediation Primary

- Review baseline `origin/master`: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Branch: `master`
- Current package: `CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
- Current overall state: `부분 구현 / 미검증`
- Codex QA37 focused/static results: reference only; latest exact-SHA final closure is not established
- Official DB Vendors: Oracle, PostgreSQL, MariaDB
- Official Root: `cpf-starters/`
- Permanent direction: Lightweight `cpf-core` + explicit Leaf Starter + Generator Capability Profile + limited Aggregate Starter

## First read order

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/governance/CPF_REQUIREMENT_CONTINUITY_LEDGER.md`
3. `cpf-docs/governance/CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md`
4. `cpf-docs/work/current/CPF_20260802_05_POST_QA37_INTEGRATED_DEVELOPMENT_REQUEST.md`
5. `cpf-docs/work/state/CPF_CODEX_CONTINUITY_STATE.md`
6. `cpf-docs/work/codex/qa38/CODEX_START_HERE.md`

## Current integrated scope

- Reconcile all pushes after QA37 with actual Source/SQL/Test/Config/Frontend/Generator
- Finish Core-to-Starter modularization and real Domain consumers
- Implement Capability Profile and approved Aggregate Starter mechanism
- Restore MQ/JMS/IBM MQ/RabbitMQ/TCP requirements and implementations
- Execute Generator-first Fresh DB lifecycle for Oracle/PostgreSQL/MariaDB
- Complete Java, Frontend, DB, Runtime, Fault, Browser and Supply-chain exact-SHA validation
- Consolidate current/canonical documents and remove only approved stale artifacts
- Update Codex execution/defect/verification history continuously

## Non-negotiable rules

- Fix Canonical Metadata/Generator first; never patch Vendor SQL first.
- Each DB validation starts from a dedicated CPF QA Database/Schema with CPF Object count 0.
- Existing user databases and Docker assets are protected.
- Findings are not completion; actual source remediation and revalidation are required.
- Do not reuse prior PASS as current success unless exact HEAD, command, environment and artifact hashes match.
- No partial implementation, marker-only Starter, consumerless abstraction or dual primary.
- No Commit/Push/Branch/Tag/PR/Reset/Restore/Stash/Clean without explicit user approval.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.
