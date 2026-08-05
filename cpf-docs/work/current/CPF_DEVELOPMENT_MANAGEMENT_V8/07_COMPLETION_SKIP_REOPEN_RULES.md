# 완료 제외와 QA 재개방

개발 요청 생성기는 `완료 스킵`, `해당 없음 스킵`, `소유권 검토`, `외부환경 차단`을 자동 제외한다. 단, 후자의 두 상태는 별도 의사결정/환경 요청 목록에 남긴다.

QA Feed Action:

- `REDEVELOP`: `재개발 대상`
- `REREVIEW`: `재검수 대상`
- `INVALIDATE_IMPACT`: 변경 영향으로 Evidence 무효화 후 `재검수 대상`
- `REOPEN_OWNER`: `소유권 검토`
- `EXTERNAL_BLOCK`: `외부환경 차단`

QA 재개방 시 기존 완료 SHA와 Evidence는 삭제하지 않고 이력으로 보존하되 `evidence_valid=false`로 바꿔 현재 완료 근거로 사용하지 않는다.
