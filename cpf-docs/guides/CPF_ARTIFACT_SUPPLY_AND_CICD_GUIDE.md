# CPF 산출물 공급과 CI/CD 가이드

## 1. 목적

CPF Library, BOM, Gradle Plugin, Runtime, Frontend와 Offline Bundle을 버전·Source Commit·Hash가 추적되는 제품 산출물로 공급한다.

## 2. 공급 원칙

- 수동 JAR 복사 금지
- Immutable Release
- Source Commit 추적
- SHA-256
- Signature
- BOM
- SBOM
- License
- Provenance
- Quality Gate
- 승인된 Repository

## 3. Artifact Mode

| Mode | 용도 | 공급원 |
|---|---|---|
| `LOCAL_DEV` | 개발자 Local | 검증 Local Maven Repository |
| `REMOTE` | CI/STG/PROD | Nexus/Artifactory |
| `OFFLINE` | 폐쇄망 | 검증 Offline Bundle |

CI/운영은 Local Repository로 Fallback하지 않는다.

## 4. Platform Artifact Set

- `cpf-core`
- `cpf-common`
- Batch Contract
- Batch Testkit
- Platform BOM
- Domain Convention Plugin
- 공개 Test Fixture
- Metadata

하나의 Promotion 단위로 Version을 맞춘다.

## 5. Local 개발

같은 Repository는 Project Dependency를 사용한다.

독립 Generated Domain은 검증 Local Repository를 사용한다.

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts `
  -PcpfArtifactMode=LOCAL_DEV
```

## 6. Promotion

```text
Quality Build
→ 격리 Staging Publish
→ POM/Metadata/BOM/Hash 검증
→ Lock
→ Promotion
→ Manifest 공개
```

Manifest는 마지막에 공개한다.

## 7. Remote

```powershell
$env:CPF_ARTIFACT_MODE='REMOTE'
$env:CPF_ARTIFACT_REPOSITORY_URL='https://nexus.example/repository/cpf-releases/'
```

```powershell
.\gradlew.bat publishCpfPlatformArtifacts `
  -PcpfArtifactMode=REMOTE
```

Credential은 Secret으로 주입한다.

## 8. Offline

```powershell
.\gradlew.bat buildCpfOfflineArtifactBundle `
  -PcpfArtifactMode=LOCAL_DEV
```

구조:

```text
cpf-offline-artifacts-<version>.zip
├─ repository/
├─ metadata/
├─ manifests/
├─ SBOM/
├─ README.txt
└─ SHA256SUMS.txt
```

Consumer는 압축 내부 Maven Repository를 사용한다.

## 9. Version

- Semantic Version
- Compatibility
- Snapshot 제한
- Release Immutable
- Source Commit
- Build Number
- Database Version
- API/Message Version

## 10. BOM

업무 Domain은 BOM으로 CPF Version Set을 맞춘다.

```groovy
dependencies {
    implementation platform("com.cpf:cpf-platform-bom:${cpfVersion}")
    implementation "com.cpf.core:cpf-core"
    implementation "com.cpf.common:cpf-common"
}
```

## 11. Gradle Plugin

Convention Plugin은 다음을 통일한다.

- Java
- Test
- Dependency
- Packaging
- Checkstyle/Static
- OpenAPI
- Artifact Manifest
- Generated Domain 규칙

## 12. bootJar/bootWar

Packaging 후 내부 CPF JAR의 Version과 Hash를 Manifest와 비교한다.

## 13. Frontend

- `npm ci`
- Lock File
- Typecheck
- Lint
- Unit
- Production Build
- SBOM
- License
- 외부 Runtime CDN 0
- Static Hash

## 14. Pipeline

```text
Checkout
→ Clean Source
→ Compile/Test
→ Frontend
→ Static Gate
→ DB/Generator
→ Package
→ SBOM/License/CVE
→ Signature
→ Staging
→ Runtime Smoke
→ Approval
→ Publish
```

## 15. Required Check

- Build
- Unit/Integration
- Architecture
- Security
- DB Parity
- Generator
- Frontend
- License/CVE
- Artifact Hash
- Evidence

## 16. Signature와 Provenance

- Artifact Signature
- Signer
- Algorithm
- Timestamp
- Source Commit
- Build Tool
- Runner Identity
- Dependency
- Attestation

## 17. SBOM

Backend와 Frontend Dependency를 포함한다.

- Coordinate
- Version
- License
- Hash
- Source
- CVE
- Transitive

## 18. Promotion

환경 Promotion Manifest:

- sourceEnvironment
- targetEnvironment
- releaseId
- artifact
- config
- DB
- reason
- approval
- hash

## 19. 실패

| 실패 | 동작 |
|---|---|
| Build/Test | 공개 금지 |
| Hash 불일치 | Promotion 금지 |
| Signature 실패 | 사용 금지 |
| CVE 정책 위반 | Release 금지 |
| Remote URL 없음 | 실패 |
| Offline Manifest 없음 | Build 실패 |
| Source SHA 불일치 | 재검증 |

## 20. Rollback

- 이전 Immutable Artifact
- Config Version
- DB 호환
- Frontend
- Gateway
- Batch
- Manifest
- Health Gate

## 21. Repository 권한

- Publisher
- Reader
- Release Manager
- Retention
- Immutability
- Audit
- Token Rotation

## 22. 폐쇄망

- Bundle Signature
- 전달 매체
- Checksum
- Import Log
- Malware Scan
- 내부 Repository
- Update 절차
- License/CVE Feed

## 23. Evidence

- Pipeline ID
- Commit
- Artifact Version
- Hash
- Signature
- SBOM
- Test
- Approver
- Publish Target
- 시각
- 결과

## 24. 체크리스트

- [ ] 수동 JAR 복사가 없다.
- [ ] Artifact Set Version이 일치한다.
- [ ] Manifest와 Hash가 있다.
- [ ] Release가 Immutable이다.
- [ ] Frontend도 SBOM/License 대상이다.
- [ ] Local/Remote/Offline 공급이 분리된다.
- [ ] CI 실패 시 Publish하지 않는다.
- [ ] Rollback Artifact를 보존한다.
