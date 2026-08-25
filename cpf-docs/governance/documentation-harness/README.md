# CPF Documentation Harness

이 디렉터리는 CPF 공식 산출물 생성의 단일 작성 하네스다.

## 매 세션 필수 읽기 순서

1. `HARNESS_LOCK.json`
2. `CPF_DOCUMENTATION_HARNESS.md`
3. `harness.json`
4. `scope.json`
5. `design-tokens.json` + `writing-style.json` + `terminology.json`
6. `content-models.json` + `table-presets.json` + `figure-presets.json`
7. `product-coverage.json`
8. 작업 대상 `profiles/*.json`
9. 최신 Source Identity

Harness를 수정할 수 있는 유일한 권한은 **사용자의 명시적 요청**이다. Source 변경, QA Finding, 작성자 판단은 자동 수정 권한이 아니다.

기존 분산 산출물 작성 지침 삭제 대상은 `DELETE_MANIFEST.txt`에 Root-relative exact path로 기록한다.
