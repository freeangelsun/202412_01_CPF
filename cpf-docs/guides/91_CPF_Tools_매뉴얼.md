# CPF Tools 매뉴얼


## 문서 기준과 판정

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Source 기준 Commit: `e134c1f275c306c0e9ab4a044d9140ac4b3ca620`
- 최상위 목표 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 문서 표준 정본: `cpf-docs/specification/CPF_DOCUMENTATION_STANDARD.md`
- 사용자 지시 적용: 요구사항에 정의되고 Source에 연결된 기능은 사용 가능한 제품 기능으로 설명한다.
- 이 문서 작업에서 직접 수행한 Runtime·DB·Browser·다중 인스턴스 검증: `미검증`
- 문서와 Source의 경로·식별자 정합성 검토: `완료`

> Runtime 미검증은 기능 절차를 생략하는 이유가 아니다. 다만 실행 결과를 직접 확인하지 않은 항목은 배포 승인 시 해당 환경의 Evidence로 다시 확인한다.


## 1. Tool 지도

| Tool | 경로 | 목적 |
|---|---|---|
| Domain Generator | `cpf-tools/generator/create-domain.ps1` | 신규 Domain 생성 |
| Domain Upgrade | `cpf-tools/generator/upgrade-domain.ps1` | 기존 Domain Profile/Lock 갱신 |
| Gradle Plugin | `cpf-tools/build/gradle-plugin` | 표준 Build·Policy Gate |
| Platform BOM | `cpf-tools/build/platform-bom` | Version 정렬 |
| DB Vendor Pack | `cpf-tools/db/vendor/**` | 설치·Migration·Rollback |
| QA39 Tool | `cpf-tools/scripts/Qa39Tool.java` | Canonical/Naming/Evidence/DB/OpenAPI 검증 |
| Runtime Validation | `cpf-tools/verification/qa39/**` | Runtime/Fault/환경 검증 |
| Docker Tool | `cpf-tools/environment/docker-development-test/**` | 외부 Provider Fixture |
| Local Runtime | `cpf-tools/runtime/**` | Local 실행·교육 |

## 2. Generator

### 실행 전

Java 25, PowerShell, Repository Root, 깨끗한 대상 경로를 확인한다.

### 절차

1. Dry Run으로 입력과 생성 목록을 확인한다.
2. Profile, Capability Binding, DB Vendor, Security를 입력한다.
3. 기존 파일 충돌을 확인한다.
4. Apply 후 Manifest/Lock/Exception Policy를 검토한다.
5. Fresh Build와 Runtime Policy Verify를 실행한다.

### 생성 파일

Gradle Module, Source Layer, Config, DB Template, `domain-manifest.json`, `resolved-starter-lock.json`, 예외 정책과 Test Skeleton.

### 실패

잘못된 Profile, Provider 충돌, 기존 파일, Catalog 불일치, Policy Hash 오류는 Exit Code 비정상으로 종료한다. 일부 파일만 생성됐으면 Manifest를 기준으로 정확한 경로를 제거하고 재실행한다.

## 3. Upgrade Tool

기존 Domain의 Profile/Capability Version을 새 Catalog에 맞춘다. Upgrade 전 Git Diff와 Backup을 만들고, Dry Run에서 Dependency/Config/SQL 이동을 확인한다. 업무 Source 자동 덮어쓰기를 허용하지 않는다.

## 4. Build Tool

```powershell
./gradlew.bat clean test assemble --no-daemon --max-workers=1 --stacktrace
./gradlew.bat qualityGate --no-daemon --max-workers=1 --stacktrace
./gradlew.bat publishCpfStagingPlatformArtifacts --no-daemon
```

실패 시 첫 실패 Task와 Report를 기준으로 수정한다. Build Output을 Source ZIP에 포함하지 않는다.

## 5. QA39 검증 Tool

| Command | 검증 |
|---|---|
| `canonical-closure` | Profile/Capability/Consumer/Delete Closure |
| `provider-conformance` | 고객 Provider SPI Compile/Runtime |
| `verify` | 물리 경로·Catalog·BOM·Generator 일치 |
| `naming` | Group/Function/Provider/API/SPI/Internal Naming |
| `evidence` | Stale SHA, False PASS, Missing Hash 차단 |
| `db` | 3 Vendor SQL Parity |
| `openapi` | Backend OpenAPI·Client·Route Contract |

Gradle Task가 `Qa39Tool.java`를 호출하므로 직접 Script와 Gradle 결과가 같아야 한다.

## 6. DB Tool

Vendor Profile, Upgrade Profile, Backup Manifest와 Evidence Root를 입력한다. Dry Run/Verify를 먼저 수행하고 운영 환경의 파괴적 Rollback은 금지한다. 정상 결과는 Object Count, Schema Version, Checksum, Smoke Query와 Drift 0건이다.

## 7. OpenAPI Client Tool

Backend OpenAPI 생성 → `cpf-openapi.json` 갱신 → Generated Client 생성 → Route Operation Contract 검증 → Frontend Typecheck/Test/Build 순서로 실행한다. Generated Source를 수동 수정하지 않는다.

## 8. Docker Tool

QA39 Runtime Fixture는 필요할 때 수동 기동한다. Secret은 `C:\dev\Docker\Secrets\` 등 Repository 밖에서 공급한다. `restart: "no"`를 유지하고 Validation Script의 `finally`에서 Container를 중지한다.

## 9. Supply Chain Tool

CycloneDX SBOM, Artifact Hash, License, Publication Metadata를 생성한다. 제거된 Artifact가 SBOM에 남으면 실패한다. Evidence에는 기준 Commit과 Artifact Hash를 기록한다.

## 10. 재실행 안전성

- Generator: 동일 입력 Dry Run은 변화 0건이어야 한다.
- DB Migration: 적용 이력과 Checksum이 일치해야 한다.
- Verification: Source를 수정하지 않아야 한다.
- Docker Validation: 종료 후 실행 중 CPF Container가 없어야 한다.
- Publication: 격리된 Staging Repository를 사용한다.

## 11. Cleanup

Build, Log, Temp, Evidence 중 Repository 비정본 경로만 정확한 경로로 제거한다. `git clean`, Wildcard 전체 삭제, Working Tree Reset을 사용하지 않는다.

## 12. EDU Tool 활용

`cpf-reference`, `cpf-member`, Local Runtime을 사용해 정상·오류·UNKNOWN·복구를 재현한다. 교육 결과를 그대로 제품 완료 Evidence로 사용하지 않고 실제 Consumer/환경 검증을 별도로 수행한다.
