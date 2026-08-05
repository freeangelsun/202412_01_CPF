# DEVGPT-6A Test and Evidence

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- Campaign baseline SHA: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c`
- Session: `DEVGPT-6A`
- Scope: Work Item 101 / Canonical 20 / CPF-FR 7,523 / CPF-SC 10,690 / Engineering Gate 19
- 중앙 V8 상태 원장 수정: 없음
- Local Working Tree: Git metadata 없는 Connector 기반 최소 Workspace. Remote exact SHA는 GitHub Connector로 확인.

## 제품 변경 및 검증 결과

| Functional Slice | 직접·대체검증 | 실제 결과 |
|---|---|---|
| ADM OpenAPI/Generated Client | Node semantic/runtime gates | 16/16, Exit 0; 321 Operations, 153 Mutations |
| ADM Notification/Cache/Message/Reference Consumer | Generated client runtime fixtures | raw ADM URL 0, actor spoof 차단, reason/version/body/query 계약, Exit 0 |
| ADM Route/UX Contract | Route consumer boundary fixture | 62 Routes, 263 explicit operation refs, Exit 0 |
| BZA Generated Client | Node semantic/runtime gates | 6/6, Exit 0; 84 Operations, 38 Mutations |
| ADM/BZA strict TypeScript | `tsc --noEmit --strict` minimal contract workspaces | ADM Exit 0, BZA Exit 0 |
| OpenAPI Web MVC Starter | Java21 independent compile/runtime Harness | 14 checks, parsed PathPattern/secure defaults, Exit 0 |
| Controller-source OpenAPI Generator | Python unit/static + actual 321 operation regeneration | missing/duplicate 0, query/header/body preserved |
| Scope/ledger | exact-ID validator | Work Item/FR/SC/Gate 누락·중복·미귀속 0 |

## 명령과 Evidence

- ADM Gate: `evidence/ADM_GENERATED_CLIENT_GATES.log`
- BZA Gate: `evidence/BZA_GENERATED_CLIENT_GATES.log`
- strict TypeScript: `evidence/STRICT_TYPESCRIPT.log`
- Starter Java Harness: `evidence/OPENAPI_WEBMVC_STARTER_GATES.log`
- Python/Node/JSON 정적검사: `evidence/STATIC_VALIDATION.log`
- Scope Build/검산: `evidence/SCOPE_BUILD.log`, `evidence/SCOPE_VALIDATION.log`
- 환경과 직접 실행 실패: `evidence/ENVIRONMENT_AND_DIRECT_ATTEMPTS.log`
- Remote baseline: `evidence/REMOTE_BASELINE_CONFIRMATION.json`
- Final Root Overlay Node Gate: `evidence/FINAL_STAGE_FUNCTIONAL_GATES.log`
- Final Root Overlay strict TypeScript: `evidence/FINAL_STAGE_STRICT_TYPESCRIPT.log`
- Final Root Overlay Starter Harness: `evidence/FINAL_STAGE_OPENAPI_STARTER.log`
- Final Root Overlay package validation: `evidence/FINAL_OVERLAY_VALIDATION.json`

## 목표 환경 직접검증 실패와 대체검증

1. `git clone`/`git ls-remote`: DNS 오류 `Could not resolve host: github.com`.
2. `npm ci`: Node `22.16.0`이 요구 `>=22.18.0`보다 낮고 내부 Registry `zod-4.4.3.tgz` 404로 Exit 1.
3. Java25/full Gradle/Spring Runtime: Java21만 존재하여 실행하지 못함.
4. Browser/Playwright/a11y Runtime: Browser·설치 의존성 부재로 실행하지 못함.
5. Oracle/PostgreSQL/MariaDB Runtime: 공식 DB 환경 부재 및 6E 소유권으로 실행하지 못함.

이 차이는 성공으로 확대하지 않았다. 각 Requirement와 Scenario의 `remaining_gap`에 개별 기록했고, 가능한 Node AST/semantic/runtime, strict TypeScript, Java21 independent Harness, Python Generator Fixture를 실행했다.

## 상태 판정 의미

- `개발GPT_수행상태=완료`: 제품 Source·Test·Consumer 또는 세션 소유 구현을 완료했음을 의미한다.
- `개발GPT_자체검수상태=완료`: 해당 ID를 개별 검토하고 가능한 직접·대체검증으로 판정했음을 의미한다.
- `remaining_gap`: 목표 Java25/Browser/3 Vendor/전체 npm 환경에서만 남는 검증 차이다.
- QA 최종 완료가 아니며 Codex 독립검수와 최신 통합 Git QA가 필요하다.
