# CPF Tools

`cpf-tools`는 Core Platform Framework의 Build Support, DB lifecycle, Generated Customer Domain, Runtime, Release, Testing, Verification 보조 도구의 canonical owner다.

개발/Generator 사용법은 [`CPF 개발자 매뉴얼`](../cpf-docs/guides/01_개발자매뉴얼.md), 설치/DB/배포 운영은 [`CPF 플랫폼 운영 매뉴얼`](../cpf-docs/guides/05_플랫폼운영매뉴얼.md)을 따른다.

## Canonical 물리 경계

```text
cpf-tools/build         Build Support Unit / BOM
cpf-tools/db            CPF DB canonical source / DB3 lifecycle
cpf-tools/generator     Generated Customer Domain definition / engine / lifecycle
cpf-tools/governance    Architecture / ownership / catalog governance
cpf-tools/release       Overlay / publication / release qualification
cpf-tools/runtime       CPF CLI / runtime diagnostics / smoke
cpf-tools/testing       공용 test support
cpf-tools/verification  안정된 통합/기능 Gate
```

일회성 migration/currentizer/QA 회차/날짜별 Script를 current tool로 보관하지 않는다. 과거 실행 이력은 Git History가 담당한다.

## Generated Customer Domain

Generated Project 내부에는 lifecycle ownership/manifest/lock을 영구 저장하지 않는다. 입력 정본은 Framework의 `cpf-<domain>/cpf-domain.yaml` 또는 명시적인 `--file`이며, 검증용 상태는 `build/domain-generator/verification/**`에만 일시 저장한다.

Windows 프로젝트 루트 기준 대표 명령:

```powershell
# 정의 검증 / Dry-run
.\cpf-tools\runtime\cli\cpf.bat domain validate --file cpf-member/cpf-domain.yaml
.\cpf-tools\runtime\cli\cpf.bat domain dry-run --file cpf-member/cpf-domain.yaml

# 생성 / 검증 / 차이 확인
.\cpf-tools\runtime\cli\cpf.bat domain generate --file cpf-member/cpf-domain.yaml
.\cpf-tools\runtime\cli\cpf.bat verify domain --file cpf-member/cpf-domain.yaml --output cpf-member
.\cpf-tools\runtime\cli\cpf.bat domain diff --file cpf-member/cpf-domain.yaml --output cpf-member

# 사용자 변경 보호가 적용되는 upgrade/remove/restore
.\cpf-tools\runtime\cli\cpf.bat domain upgrade member
.\cpf-tools\runtime\cli\cpf.bat domain remove member
.\cpf-tools\runtime\cli\cpf.bat domain restore --file cpf-member/cpf-domain.yaml --output cpf-member

# Generator 전체 정적 검증
.\cpf-tools\runtime\cli\cpf.bat verify all
python .\cpf-tools\generator\verification\verify-cpf-generator-lifecycle.py --root .
python .\cpf-tools\verification\nxt3\cpf_nxt3_generator_gate.py --root .
```

Framework 계약을 바꿨다면 Generator Definition/Schema/Engine을 먼저 수정하고 `cpf-member`, `cpf-external` 회귀 생성물을 같은 Engine으로 재생성한다. 생성물만 직접 수정해서 Generator 결함을 숨기지 않는다.

## DB

공식 관계형 DB Vendor는 Oracle/PostgreSQL/MariaDB 세 개뿐이다. Generated Customer Domain은 Domain별 Physical DB를 생성하지 않고 Customer Business DB + Domain Table Prefix를 사용한다.

```powershell
# Canonical DB artifact 동기화/검증
pwsh -NoProfile -File .\cpf-tools\db\tools\sync-database-artifacts.ps1

# Platform DB 설치/검증은 환경별 Profile과 canonical DB lifecycle Tool을 사용
pwsh -NoProfile -File .\cpf-tools\db\tools\initialize-cpf-database.ps1 -All -RequireRun
```

DB/SQL/Metadata 변경은 Canonical Source → Oracle/PostgreSQL/MariaDB renderer → Migration/Seed/Install/Upgrade/Rollback → Runtime Query/Test의 정합성을 함께 맞춘다.

## 통합 검증

안정된 로컬 진입점은 다음과 같다.

```powershell
# 최신 정적/Generated/Config/Query/ADM-BZA/Hygiene 누적 Gate
python .\cpf-tools\verification\nxt3\run_nxt3_final_all.py --root .

# Build/Test/DB/Generator/Frontend/Browser를 선택적으로 포함하는 전체 제품 Gate
pwsh -NoProfile -File .\cpf-tools\verification\tools\verify-full-product.ps1 -Root . -WithDatabase -WithGeneratorLifecycle -WithBrowser -RequireAll -Profile local

# Release 완료 판정은 clean exact-SHA + 외부 Runtime Evidence가 준비된 환경에서만 실행
pwsh -NoProfile -File .\cpf-tools\release\tools\verify-cpf-release-completion.ps1 -DatabaseProfilePath <oracle,postgresql,mariadb profiles> -BrowserEvidencePath <exact-sha evidence>
```

실행하지 않은 Runtime/DB/Browser/Fault 검증은 PASS가 아니라 `UNVERIFIED/미검증`이다.

## Verification Tool 수명주기

각 verification helper는 Workflow, Gradle task, stable integration script, 공식 Runbook/Developer workflow, 독립 Runtime fault harness 중 하나 이상의 실제 Consumer를 가져야 한다. Consumer가 없고 동일 검증이 current stable Gate에 흡수된 과거 migration/QA/date script는 Repository에 역사 보관하지 않는다.

Tool Hygiene 판정은 `KEEP_CANONICAL_GATE / MERGE_INTO_CANONICAL_GATE / RENAME_CURRENT / REMOVE_CANDIDATE`로 관리하며, 삭제는 exact Delete Manifest와 stale-reference 0을 확인한 뒤 적용한다.
