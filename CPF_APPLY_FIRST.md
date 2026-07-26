# CPF Final Completion Package — Apply First

기준 Repository: `https://github.com/freeangelsun/202412_01_CPF`  
기준 Branch: `master`  
기준 SHA: `9253097086322c0eacc00c005e944b132e31ae06` (`20260726_04`)

## 적용 순서

1. 압축은 CPF Repository **밖**에 푼다.
2. 현재 Repository가 위 기준 SHA 또는 그 이후 사용자가 의도한 최신 master인지 확인한다.
3. 적용:

```powershell
pwsh <package>\cpf-tools\scripts\apply-cpf-final-completion-package.ps1 `
  -PackageRoot <package> `
  -RepositoryRoot <CPF repository>
```

4. 적용 후 `cpf-batch/src`는 제거되고 아래 구조가 정본이다.
   - `cpf-batch/contract`
   - `cpf-batch/runtime-common`
   - `cpf-batch/control-server`
   - `cpf-batch/scheduler`
   - `cpf-batch/worker`
   - `cpf-batch/center-cut-runner`
   - `cpf-batch/host-agent`
   - `cpf-batch/testkit`
5. 검증:

```powershell
pwsh .\cpf-tools\scripts\verify-cpf-final-completion.ps1
```

## 중요한 정책

- 신규 고정 업무 Domain을 만들지 않는다. Domain은 Generator/독립 Repository 방식이다.
- EXS 고정 Module을 복원하지 않는다.
- `cpf-tools/db/source` standalone 정본을 복원하지 않는다.
- Generated/Business Domain이 `com.cpf.core.common.*`를 직접 import하지 않는다.
- 위험 Runtime/Deployment 조치는 Approval/사유/멱등/감사/UNKNOWN_RESULT 정책을 따른다.
- Host Agent는 자유 Shell 또는 임의 Path 실행 인터페이스를 제공하지 않는다.
- 사용자 승인 없이 Commit/Push/Branch를 생성하지 않는다.

## 검증 상태

이 패키지 작성 환경에서 실제 실행한 정적 Gate 결과는
`cpf-docs/evidence/CPF_FINAL_STATIC_VALIDATION_20260726.txt`에 있다.

Java 25 전체 Gradle, MariaDB lifecycle, Browser E2E, 실제 원격 Host Agent,
Commercial SBOM/License/CVE/Signature 검증은 대상 환경에서 실행한 Evidence가 생기기 전까지 `미검증`이다.
