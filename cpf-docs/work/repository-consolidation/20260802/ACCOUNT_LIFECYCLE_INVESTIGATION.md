# `acc` / `cpf-account` Lifecycle 조사

## 결론

`acc`는 현재 Codex가 삭제한 것이 아니다.

### Timeline

| 시각(KST) | Commit | 변화 |
|---|---|---|
| 2026-07-22 15:23:02 | `7251bd996a99ec61d9ea83559578ead0047d5f47` | `acc/**`를 `cpf-account/**`로 전체 Rename |
| 2026-07-30 02:47:18 | `c599b2abc2e4980ce82a41493052ed7529e7d625` | `cpf-account/**` 전체 삭제, 같은 Commit에서 당시 `cpf-member/**`도 삭제 |
| 2026-07-30 02:49:24 | `4732d17259e39da93e781fd14cd545b3c897fa87` | `cpf-member`가 `GENERATED_DOMAIN` Golden Reference로 재생성 |

## 왜 삭제됐는가

확인 가능한 사실은 Generated Domain 구조를 초기화하고 `cpf-member`를 새 Golden Reference로 재생성한 흐름이다.

그러나 `c599b2ab…`의 Commit Message는 `20260730_01`뿐이며 Account를 제외한 명시적 Architecture Decision이나 Delete Manifest는 확인되지 않았다. 따라서 다음 중 어느 하나라고 단정할 수 없다.

- Account가 더 이상 필요 없어 의도적으로 제거
- Golden Reference를 Member 하나로 줄이기 위한 구조 결정
- Account 재생성이 누락
- 여러 작업자의 Overlay 병합 과정에서 의도치 않게 제외

현재 판정:

```text
development_status = 재확인 필요
verification_status = 재확인 필요
```

## 조치 원칙

1. `cpf-account`를 과거 Git에서 수동 복원하지 않는다.
2. Account 업무영역이 필요하다는 Requirement가 확정되면 최신 Generator로 새로 생성한다.
3. 생성 시 `DomainName=account`, `SystemCode=ACC`, Package·Schema·Route·Port·DB 충돌을 사전 검증한다.
4. Generator Manifest·Ownership Hash·3DB Template·Test·OpenAPI·EDU를 최신 정본으로 생성한다.
5. 임의 Folder 복사나 과거 Source Cherry-pick은 금지한다.
6. Generated Domain 삭제도 Delete Manifest·영향도·사용자 승인 없이는 금지한다.
