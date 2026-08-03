# QA Requirement Detail — CPF-SELF-DEV-S4-009

## 판정

- QA 결과: `미통과`
- 기준 SHA: `2903de14eb9cd6cfcccf8e4d2a3489ee2e4193ca`
- Requirement: Canonical Starter Catalog·Capability Profile 파생 정합성
- QA 회차: `QA-DEV-R1`

## 실제 확인 파일

1. `cpf-tools/generator/contracts/cpf-starter-catalog.json`
2. `cpf-tools/generator/contracts/capability-profiles.json`
3. `settings.gradle`
4. `cpf-tools/build/platform-bom/public-bom/build.gradle`
5. `cpf-starters/profiles/web-api/build.gradle`
6. Persistence JDBC/MyBatis 실제 Java Config
7. `cpf-tools/scripts/verify-cpf-starter-catalog-truth.py`
8. `cpf-tools/scripts/tests/test_verify_cpf_starter_catalog_truth.py`
9. `cpf-docs/work/evidence/20260803/session4/P05_STARTER_CATALOG_R2_TARGETED.json`

## 확인된 메타데이터

- Module 38
- Public Profile 6
- Internal Module 32
- Capability Group 7
- Provider Slot 9
- Settings는 Catalog에서 Project를 Include한다.
- Public BOM은 visibility=public인 6개 Profile을 파생한다.

## 미통과 근거

1. Catalog의 `baselineSha`는 `4aea798c...`로 최신 HEAD가 아니다.
2. Gate는 baseline SHA를 출력만 하고 현재 Git HEAD와 비교하지 않는다.
3. Persistence JDBC Catalog `packageBase`는 `com.cpf.starter.data.persistence.jdbc`이나 실제 Product Config는 `com.cpf.common.config`다.
4. Persistence MyBatis Catalog `packageBase`도 `com.cpf.starter.data.persistence.mybatis`이나 실제 Config는 `com.cpf.common.config`다.
5. Gate는 실제 Java Package와 Catalog PackageBase를 전수 스캔하지 않는다.
6. `publicationRequired=true`인 38개 Module의 Publication Task/GAV/POM/Sources/Javadoc/SBOM을 검증하지 않는다.
7. Profile Build가 Catalog의 Capability Composition과 정확히 일치하는지 Dependency Graph를 비교하지 않는다.
8. Fresh Consumer가 Public Profile/BOM만으로 Resolve·Compile·Run되는지 검증하지 않는다.

## 재개발 요청

- Catalog PackageBase와 실제 Source Package Ownership 정합화
- 최신 Candidate SHA/Revision을 Catalog에 기록하고 Gate에서 exact HEAD 비교
- 38개 Module의 group/artifact/publicationRequired와 Gradle Publication 검증
- Public 6 Profile의 Dependency Graph를 Capability Composition과 비교
- Generated POM/BOM/SBOM/Internal Leak 검사
- Fresh Consumer 6 Profile Resolve/Compile/Smoke Test

## 성공 기대 결과

- Catalog/Settings/Build/Source Package Drift 0
- 38개 Artifact Publication Contract 일치
- Public BOM Internal Artifact Leak 0
- 6개 Profile Fresh Consumer 성공
- Evidence SHA와 Git HEAD 일치
