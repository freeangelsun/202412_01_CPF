# CPF QA32 Evidence Schema

## 1. Evidence 파일 단위

각 Requirement 또는 Scenario 실행은 하나 이상의 JSON Evidence를 `cpf-docs/evidence/current/` 아래에 보관한다.

권장 파일명:

`<requirement-id>__<scenario-id>__<environment>__<timestamp>.json`

## 2. 필수 필드

```json
{
  "schemaVersion": "CPF-QA32-EVIDENCE-1",
  "requirementId": "OSS-GWY-001",
  "scenarioId": "QA32-S000",
  "defectIds": ["QA32-D000"],
  "repository": "freeangelsun/202412_01_CPF",
  "branchOrRef": "master",
  "sourceSha": "40-hex",
  "sourceClean": true,
  "environment": {
    "os": "...",
    "java": "...",
    "gradle": "...",
    "node": "...",
    "npm": "...",
    "powershell": "...",
    "database": "...",
    "browser": "..."
  },
  "invocation": {
    "command": "...",
    "workingDirectory": ".",
    "startedAt": "ISO-8601",
    "finishedAt": "ISO-8601",
    "exitCode": 0
  },
  "expected": "...",
  "actual": "...",
  "status": "PASS|FAIL|NOT_EXECUTED|BLOCKED",
  "artifacts": [
    {"path": "...", "sha256": "...", "kind": "log|report|jar|sbom|trace|screenshot"}
  ],
  "runtimeFacts": {},
  "sanitization": {"secretsRemoved": true, "method": "..."},
  "blockers": [],
  "reviewedBy": "..."
}
```

## 3. 상태 규칙

- `PASS`: 실제 실행, Exit 0, Expected와 Actual 일치, 증적 파일 존재
- `FAIL`: 실행됐으나 기대 불일치 또는 Gate 실패
- `NOT_EXECUTED`: 환경·시간·도구 등의 이유로 실행하지 않음
- `BLOCKED`: 선행 결함이나 외부 의존성으로 실행 불가

`NOT_EXECUTED`와 `BLOCKED`는 완료가 아니다.

## 4. 요구사항 Result Matrix 필수 열

- requirement_id
- priority
- implementation_status
- validation_status
- final_status
- source_sha
- changed_paths
- legacy_removed
- static_gate_evidence
- integration_evidence
- runtime_evidence
- browser_evidence
- security_evidence
- artifact_evidence
- unresolved_blocker
- reviewer_note

## 5. OSS Migration Result 필수 열

- change_id
- selected_version
- resolved_license
- direct_and_transitive_approved
- consumers_total
- consumers_migrated
- legacy_paths_remaining
- parity_status
- failure_recovery_status
- final_artifact_present
- sbom_ort_syft_grype_status
- exact_sha
- final_status

## 6. Evidence 금지

- 존재하지 않는 경로
- 로그 내용이 없는 빈 Placeholder
- 과거 SHA에서 생성된 Report
- 사람이 실행하지 않은 명령을 Exit 0으로 기록
- Secret·Token·Password·Private Key·실제 개인정보
- Local machine 절대 경로만 있어 다른 환경에서 재현 불가한 Evidence
