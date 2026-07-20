# CPF 전체 요청 정합성 갱신 적용 안내 — 2026-07-16

## 기준

- repository: `freeangelsun/202412_01_CPF`
- branch: `master`
- 기준 commit: `ff4661e673dab9a2f417e75f8ad64fb712c96fa6`

## 지금 교체할 파일

1. `CPF_NEW_REQUEST.md`
2. `CPF_FINAL_TARGET_REQUIREMENTS.md`
3. `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
4. `CPF_STABILIZATION_REPORT.md`
5. `CPF_GAP_MATRIX.md`
6. `CPF_EVIDENCE_INDEX.md`

## 지금 수정하지 않을 파일

실제 Codex 구현·정리·검증 후 결과에 따라 갱신한다.

- `README.md`
- `specs/기능_구현_매트릭스.md`
- `specs/기능_구현_매트릭스.json`
- `specs/sample-coverage-matrix.md`
- source/test/resources
- SQL/Flyway/all_install
- OpenAPI
- `specs/evidence/**`
- DOCX/PDF

README는 작업 지시를 미리 적는 문서가 아니므로 Codex가 실제 최종 구조·기능과 맞춰 제품 문서로 갱신해야 한다.

## 상태 기준

이번 변경은 요구사항과 검수 기준 갱신이다. 신규 기능 완료가 아니다.

다음은 `재확인 필요`로 관리한다.

- package structure
- repository garbage cleanup
- README product document
- ACC generator output cleanup
- XYZ EDU package standard
- BAT job package standard

## 권장 commit message

```text
20260716_06 CPF 패키지·README·가비지 정리 기준 및 전체 요청 갱신
```
