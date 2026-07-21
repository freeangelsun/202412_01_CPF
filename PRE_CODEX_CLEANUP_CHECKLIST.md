# Pre-Codex Cleanup Checklist

이 파일은 Repository에 최종 보관하는 제품 문서가 아닙니다. 아래 정리 후 삭제합니다.

## 1. Root에서 삭제할 패키지 부속 파일

이전 통합 압축본을 Root에 모두 복사했다면 아래 파일은 삭제합니다.

```text
README_PACKAGE.md
PACKAGE_MANIFEST.json
SHA256SUMS.txt
REFERENCE_CODEX_INTERRUPTED_PROGRESS.txt
CPF_CODEX_INTEGRATED_REQUEST_PACKAGE_20260721.zip
```

유사한 이름의 날짜별 압축본, 임시 TXT와 중복 패키지 디렉터리도 Repository Root에 넣지 않습니다.

## 2. 교체할 파일

기존 `README.md`는 이 패키지의 새 `README.md`로 교체합니다.

기존 README를 별도 이름으로 복사해 두지 않습니다.

삭제 대상 예:

```text
README_OLD.md
README_BACKUP.md
README_20260721.md
```

## 3. 새로 복사할 구조

```text
README.md
cpf-docs/
  README.md
  architecture/ARCHITECTURE_GUIDE.md
  development/DEVELOPER_GUIDE.md
  development/GENERATOR_GUIDE.md
  development/EDU_GUIDE.md
  operations/OPERATOR_GUIDE.md
  operations/INSTALLATION_GUIDE.md
  operations/DEPLOYMENT_GUIDE.md
  operations/RECOVERY_GUIDE.md
  security/SECURITY_GUIDE.md
  api/API_GUIDE.md
  releases/MIGRATION_GUIDE.md
  releases/RELEASE_NOTES.md
```

## 4. Codex 작업 전에 유지할 파일

현재 작업을 지시하는 아래 6개 파일은 삭제하지 않습니다.

```text
CPF_CURRENT_WORK_REQUEST.md
CPF_FINAL_TARGET_REQUIREMENTS.md
CPF_GAP_MATRIX.md
CPF_STABILIZATION_REPORT.md
CPF_EVIDENCE_INDEX.md
CPF_REVIEW_PROGRESS_COMPLETION_GUIDE.md
```

Codex가 공식 문서 구조를 정리하면서 최종 위치로 이동하고 링크를 보정하도록 둡니다.

## 5. 즉시 삭제하지 말고 Codex가 판단할 항목

아래는 실제 Consumer와 Script 참조를 확인하지 않고 사용자가 먼저 삭제하면 안 됩니다.

```text
pfw/
cmn/
adm/
bza/
bat/
mbr/
acc/
xyz/
pfw-gateway-runtime/
legacy SQL
legacy config
legacy script
```

이들은 공식 `cpf-*` Module로 Migration한 뒤 Codex가 잔존 참조 0을 확인하고 삭제해야 합니다.

## 6. 최종 확인

```text
[ ] 새 README.md가 Root에 있다.
[ ] cpf-docs 문서 구조가 있다.
[ ] 패키지 부속 파일을 삭제했다.
[ ] 압축 파일과 임시 TXT가 Root에 없다.
[ ] 6개 작업·검수 정본은 유지했다.
[ ] legacy Source Module은 임의 삭제하지 않았다.
[ ] git status에서 의도한 문서 변경만 확인했다.
```

정리 후 이 `PRE_CODEX_CLEANUP_CHECKLIST.md` 자체도 Repository에 커밋하지 말고 삭제합니다.
