# CPF QA37 Apply, Delete Review, Cleanup and Rollback

Baseline exact SHA: `1edd96c6dcc69b0b4d6e9e22a0709d910d7cfb04`

## Overlay 적용

Staging에 ZIP을 해제한 뒤 `cpf-tools/scripts/apply-cpf-qa37-overlay.ps1`을 실행한다.

- exact Baseline SHA와 Clean Working Tree 확인
- README와 연결 Guide·Manual 보호
- 기존 파일을 Repository 밖에 Backup
- Overlay 복사
- `git diff --check`
- merged Repository QA37 Source Gate
- 실패 시 외부 Backup Rollback

## stale 추적 문서

`CPF_20260801_QA37_DELETE_MANIFEST.txt`의 50개 문서는 Overlay 적용 시 삭제하지 않는다. Review CSV 확인 후 `remove-cpf-qa37-approved-stale-documents.ps1 -ConfirmRemoval`을 명시적으로 실행한다. Script는 `cpf-docs/work/current` 경로만 허용하고 보호 문서를 거부하며 삭제 전 외부 Backup을 만든다.

## 생성 가비지와 빈 폴더

`cleanup-cpf-generated-garbage.ps1`은 Build Cache, node_modules, Frontend 결과, Test 결과, log/tmp/Python Cache와 빈 폴더만 제거한다. `cpf-tools/build`의 Gradle Plugin/BOM Source와 `.git`, Source, SQL, 문서는 보호한다.

## Rollback

`rollback-cpf-qa37-overlay.ps1`은 외부 Backup Manifest만 사용한다. Git reset·restore·clean·stash·commit·push를 수행하지 않는다. stale 문서 삭제 Backup은 별도 BackupRoot에서 복구한다.
