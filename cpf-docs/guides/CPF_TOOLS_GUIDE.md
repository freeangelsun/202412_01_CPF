# CPF 도구 운영 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 도구 사용자, 설치·운영 담당자, 플랫폼 유지보수자
> **목적**: CPF 도구를 목적별로 선택하고 계획·적용·검증·복구 순서로 안전하게 실행한다.
> **관련 문서**: [도구 상세 참조](CPF_TOOL_REFERENCE.md) · [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md)

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
