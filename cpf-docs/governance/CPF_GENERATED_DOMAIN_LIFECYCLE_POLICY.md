# CPF Generated Domain Lifecycle 정책

- 중앙 정책 현행화 기준 SHA: `4870b20733875c3955f93846307fa5041e6f6c22` (`07_06`)
- 대상: `cpf-member`와 Generator로 생성되는 `cpf-<domain>` 업무영역
- 구현·Runtime 완료 여부는 이 정책의 고정 상태값이 아니라 current exact-SHA Source/Evidence에서 판정한다.

## 1. 생성 원칙

Generated Domain은 반드시 `cpf-tools/generator/create-domain.ps1` 또는 동일 정본 Generator를 통해 생성한다.

수동으로 Root Folder, `settings.gradle` include, Package, SQL, Manifest를 조합해 생성하지 않는다.

## 2. 필수 Provenance

모든 Generated Domain은 다음을 가진다.

- `manifest/domain-manifest.json`
- `manifest/generator-ownership.json`
- Generator Version·Template Contract Version
- 생성 명령의 비밀정보 제거된 요약
- 생성 기준 Commit과 생성 결과 Hash
- DomainName·3자리 SystemCode·Package·Schema·Table Prefix 충돌 검사 결과

## 3. 삭제·재생성 원칙

Generated Domain 삭제는 다음 없이는 금지한다.

1. Delete Manifest
2. 삭제 사유와 대체 Domain
3. Consumer·Route·DB·Migration·Test·Guide 영향도
4. 재생성 여부와 명령
5. 사용자 승인

재생성할 때 기존 수동 수정 영역을 덮어쓰지 않고 Ownership Hash 차이를 Fail-closed한다.

## 4. Account 사례 적용

- `acc`는 2026-07-22 `7251bd996a99ec61d9ea83559578ead0047d5f47`에서 `cpf-account`로 Rename됐다.
- `cpf-account`는 2026-07-30 `c599b2abc2e4980ce82a41493052ed7529e7d625`에서 당시 `cpf-member`와 함께 삭제됐다.
- 직후 `cpf-member`만 Generator Golden Reference로 재생성됐다.
- Commit Message만으로 Account 제외가 의도인지 누락인지 확정할 수 없으므로 상태는 `재확인 필요`다.
- Account가 다시 필요하면 수동 복원하지 않고 Generator로 신규 생성하고 최신 Template·3DB·Test·Manifest를 적용한다.

## 5. Required Gate

- Manifest Pair 없는 `cpf-*` 업무 Root 금지
- Generator Provenance 없는 업무 Root 금지
- 직접 삭제된 Generated Domain을 Lifecycle Ledger 없이 허용하지 않음
- `settings.gradle`과 Domain Manifest의 양방향 일치
- Fresh Clone arbitrary-domain 생성·검증·삭제 Dry-run
