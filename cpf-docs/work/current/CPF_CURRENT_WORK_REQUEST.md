# CPF Current Work Request — R14 통합 검증/잔존 결함 Closure

## 기준
R14 Overlay 적용 후 사용자가 Push한 **최신 master SHA를 다시 조회해 그 SHA를 새 기준으로 사용한다.**
R14 작성 기준 SHA는 `56b165513f73f0548d41d2d52197abcdf69a0d14`다.

최상위 기준: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
QA 입력: `cpf-docs/review/CPF_MASTER_FULL_DEFECT_AUDIT_20260726.md`
Handover: `cpf-docs/work/state/CPF_R14_HANDOVER.md`

## 현재 해야 할 일
1. R14 Overlay가 최신 master에 정확히 반영됐는지 file/hash/diff로 확인.
2. QA 289개 ID와 12개 제품 Gap을 최신 Source 기준으로 재판정.
3. P0/P1 중 실제 잔존 결함은 검증 과정에서 즉시 수정.
4. 전체 Build/DB/Frontend/Browser/Multi-instance/Generator/Service Call/Batch/Async 검증 수행.
5. 실제 실행 Evidence를 현재 Commit 기준으로 보존.
6. 미검증을 완료로 바꾸지 말고, Runtime Evidence가 확보된 것만 완료 처리.

## 완료 금지
- 일부 Unit Test 또는 정적 검색만 통과.
- Frontend build 없이 화면 결함 완료 처리.
- MariaDB 실제 fresh/upgrade/rollback 없이 DB 완료 처리.
- single-instance만으로 multi-instance 완료 처리.
- Sample을 제품 구현으로 간주.
- unsupported Vendor를 SQL 복사로 지원 처리.
- Release Artifact가 존재한다는 이유만으로 License/CVE/Signature/Provenance 완료 처리.
