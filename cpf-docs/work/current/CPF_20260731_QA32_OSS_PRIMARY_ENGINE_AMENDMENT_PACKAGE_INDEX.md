# QA32 OSS Primary Engine Amendment — Package Index

- Package ID: `CPF-20260731-QA32-OSS-PRIMARY-ENGINE-AMENDMENT`
- Baseline Source: `4f675c7f89998cdbba7202e6c83320a0a4421a1f`
- Purpose: Spring Batch를 CPF 전체 Batch Primary Execution Engine으로 정정하고, 모든 `ADOPT_NOW` OSS에 실제 Consumer 이관·Legacy 제거 원칙을 강화한다.

## Overlay Files

1. `cpf-docs/architecture/ADR_OSS_FIRST_PLATFORM_DIRECTION.md` — replacement
2. `cpf-docs/architecture/CPF_BUILD_VS_BUY_MATRIX.md` — replacement
3. `cpf-docs/quality/CPF_20260730_QA32_OSS_MIGRATION_MATRIX.csv` — replacement
4. `cpf-docs/work/current/CPF_20260731_QA32_OSS_PRIMARY_ENGINE_STEERING.md` — new authoritative steering
5. `cpf-docs/quality/CPF_20260731_QA32_OSS_PRIMARY_ENGINE_CHANGE_MATRIX.csv` — new decision delta
6. `cpf-tools/scripts/apply-cpf-qa32-oss-primary-engine-amendment.ps1` — fail-closed patch for Requirement/Scenario/current instruction/handover files
7. `cpf-docs/work/manifest/CPF_20260731_QA32_OSS_PRIMARY_ENGINE_AMENDMENT_MANIFEST.json` — package hash manifest

## Apply

Repository root에서 ZIP을 덮어쓴 뒤 다음을 실행한다.

```powershell
pwsh -NoProfile -File cpf-tools/scripts/apply-cpf-qa32-oss-primary-engine-amendment.ps1 -Root .
```

스크립트는 기준 문구가 정확히 1회 존재할 때만 Requirement·Scenario를 변경한다. 이미 적용된 경우에는 재적용하지 않으며, 기준이 달라졌으면 임의 수정하지 않고 실패한다.

현재 개발 작업자는 새 Steering 문서를 즉시 읽고 진행 중 구현·Matrix·Evidence에 반영해야 한다. 사용자 승인 없이 Commit·Push·Branch·Tag·PR을 생성하지 않는다.
