# CPF 도구 운영 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 도구 사용자, 설치·운영 담당자, 플랫폼 유지보수자
> **목적**: CPF 도구를 목적별로 선택하고 계획·적용·검증·복구 순서로 안전하게 실행한다.
> **관련 문서**: [도구 상세 참조](CPF_TOOL_REFERENCE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-tools` |
| 이 문서로 완료하는 일 | Generator·DB·Runtime·Quality·Supply 도구를 Dry-run·Fail-fast·Idempotency·Exit Code·Evidence 원칙으로 안전하게 사용한다. |
| 적용 범위 | PowerShell/Python/Gradle 도구, Runtime 조립, 검증 Gate, Package 생성 |
| 주요 독자 | 개발자, DBA, Release 담당자, QA, 운영자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

`cpf-tools`는 임시 Script 보관소가 아니라 CPF 제품을 생성·설치·검증·공급·이관·복구하기 위한 공식 Tooling 영역이다.

## 2. Directory 책임

| 경로 | 책임 |
|---|---|
| `cpf-tools/config` | 프로필, Coverage, 소스 계획 |
| `cpf-tools/db/metadata` | Canonical 스키마와 정책 |
| `cpf-tools/db/vendor` | 공급자 소스와 Lifecycle |
| `cpf-tools/db/generated` | 생성 명세서 |
| `cpf-tools/generator` | 업무영역 생성기 |
| `cpf-tools/scripts` | 설치·검증·동기화 도구 |
| `cpf-tools/build/platform-bom` | BOM |
| `cpf-tools/build/gradle-plugin` | Convention Plugin |
| `cpf-tools/runtime` | 로컬 실행 환경 Assembly |

## 3. 도구 분류

| 분류 | 의미 |
|---|---|
| `DEV_ONLY` | 개발자 로컬 편의 |
| `QUICK` | 저비용 정적 Gate |
| `VERIFY` | 변경 단위 검증 |
| `FULL` | 릴리스 후보 통합 검증 |
| `CI_RELEASE` | CI와 릴리스 |
| `PRODUCT_ADMIN_TOOL` | 고객 설치·운영 도구 |

개발 Gate를 운영 실행 환경에 포함하지 않는다.

## 4. 공통 실행 원칙

- 저장소 Root에서 실행
- `git rev-parse HEAD` 기록
- Clean/Dirty 상태 기록
- 인증정보를 Argument에 넣지 않음
- 사전 계획 우선
- 적용 명시 확인
- Exit Code 확인
- 실패 숨김 금지
- 검증 증적 저장
- UTF-8 without BOM

## 5. Help

```powershell
Get-Help .\cpf-tools\scripts\<script>.ps1 -Detailed
```

문서와 Script 매개변수가 다르면 같은 변경에서 수정한다.

## 6. 생성기

```powershell
pwsh -File .\cpf-tools\generator\create-domain.ps1 `
  -DomainName payment `
  -SystemCode PAY `
  -DryRun
```

상세는 생성기 Guide를 참고한다.

## 7. DB 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-database-artifacts.ps1
```

단계:

- 이관 Checksums
- Install Bundle
- 스키마 명세서
- 정본 불일치
- 프로필
- 생성 업무영역
- 공급자 Parity

## 8. DB 설치

```powershell
pwsh -File .\cpf-tools\scripts\initialize-cpf-database.ps1 -All -RequireRun
```

생성 업무영역:

```powershell
pwsh -File .\cpf-tools\scripts\initialize-generated-domain-databases.ps1 `
  -All `
  -Operation bootstrap `
  -Apply
```

## 9. 이관

```powershell
pwsh -File .\cpf-tools\scripts\invoke-platform-database-migration.ps1 `
  -Direction upgrade `
  -FromVersion 72 `
  -ToVersion 73 `
  -Modules batch
```

사전 계획 Plan 해시 검토 후 적용한다.

## 10. 백업/복원

```powershell
pwsh -File .\cpf-tools\scripts\backup-cpf-database.ps1 ...
pwsh -File .\cpf-tools\scripts\restore-cpf-database.ps1 ... -ConfirmRestore
pwsh -File .\cpf-tools\scripts\verify-dr-restore.ps1 ...
```

## 11. 실행 환경

```powershell
pwsh -File .\cpf-tools\scripts\start-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\status-cpf-local.ps1
pwsh -File .\cpf-tools\scripts\stop-cpf-local.ps1
```

배치:

```powershell
pwsh -File .\cpf-tools\scripts\start-bat-local-distributed.ps1
pwsh -File .\cpf-tools\scripts\stop-bat-local-distributed.ps1
```

## 12. Aggregate Gate

### QUICK

개발 중 반복 실행:

- Syntax
- Architecture
- 비밀값
- Document Link
- 저장소 Hygiene
- 프런트엔드 경로
- SQL Parse

### VERIFY

작업 단위:

- 영향 모듈 테스트
- Packaging
- DB Sync
- 생성기
- Focused 실행 환경
- 검증 증적

### FULL

릴리스 후보:

- Clean Build
- 프런트엔드
- 3 공급자
- 생성기 Lifecycle
- Multi-instance
- 장애 주입
- 브라우저
- 산출물
- 검증 증적

## 13. 대표 Gate

```powershell
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-source-documentation-standard.ps1
pwsh -File .\cpf-tools\scripts\check-admin-data-safety.ps1
```

## 14. 통합 검증

```powershell
pwsh -File .\cpf-tools\scripts\verify-full-product.ps1 `
  -WithDatabase `
  -WithGeneratorLifecycle `
  -WithBrowser `
  -RequireAll `
  -Profile local
```

`RequireAll`에서는 Skip도 전체 성공으로 인정하지 않는다.

## 15. 산출물 공급

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts -PcpfArtifactMode=LOCAL_DEV
.\gradlew.bat publishCpfPlatformArtifacts -PcpfArtifactMode=REMOTE
.\gradlew.bat buildCpfOfflineArtifactBundle -PcpfArtifactMode=LOCAL_DEV
```

## 16. 변경집합

환경 Promotion:

```powershell
pwsh -File .\cpf-tools\scripts\new-cpf-changeset.ps1 ...
pwsh -File .\cpf-tools\scripts\verify-cpf-changeset.ps1 ...
```

명세서는 Commit과 파일 해시를 기록한다.

## 17. 인증서

```powershell
pwsh -File .\cpf-tools\scripts\check-certificate-expiry.ps1 `
  -인증서Path .\certificate.pem `
  -WarnDays 30
```

개인 키를 읽지 않는다.

## 18. 생성 업무영역 동기화

```powershell
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope Database
pwsh -File .\cpf-tools\scripts\sync-generated-domain-artifacts.ps1 -Scope AllGeneratorOwned -Apply
```

사용자 수정 파일을 기본 덮어쓰지 않는다.

## 19. 저장소 Hygiene

검출 대상:

- build
- logs
- tmp
- zip
- bak
- patch
- 비밀값
- Stale Root 문서
- Dead 소스
- 외부 CDN
- 잘못된 모듈

## 20. 도구 출력 표준

JSON 결과:

- tool
- version
- sourceCommit
- startedAt
- finishedAt
- command
- environment
- status
- exitCode
- findings
- evidence
- sanitized

## 21. 실패 분류

- SOURCE_DEFECT
- ENVIRONMENT
- CREDENTIAL
- UNSUPPORTED
- DRIFT
- EXTERNAL_DEPENDENCY
- SECURITY_POLICY
- USER_CANCELLED

실패를 0건 성공으로 바꾸지 않는다.

## 22. 안전한 적용

파괴적 도구는 다음을 요구한다.

- 사전 계획
- Expected Plan 해시
- Confirmation
- 대상 허용 목록
- 백업
- 되돌리기
- 권한
- 사유
- 감사

## 23. 여러 작업 환경

작업 시작:

```powershell
pwsh -File .\cpf-tools\scripts\check-work-context.ps1
```

작업 종료:

- 소스/API/SQL/테스트
- Guide
- Handover
- Verification Plan
- 검증 증적
- Clean Working Tree

## 24. 도구 문서화 기준

공식 Entry마다 다음을 제공한다.

- 목적
- 매개변수
- 기본값
- 조합 제약
- 환경변수
- 입력
- 출력
- 변경 영향
- 정상 예
- 실패 예
- 재실행
- 복구
- 검증 증적

## 25. 체크리스트

- [ ] 도구 분류가 있다.
- [ ] 사전 계획과 적용이 분리된다.
- [ ] 인증정보를 출력하지 않는다.
- [ ] 실패 Exit Code가 정확하다.
- [ ] 결과 JSON과 검증 증적이 있다.
- [ ] 문서와 매개변수가 일치한다.
- [ ] 운영 실행 환경에 개발 Gate가 포함되지 않는다.

## 부록 A. 안전 실행 형식

모든 변경 도구는 가능한 한 다음 흐름을 제공한다.

```text
도움말 → 입력 검증 → 계획·미리보기 → 승인 가능한 계획 해시
→ 적용 → 단계별 로그 → 검증 → 재개·복구 또는 되돌리기
```

## 부록 B. 운영자 도구와 유지보수 도구

- 운영자 도구: 설치, 상태, 백업, 복구, 이관, 업무영역 생성
- 유지보수 도구: 정본 동기화, 구조 검사, 문서 검사, 저장소 위생, 산출물 생성

고객 운영 경로가 작업 세션 문서나 개발자 개인 환경에 의존하지 않도록 한다.

## 부록 C. 종료 코드 기준

| 코드 | 의미 |
|---:|---|
| 0 | 성공 |
| 1 | 일반 실행 실패 |
| 2 | 입력·사용법 오류 |
| 3 | 사전 조건 실패 |
| 4 | 정본 불일치·충돌 |
| 5 | 부분 적용·복구 필요 |
| 6 | 권한·보안 정책 거부 |

개별 도구가 추가 코드를 사용하면 상세 참조에 명시한다.

## 32. Tool 설계 공통 계약

모든 쓰기 Tool은 가능한 범위에서 다음을 제공한다.

- `-DryRun`, `-WhatIf` 또는 Preview
- 입력·환경·권한·충돌의 사전 검증
- 실제 변경 목록과 대상 Root 표시
- 멱등 실행 또는 명확한 중복/Drift 오류
- 부분 실패 시 재개·Rollback·정리 방법
- 0/비0 Exit Code와 Machine-readable 결과
- 기준 Commit, 명령, 시작·종료 시각을 포함한 Evidence
- Secret·Credential·민감 경로의 정제

## 33. Tool 실행 전 질문

1. 이 Tool이 수정·삭제·생성하는 경로는 어디인가?
2. DB·Network·Repository·Process와 같은 외부 Side Effect가 있는가?
3. Dry-run과 실제 실행 결과의 차이는 무엇인가?
4. 중간 실패 시 이미 반영된 변경을 어떻게 찾는가?
5. 다른 인스턴스가 동시에 실행해도 안전한가?
6. Source Commit과 Artifact/DB Version을 어떻게 기록하는가?
7. 성공 판정은 파일 존재인가, 실제 Runtime/DB 결과인가?

## 34. 부분 실패 처리

Tool이 여러 항목을 처리할 때 전체를 성공으로 출력한 뒤 실패 항목을 숨기지 않는다. Item별 상태, 재시도 가능 여부, 마지막 성공 Checkpoint와 Cleanup 필요 여부를 Result Manifest에 기록한다.

## 35. 자동화 사용

CI/CD에서는 Interactive Prompt를 사용하지 않고 모든 필수 입력을 Parameter 또는 승인된 환경 변수로 제공한다. Human-readable Log와 별도로 JSON/CSV Result를 생성하고 Exit Code와 Result 상태가 일치하도록 한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Generator | `cpf-tools/generator` | 업무영역 계획·생성 |
| DB | `cpf-tools/db`, DB Script | Canonical·Vendor Lifecycle |
| Runtime | `cpf-tools/runtime`, start/stop/status Script | 로컬·통합 Runtime |
| Quality | `cpf-tools/scripts/check-*`, `verify-*` | 저비용 Gate와 통합 검증 |
| Supply | Build·Package·Offline Script | Artifact Manifest·Hash·Bundle |

### Z.1 공통 확인 명령

```powershell
git status --short
git diff --check
git grep -n "TODO\|UnsupportedOperationException\|return null" -- ':!cpf-docs/archive/**'
pwsh -File .\cpf-tools\scripts\check-architecture-ownership.ps1
pwsh -File .\cpf-tools\scripts\check-document-links.ps1
pwsh -File .\cpf-tools\scripts\check-repository-hygiene.ps1
```

명령이 현재 Repository에 존재하지 않거나 Parameter가 달라졌다면 해당 Tool Source와 [도구 상세 참조](CPF_TOOL_REFERENCE.md)를 먼저 갱신한다.

### Z.2 완료 상태 사용

- **완료**: 구현·Consumer·운영 경로·검증·Evidence가 현재 Commit에서 확인됨
- **부분 구현**: 일부 계층 또는 실패·복구·운영 경로가 빠짐
- **미구현**: 제품 동작이 없음
- **미검증**: 구현은 있으나 요구된 실행 검증을 수행하지 않음
- **실패**: 검증을 수행했으나 기대 결과를 충족하지 못함
- **재확인 필요**: Source·문서·Evidence 또는 환경이 서로 달라 현재 상태를 확정할 수 없음
