# CPF 공식 문서 작업 인수인계

기준 Source SHA: `92169d9918dd176e8322ac2f9dfc29ebe1d2ea12`

## 현재 공식 사용자 문서

공식 문서는 README와 02~07의 7종 체계를 사용한다.

- README
- 02 프레임워크 개발자 가이드
- 03 배치 개발자 가이드
- 04 운영자 매뉴얼
- 05 배치 운영 가이드
- 06 Gateway 개발·사용 가이드
- 07 Specification / 기술 명세

## 완료 Gate

- README 내용 Gate 12/12 PASS
- 02~07 지침 심층 Gate 121/121 PASS
- 지침 원문 SHA 8/8 무변경
- DOCX 접근성 6/6 finding 0
- 최종 DOCX/PDF 총 136페이지
- 136/136 페이지 시각 QA PASS
- PDF 독립 재렌더 6/6 PASS
- README 로컬 링크/이미지 14/14 PASS
- README와 07 Architecture 동일 자산/해시 PASS
- 사용자 문서 제작 일시 및 DOCX creator/created/modified metadata 제거
- 사용자 문서의 내부 DEV-DOC ID 제거

## 개발 쪽 별도 검토

문서 작업 중 확인된 Source/사용성 문제는 사용자 문서와 분리했다.

- `CPF_DOCUMENTATION_TO_DEVELOPMENT_REVIEW.md`
- `CPF_DEVELOPER_USABILITY_REVIEW.md`

특히 Generated Domain Generator와 Batch Capability 경계, Generator preset UX, 동일 단순명 `CpfBatchJob` Annotation 2종은 개발 판정이 필요하다. 문서팀은 해당 개발/QA 상태를 임의로 완료 처리하지 않는다.

## Git 안전

이 문서 작업에서는 Commit, Push, Branch, Tag, Reset, Restore, Stash, Clean, Repository 파일 삭제를 수행하지 않았다. 구 공식 문서는 `DELETE_MANIFEST.csv` exact allowlist로만 사용자 실행 대상이다.

## 다음 검수자

다음 검수자는 과거 FINAL/Evidence를 승계하지 말고 이 패키지의 `PACKAGE_MANIFEST.json`, `SHA256SUMS.txt`, 기준 Source SHA, 실제 DOCX/PDF를 다시 기준으로 삼는다.
