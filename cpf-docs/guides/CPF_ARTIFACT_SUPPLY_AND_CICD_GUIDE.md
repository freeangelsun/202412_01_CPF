# CPF 산출물 공급과 CI/CD 가이드

[← 문서 홈](README.md) · [제품 소개](../../README.md) · [구조와 배포](CPF_ARCHITECTURE_AND_TOPOLOGY_GUIDE.md) · [용어와 계약](CPF_TERMINOLOGY_AND_CONTRACT_REFERENCE.md)

> **대상**: 빌드·배포 담당자, 저장소 관리자, 공급망 보안 담당자
> **목적**: 검증된 CPF 산출물을 로컬·사내 저장소·폐쇄망으로 안전하게 공급한다.
> **관련 문서**: [설치·업그레이드·되돌리기](CPF_INSTALL_UPGRADE_ROLLBACK_GUIDE.md) · [테스트와 검증 증적](CPF_TEST_AND_EVIDENCE_GUIDE.md)

---


## 0. 문서 계약

| 항목 | 기준 |
|---|---|
| 기준 Source | `master` / `b7c6146e952c10b885952fa2bc6b6786f4611d86` |
| Owner | `cpf-tools` 공급·Build 영역 |
| 이 문서로 완료하는 일 | Source Commit과 일치하는 Artifact·Manifest·Hash·Signature·SBOM을 생성·승격·폐쇄망 반입하고 Rollback Artifact까지 보존한다. |
| 적용 범위 | Library·Application·Frontend·DB Vendor Pack·Offline Bundle·CI/CD Gate |
| 주요 독자 | Build·Release 담당자, 저장소 관리자, 공급망 보안 담당자 |
| 완료 판정 | Source·API·SQL·Config·Test·Runtime·Evidence 중 해당 범위가 실제로 연결되고 검증돼야 한다. |

### 0.1 읽는 순서

1. 책임 경계와 상태 모델을 먼저 확인한다.
2. 정상 절차를 수행하기 전에 권한·설정·데이터베이스·다중 인스턴스 영향을 확인한다.
3. 오류·부분 실패·복구 절차와 완료 점검을 같은 작업 범위로 수행한다.
4. 직접 실행하지 않은 검증은 `완료`로 기록하지 않는다.

---


## 1. 목적

CPF 라이브러리, BOM, Gradle Plugin, 실행 환경, 프런트엔드와 Offline Bundle을 버전·소스 Commit·해시가 추적되는 제품 산출물로 공급한다.

## 2. 공급 원칙

- 수동 JAR 복사 금지
- Immutable 릴리스
- 소스 Commit 추적
- SHA-256
- 서명
- BOM
- SBOM
- License
- Provenance
- Quality Gate
- 승인된 저장소

## 3. 산출물 공급 방식

| Mode | 용도 | 공급원 |
|---|---|---|
| `LOCAL_DEV` | 개발자 로컬 | 검증 로컬 Maven 저장소 |
| `REMOTE` | CI/STG/PROD | Nexus/Artifactory |
| `OFFLINE` | 폐쇄망 | 검증 Offline Bundle |

CI/운영은 로컬 저장소로 Fallback하지 않는다.

## 4. Platform 산출물 Set

- `cpf-core`
- `cpf-common`
- 배치 Contract
- 배치 Testkit
- Platform BOM
- 업무영역 Convention Plugin
- 공개 테스트 Fixture
- 메타데이터

하나의 Promotion 단위로 버전을 맞춘다.

## 5. 로컬 개발

같은 저장소는 Project 의존 대상을 사용한다.

독립 생성 업무영역은 검증 로컬 저장소를 사용한다.

```powershell
.\gradlew.bat publishCpfVerifiedLocalPlatformArtifacts `
  -PcpfArtifactMode=LOCAL_DEV
```

## 6. 승격

```text
Quality Build
→ 격리 Staging Publish
→ POM/Metadata/BOM/Hash 검증
→ Lock
→ Promotion
→ Manifest 공개
```

명세서는 마지막에 공개한다.

## 7. 원격

```powershell
$env:CPF_ARTIFACT_MODE='REMOTE'
$env:CPF_ARTIFACT_REPOSITORY_URL='https://nexus.example/repository/cpf-releases/'
```

```powershell
.\gradlew.bat publishCpfPlatformArtifacts `
  -PcpfArtifactMode=REMOTE
```

인증정보는 비밀값으로 주입한다.

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

소비자는 압축 내부 Maven 저장소를 사용한다.

## 9. 버전

- Semantic 버전
- Compatibility
- 스냅샷 제한
- 릴리스 Immutable
- 소스 Commit
- Build Number
- 데이터베이스 버전
- API/Message 버전

## 10. BOM

업무영역은 BOM으로 CPF 버전 Set을 맞춘다.

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
- 테스트
- 의존 대상
- Packaging
- Checkstyle/Static
- OpenAPI
- 산출물 명세서
- 생성 업무영역 규칙

## 12. bootJar/bootWar

Packaging 후 내부 CPF JAR의 버전과 해시를 명세서와 비교한다.

## 13. 프런트엔드

- `npm ci`
- 잠금 파일
- Typecheck
- Lint
- 단위
- Production Build
- SBOM
- License
- 외부 실행 환경 CDN 0
- Static 해시

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
→ 실행 환경 기본 검증
→ Approval
→ Publish
```

## 15. Required Check

- Build
- 단위/통합
- Architecture
- 보안
- DB Parity
- 생성기
- 프런트엔드
- License/CVE
- 산출물 해시
- 검증 증적

## 16. 서명과 출처 증명

- 산출물 서명
- Signer
- Algorithm
- Timestamp
- 소스 Commit
- Build 도구
- Runner Identity
- 의존 대상
- Attestation

## 17. SBOM

백엔드와 프런트엔드 의존 대상을 포함한다.

- Coordinate
- 버전
- License
- 해시
- 소스
- CVE
- Transitive

## 18. 승격

환경 Promotion 명세서:

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
| Build/테스트 | 공개 금지 |
| 해시 불일치 | Promotion 금지 |
| 서명 실패 | 사용 금지 |
| CVE 정책 위반 | 릴리스 금지 |
| 원격 URL 없음 | 실패 |
| Offline 명세서 없음 | Build 실패 |
| 소스 SHA 불일치 | 재검증 |

## 20. 되돌리기

- 이전 Immutable 산출물
- 설정 버전
- DB 호환
- 프런트엔드
- 게이트웨이
- 배치
- 명세서
- 상태 점검 Gate

## 21. 저장소 권한

- Publisher
- Reader
- 릴리스 Manager
- 보존
- Immutability
- 감사
- Token Rotation

## 22. 폐쇄망

- Bundle 서명
- 전달 매체
- 체크섬
- Import 로그
- Malware Scan
- 내부 저장소
- Update 절차
- License/CVE Feed

## 23. 검증 증적

- Pipeline ID
- Commit
- 산출물 버전
- 해시
- 서명
- SBOM
- 테스트
- Approver
- 게시 대상
- 시각
- 결과

## 24. 체크리스트

- [ ] 수동 JAR 복사가 없다.
- [ ] 산출물 Set 버전이 일치한다.
- [ ] 명세서와 해시가 있다.
- [ ] 릴리스가 Immutable이다.
- [ ] 프런트엔드도 SBOM/License 대상이다.
- [ ] 로컬/원격/Offline 공급이 분리된다.
- [ ] CI 실패 시 게시하지 않는다.
- [ ] 되돌리기 산출물을 보존한다.

## 부록 A. 공급 단계

```text
소스 검증
→ 재현 가능한 빌드
→ 단위·계약·통합 검사
→ 취약점·라이선스·비밀값 검사
→ SBOM·출처·서명 생성
→ 시험 저장소 게시
→ 실행·DB·브라우저 검증
→ 승인
→ 배포 저장소 승격
→ 폐쇄망 묶음 생성
```

## 부록 B. 저장소 배치 예

```text
com/cpf/<module>/<version>/
├─ <module>-<version>.jar
├─ <module>-<version>.pom
├─ <module>-<version>-sources.jar
├─ <module>-<version>-javadoc.jar
├─ <module>-<version>.module
├─ checksums
├─ signatures
├─ sbom
└─ provenance
```

## 부록 C. 폐쇄망 반입

반입 묶음은 산출물, 의존성, 메타데이터, 해시, 서명, 자재 명세서, 라이선스 목록, 설치 명세서와 검증 도구를 포함한다. 반입 전후 해시를 비교하고 인터넷 연결 없이 전체 빌드·설치를 재현한다.

## 부록 D. 취소·회수

잘못 게시된 버전을 조용히 덮어쓰지 않는다. 사용 금지 표시, 영향 소비자, 대체 버전, 회수 사유, 배포 중단과 되돌리기 절차를 기록한다.

## 29. Release Manifest 예

```yaml
releaseId: <release>
sourceCommit: <sha>
productVersion: <version>
buildEnvironment: <sanitized-env>
artifacts:
  - name: cpf-core
    coordinate: <group:artifact:version>
    sha256: <hash>
    signature: <reference>
  - name: cpf-admin
    type: boot-jar
    sha256: <hash>
databasePacks:
  - vendor: postgresql
    version: <version>
    sha256: <hash>
sbom: <reference>
licenseReport: <reference>
configSchemaVersion: <version>
createdAt: <offset-date-time>
```

Manifest는 생성된 파일 목록이 아니라 동일 Release를 설치·검증·되돌릴 수 있는 정본이다.

## 30. 승격 Pipeline

```text
Source Checkout
→ Clean Build
→ Unit/Contract/Static Gate
→ Artifact Hash·Signature·SBOM
→ 임시 Repository 게시
→ Integration/Runtime/DB 검증
→ 승인
→ Release Repository 승격
→ Offline Bundle 생성
```

같은 Version을 다시 Build해 덮어쓰지 않는다. Rebuild가 필요하면 새 Version 또는 Build Metadata를 사용하고 이전 Hash와 구분한다.

## 31. 폐쇄망 반입

1. Bundle Manifest와 외부 저장 매체 Hash를 비교한다.
2. 반입 승인과 Malware Scan 결과를 확인한다.
3. 내부 격리 Repository에 Import한다.
4. Artifact별 Signature·Hash·SBOM을 다시 검증한다.
5. 외부 URL·CDN·Font·Script 의존이 없는지 검사한다.
6. Offline Mode에서 Clean Build와 설치를 수행한다.
7. 내부 Repository 좌표와 Import 결과를 Evidence에 남긴다.

## 32. 공급 Rollback

Release Rollback에는 이전 Application Artifact뿐 아니라 DB Pack, Config Schema, Frontend Static Artifact, Gateway/Batch Definition 호환 정보가 필요하다. Repository에서 Artifact를 삭제해 Rollback하지 말고 승인된 이전 Release를 재승격한다.

## 33. 실행 Artifact와 Release Artifact의 서명 분리

Release JAR·Frontend Bundle·DB Pack의 공급망 서명과 Batch Shell의 실행 서명은 목적이 다르지만 동일한 검증 원칙을 사용한다.

| 구분 | 정본 | 검증 시점 | 실패 시 동작 |
|---|---|---|---|
| Release Artifact | Release Manifest·Attestation·SBOM | 저장소 승격·설치 | 게시·설치 금지 |
| Batch Shell | Worker Catalog·Detached Signature·Trust Key | 매 실행 전 | 실행 금지 |
| Offline Bundle | Bundle Manifest·매체 Hash·내부 Import 로그 | 반입 전후 | 격리·반입 중단 |

Batch Shell은 `SIGNATURE`를 기본으로 하고 SHA-256만 맞는 파일을 자동 승인하지 않는다. 공개키 또는 X.509 Chain은 제품 Trust Store에서 관리하고, 허용 Algorithm과 Key ID를 Manifest에 기록한다. 재서명은 기존 Version을 덮어쓰지 않고 새 Version·새 Hash·새 승인으로 처리한다.

## 부록 Z. 구현 추적 시작점

문서의 설명을 완료 근거로 사용하지 않는다. 아래 경로에서 실제 Consumer·구현·설정·SQL·Test 연결을 확인한다. 경로가 이동했다면 `git ls-files`와 `git grep -n`으로 최신 Owner를 다시 찾는다.

| 추적 대상 | 대표 경로 또는 명령 | 확인 목적 |
|---|---|---|
| Build 정본 | `gradle/cpf-stack.properties`, `settings.gradle` | Stack Version과 Artifact Mode |
| BOM/Plugin | `cpf-tools/build/platform-bom`, `cpf-tools/build/gradle-plugin` | Published Dependency와 Convention |
| Supply Script | `cpf-tools/scripts/`에서 `artifact`, `package`, `offline`, `supply` 검색 | Bundle·Manifest·검증 도구 |
| 검증 | `git grep -n "SHA-256\|SBOM\|signature" cpf-tools cpf-batch` | Release·실행 Artifact Hash·서명·SBOM 구현 확인 |

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
