# CPF R4 인수인계

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- 기준 SHA: `cb305fc5363263c9607e990ba640233c28668f01`
- 범위: 논리 실행순서 `20,001~30,558`
- Requirement: `10,558건`
- Work Package: `291개`
- Scenario: `14,014건`

## 적용 후 순서

1. R4 Overlay를 Repository Root에 적용한다.
2. `DELETE_MANIFEST.csv`의 정확한 세 경로만 정리한다.
3. Commit 전 `git diff --check`와 변경 목록을 확인한다.
4. 사용자가 Commit·Push한 후 새 exact HEAD에서 다음을 실행한다.

```powershell
pwsh -ExecutionPolicy Bypass -File .\cpf-tools\scripts\run-cpf-r4-exact-head-validation.ps1 -Root . -ExpectedHead <NEW_40_CHAR_HEAD>
```

Wrapper는 clean HEAD, Java25 Gradle, 3 Vendor DB, Browser E2E, Audit Spring 2-instance를 모두 fail-closed 실행한다. 외부 환경이 준비되지 않은 항목은 성공으로 기록하지 않는다.

개발 GPT는 QA/Codex 상태를 변경하지 않고 개발 GPT 컬럼과 Evidence만 갱신한다. 현재 QA Finding 개발 상태는 완료 16·미완료 9이며 최종 완료는 QA 통과 전까지 인정되지 않는다.
