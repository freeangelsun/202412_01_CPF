# 향후 개발 GPT 세션 인수인계 작성 요구사항

이 문서는 실제 세션 인수인계가 아니다. 사용자가 나중에 인수인계를 요청할 때 적용할 불변 작성 기준이다.

## 필수 기준

1. 최신 `origin/master`, exact SHA, Working Tree를 확인한다.
2. `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/DEVELOPMENT_ITEM_STATE.csv`에서 현재 작업 대상만 전달한다.
3. 완료 스킵 항목은 QA 재개방이 없으면 개발·재검수 대상에서 제외한다.
4. 개발 GPT 비제품 산출물은 `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/<campaign>/REV-<nnn>/sessions/<session>/` 밖에 만들지 않는다.
5. 제품 변경은 공식 Owner Module에만 반영한다.
6. 다른 Session, 과거 Campaign, V9 중앙 원장과 Canonical Master를 직접 수정하지 않는다.
7. Baseline 이후 같은 제품 파일이 변경됐으면 덮어쓰지 않고 충돌 요청을 만든다.
8. 목표 환경 직접검증을 먼저 실제 시도하고, 실패하면 가능한 대체검증과 남은 차이를 기록한다.
9. 세션 종료 시 `SESSION_ARTIFACT_MANIFEST.csv`를 작성한다.
10. 세션 때문에 만들어진 정리 가능 파일에 대한 exact-path PowerShell 한 줄 삭제 명령을 제공한다.
11. 제품 Source·필수 Test·승인된 Evidence는 삭제 명령에 넣지 않는다.
12. 정리 대상이 없으면 `정리 대상 없음`이라고 명시한다.

## 인수인계 Cleanup 표준

```powershell
$paths=@("<session-temp-exact-path-1>","<session-temp-exact-path-2>"); $paths | ForEach-Object { if (Test-Path -LiteralPath $_) { Remove-Item -LiteralPath $_ -Recurse -Force } }
```

광범위 삭제 명령, 와일드카드 삭제, `git clean`, `git reset --hard`, `git restore .`는 금지한다.
