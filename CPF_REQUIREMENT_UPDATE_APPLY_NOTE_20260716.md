# CPF 요구사항 확장 파일 적용 안내 — 2026-07-16

## 기준

- repository: `freeangelsun/202412_01_CPF`
- branch: `master`
- 확인 기준 SHA: `344119731239387b0ff8d32ac96ec4608b40404b`
- 이 묶음은 요구사항·검수 기준·상태 정합성 갱신용이다.
- source 구현, SQL 실행, browser, MariaDB, broker, multi-instance 검증을 수행한 결과물이 아니다.

## 저장소에서 교체할 파일

묶음 안의 다음 파일을 repository 동일 경로에 덮어쓴다.

1. `CPF_NEW_REQUEST.md`
2. `CPF_FINAL_TARGET_REQUIREMENTS.md`
3. `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
4. `CPF_STABILIZATION_REPORT.md`
5. `CPF_GAP_MATRIX.md`
6. `CPF_EVIDENCE_INDEX.md`

## 이번에 일부러 수정하지 않은 파일

다음은 Codex 구현과 실제 검증 후 결과에 맞춰 갱신해야 하므로 미리 수정하지 않았다.

- `README.md`
- `specs/기능_구현_매트릭스.md`
- `specs/기능_구현_매트릭스.json`
- `specs/sample-coverage-matrix.md`
- source/test
- split SQL/Flyway
- `00_all_install.sql`
- `00_all_install_and_smoke.sql`
- `99_smoke_check.sql`
- OpenAPI
- ADM/BAM/BZA UI
- `specs/evidence/**`
- DOCX 9종
- PDF

요구사항만 추가한 상태에서 위 파일을 완료처럼 갱신하면 안 된다.

## 상태 변경 이유

다음 기존 evidence는 요구사항 확장 이전 기준이므로 `재확인 필요`로 변경했다.

- `standard-execution-id`
- `sample-coverage`
- `readme-docs`
- `quality-gate`
- `request-protection`
- `report-matrix-consistency`

특히 기존 16자리 실행 ID 완료 evidence는 신규 10자리 O/S/B 목표의 완료 근거가 아니다.

## push 전 확인

```bash
git status --short
git diff --stat
git diff -- CPF_NEW_REQUEST.md
git diff -- CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
git diff -- CPF_GAP_MATRIX.md
git diff -- CPF_EVIDENCE_INDEX.md
```

정본 파일은 크므로 다음도 확인한다.

```bash
git diff --numstat -- CPF_FINAL_TARGET_REQUIREMENTS.md
git diff --check
```

## 권장 commit message

```text
20260716_02 CPF 전체 요구사항·검수 기준 확장
```

## Codex 작업 후 반드시 다시 갱신할 항목

- 최신 시작/종료 SHA
- source 구현 상태
- SQL/Flyway/all_install
- OpenAPI
- ADM/BAM/BZA browser evidence
- MariaDB/broker/file server/multi-instance evidence
- 기능 matrix
- sample coverage
- README와 상세 가이드
- report/gap/evidence index
- qualityGate와 consistency evidence

실행하지 않은 검증은 완료로 올리지 않는다.
