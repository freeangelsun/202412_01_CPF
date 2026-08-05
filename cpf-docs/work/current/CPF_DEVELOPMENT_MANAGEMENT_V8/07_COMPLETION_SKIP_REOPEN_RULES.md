# 완료 제외, 보존과 QA 재개방

개발 요청 생성기는 `완료 스킵`, `해당 없음 스킵`, `소유권 검토`, `외부환경 차단`을 자동 제외한다. 후자의 두 상태는 별도 의사결정·환경 요청 목록에 남긴다.

QA Feed Action:

- `REDEVELOP`: `재개발 대상`
- `REREVIEW`: `재검수 대상`
- `INVALIDATE_IMPACT`: 변경 영향으로 Evidence 무효화 후 `재검수 대상`
- `REOPEN_OWNER`: `소유권 검토`
- `EXTERNAL_BLOCK`: `외부환경 차단`

QA 재개방 시 기존 완료 SHA와 Evidence는 삭제하지 않고 이력으로 보존하되 `evidence_valid=false`로 바꿔 현재 완료 근거로 사용하지 않는다.

## 파일 보존

`완료 스킵`된 항목의 다음 파일은 QA 재개방 여부가 확정될 때까지 보존한다.

- 제품 Source·SQL·API·Test·Config·Frontend·Script
- Requirement·Scenario별 Result
- 직접검증·대체검증 Evidence
- 변경 Manifest와 완료 기준 SHA
- QA Handoff

Session 전용 임시 파일은 `SESSION_ARTIFACT_MANIFEST.csv`에서 정리 가능으로 판정하고 사용자 승인 후 exact-path 명령으로 삭제한다.

다음 개발 요청 생성 시 완료 항목의 파일을 삭제하거나 새 요청 파일로 덮어쓰지 않는다. 완료 항목은 상태 필터로 제외할 뿐 이력과 Evidence는 유지한다.
