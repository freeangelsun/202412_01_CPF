# CPF 문서·디렉터리 통제 정책

- 기준 SHA: `2a86ff42799eddffaae87d38ae68632726a2c495`
- 갱신 시각: `2026-08-02 22:52:18 KST`
- 목적: 내용을 잃지 않으면서 같은 역할의 문서와 폴더가 작업 회차마다 늘어나는 것을 금지한다.

## 1. 활성 정본

| 역할 | 정본 |
|---|---|
| 최상위 제품 목표 | `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md` |
| 문서·경로 통제 | `cpf-docs/governance/CPF_DOCUMENT_CONTROL_POLICY.md` |
| 현재 개발·QA 통합 정본 | `cpf-docs/work/CPF_CURRENT_WORK_REQUEST.md` |
| Requirement | `cpf-docs/work/CPF_REQUIREMENT_MATRIX.csv` |
| Scenario | `cpf-docs/work/CPF_SCENARIO_MATRIX.csv` |
| Source Findings | `cpf-docs/work/CPF_SOURCE_FINDINGS.csv` |
| Starter Value | `cpf-docs/work/CPF_STARTER_VALUE_CATALOG.csv` |
| Public Surface | `cpf-docs/work/CPF_PUBLIC_SURFACE_CATALOG.csv` |
| Stage Plan | `cpf-docs/work/CPF_STAGE_PLAN.csv` |
| Change Manifest | `cpf-docs/work/CPF_CHANGE_MANIFEST.csv` |
| Product Delete Work Items | `cpf-docs/work/CPF_PRODUCT_DELETE_WORK_ITEMS.csv` |
| 정리 대상 | `cpf-docs/work/CPF_DELETE_MANIFEST.txt` |
| Package Hash | `cpf-docs/work/CPF_PACKAGE_MANIFEST.json` |

## 2. 문서 생성 제한

- 같은 역할의 파일이 있으면 새 파일을 만들지 않고 기존 정본을 갱신한다.
- 날짜, 세션, QA 번호, R1/R2/FINAL을 이름에 붙여 복제하지 않는다.
- 작업 전·후 리뷰, 개발 보고, 자체 리뷰, Handover, Continuity는 현재 통합 정본의 절로 관리한다.
- 신규 파일은 기존 정본에 통합할 수 없는 독립 역할, Owner, Consumer, 폐기 조건이 모두 있을 때만 허용한다.
- 완료 회차의 상세 경과는 Git History로 보존한다.

## 3. 디렉터리 생성 제한

- 작업 회차나 AI 세션을 이유로 새 디렉터리를 만들지 않는다.
- `current`, `state`, `handover`, `review`, `codex/<qa>`, `manifest`처럼 동일 내용을 분산시키는 구조를 재생성하지 않는다.
- 기계 검증 파일도 `cpf-docs/work` 한 곳에서 관리한다.
- 빈 폴더는 Git에 남기지 않는다.

## 4. 보호 경로

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

위 경로는 다른 Owner의 관리 범위다. 수정·이동·이름 변경·통합·삭제·자동 포맷·Stage를 금지한다.

## 5. 삭제 안전

- 최신 정본에 유효 내용이 통합된 파일만 삭제한다.
- exact path Manifest만 사용한다.
- 보호 경로 침범 시 fail-closed한다.
- Wildcard, `git clean`, `reset`, `restore`, `stash`를 금지한다.
- 삭제 후 Broken Link와 Script 참조를 검증한다.
- Commit·Push는 사용자 별도 승인 없이는 수행하지 않는다.

## 6. 완료 보고

모든 개발·QA 작업은 새 파일 수, 갱신한 정본, 통합한 파일, 삭제 후보,
남은 폴더별 역할, 빈 폴더·Stale Evidence 여부를 통합 정본에 기록한다.
