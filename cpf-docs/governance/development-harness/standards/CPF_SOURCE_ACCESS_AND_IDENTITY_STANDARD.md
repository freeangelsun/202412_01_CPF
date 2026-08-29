> Development Harness 내부 통합 표준. 이 파일은 독립 정본이 아니며 `CPF_DEVELOPMENT_HARNESS.md`의 통제를 받는다.

# CPF Repository / Source / Release Governance

- 중앙 정책 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)

- master 직접 Push 금지, 보호 Branch와 Review 사용
- Core/Batch/Security/Deployment 변경은 Code Owner Review 대상
- Source Merge 권한과 Artifact Promotion/Production Deployment 권한 분리
- 위험 운영조치 Requestor와 Approver 분리
- Machine account 최소 scope + Rotation + 감사
- Artifact에 Git SHA, checksum, SBOM, provenance, signature 연결
- Domain Repository별 접근권한 독립 부여 가능
- Deployment Manifest에는 Secret Reference만 저장
- Break-glass는 TTL, 사유, 사후 Review/Audit 필수

실제 GitHub Branch Protection/Team 설정은 Repository 관리자 실행 Evidence가 있어야 PASS로 판정한다.


## Public Framework Repository Default-Deny Distribution

Private implementation Repository와 Public Framework Repository의 Source 경계는 allowlist 기반으로 강제한다. Public staging은 항상 빈 디렉터리에서 시작하며 명시적으로 분류된 파일만 생성한다.

- Public 허용 분류: `PUBLIC_USER_DOC`, `PUBLIC_USER_SCRIPT`, `PUBLIC_USER_CONFIG`, `PUBLIC_GENERATED_SOURCE`, `PUBLIC_BOOTSTRAP`, `PUBLIC_DEPLOY_ASSET`, `PUBLIC_RELEASE_METADATA`.
- `cpf-core`, `cpf-starters/**` 구현, `cpf-admin`, 내부 `cpf-backoffice` Domain Source, governance/work/evidence, private release implementation은 Public staging에 포함하지 않는다.
- 외부 `cpf-backoffice-web`은 Frontend SPA + DB-less Pure Spring Boot BFF가 통합된 공식 Channel Reference로 명시적 Public Reference classification에 포함할 수 있다. 이 허용이 내부 `cpf-backoffice` Domain Source 공개를 의미하지 않는다.
- staging의 미분류 파일, private/internal path, secret-like file/content, source/JAR/POM/BOM leakage는 FAIL이다.
- clean public consumer/generated reference build/test, manifest/hash/SBOM/provenance, staged diff/whitespace 검증이 모두 PASS하기 전 commit/push에 도달하지 않는다.
- 실제 remote Git push는 사용자 또는 승인된 release trigger가 명시적으로 수행하며 Gate failure를 우회하는 push option을 제공하지 않는다.

Canonical implementation owner는 `cpf-tools/release/public/**`이며 기존 Publication/Release 자산을 재사용한다. 별도 `ReleaseV2`/`PublisherV2` 계층을 만들지 않는다.
