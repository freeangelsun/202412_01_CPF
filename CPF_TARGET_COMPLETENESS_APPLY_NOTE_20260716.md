# CPF 기존 capability 정본 편입 적용 안내 — 2026-07-16

## 기준

- repository: `freeangelsun/202412_01_CPF`
- branch: `master`
- 확인 기준 SHA: `dd77c10d6a0a63b2cd31c309c83614a60f39d683`
- 목적: 지금까지 구현·부분 구현·검증 대상으로 관리해온 capability가 전체 목표에서 빠지거나 설명이 약한 문제 보완
- 이번 갱신은 요구사항·검수·상태 정합성 변경이며 신규 source 구현 완료 증적이 아니다.

## 덮어쓸 파일

1. `CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `CPF_NEW_REQUEST.md`
3. `CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md`
4. `CPF_STABILIZATION_REPORT.md`
5. `CPF_GAP_MATRIX.md`
6. `CPF_EVIDENCE_INDEX.md`

## 주요 추가

- source→정본, 정본→source 양방향 trace
- 권위 있는 module topology
- BZA 정식 명칭
- ACC generated reference domain
- EXS 기능 inventory 없는 삭제 금지
- 신규 10자리 O/S/B 실행 ID와 16자리 legacy migration
- execution catalog/startup registration
- standard header/context와 PFW error envelope
- BZA password/auth/refresh token/bootstrap/session/조직·권한
- BZA 결재와 CPF 공통 승인결재 연계
- PFW AI/RAG capability
- ADM 원격 로그, async ZIP, one-time download token
- attachment/file storage
- batch dependency·ghost·center-cut
- create-domain 전체 산출
- CMN fixed-length
- broker/file transfer
- ADM/BZA 제품 UI
- evidence/request protection/qualityGate

## 미리 수정하지 않은 파일

실제 구현과 재검증 결과가 나온 뒤 갱신한다.

- `README.md`
- `specs/기능_구현_매트릭스.md`
- `specs/기능_구현_매트릭스.json`
- `specs/sample-coverage-matrix.md`
- source/test/SQL/Flyway/OpenAPI/UI
- `specs/evidence/**`
- DOCX/PDF

## push 전 확인

```bash
git status --short
git diff --stat
git diff --check
git grep "CPF_FINAL_TARGET_REQUIREMENTS_0"
git grep "BIZADM\|BizAdm\|bizadm" -- ':!specs/evidence/**'
```

삭제한 01~05 파일 참조는 없어야 한다. BIZADM 검색 결과는 migration/history 목적이 아닌 신규 source/config에는 남지 않아야 한다.

## 권장 commit message

```text
20260716_04 기존 CPF capability 정본 편입 및 회귀 방지 기준 보강
```
