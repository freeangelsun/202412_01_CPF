# TEST AND EVIDENCE

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 기준 SHA: `bd5fcd7707cfbba366071e7e4556c5cbb9b974bc`

## 수행

1. GitHub branch 조회로 `master` exact SHA 확인
2. README 링크/이미지 상대경로 검사: 13개 / 누락 0
3. 공식 파일 존재 검사: README + 6 DOCX + 6 PDF
4. Canonical `render_docx.py`로 02~07 DOCX 6종 전체 재렌더 및 PDF 재생성
5. 02~07 전체 **74페이지 전수 시각 QA**, 정식 CPF CI Header 적용 확인, 07 Specification 통합 Architecture 이미지 포함 확인
6. 공식 문서 총 페이지 정합성: **74페이지**
7. immutable 문서 작성 지침 SHA-256 검증: 8/8 PASS
8. 금지/낡은 용어 및 비공식 DB Vendor 사용자 문서 Scan
9. 원본 전체 Source ZIP의 실제 한글 경로 기준 Garbage/Delete 후보 재산정
10. Active Governance/검증 Script/Work 참조 15개를 별도 보존하고, 과거/중복 후보 186개 중 **114개만 자동 삭제(DELETE)**, `cpf-docs/deliverables/**` 보호 경로 72개는 **KEEP_PROTECTED**로 자동 삭제에서 제외
11. Delete Manifest와 최종 Overlay 경로 충돌: 0건

## 결과

- 문서 자체 검증: PASS
- README 및 Specification 정식 통합 Architecture 시각 보강: PASS
- 02~07 정식 CPF CI Header: PASS
- 삭제 안전성 정적 검증: PASS
- Git write: 수행하지 않음

## 수행하지 않은 검증

- CPF 전체 제품 Build/Test/Runtime
- Oracle/PostgreSQL/MariaDB Live DB lifecycle
- ADM/BZA/Gateway Browser Runtime
- Batch Worker process-kill/fault runtime
- Codex 독립검수
- QA 최종검수

이 항목들은 이번 문서 자체검수 성공으로 대체하지 않는다.
