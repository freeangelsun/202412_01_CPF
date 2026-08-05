# CPF DevGPT Control V9 적용 정책

## 1. 목적과 기준

이 Namespace는 앞으로 사용하는 **개발 GPT 전용 관리 정본**이다.

- Repository: `freeangelsun/202412_01_CPF`
- 기준 Branch: `master`
- 생성 기준 SHA: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c` (`04_08`)
- 관리 정본: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/`
- 개발 GPT 비제품 산출물 격리 Root: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/`
- 관리 대상: Work Package 775개 + Baseline Stabilization 28개 + Requirement Gap 24개
- Repository 최상위 신규 파일·디렉터리: 금지

현재 작업 중인 기존 개발 GPT는 과거 V8 경로를 계속 사용한다. **신규 개발 요청부터 V9만 사용한다.** V8과 V9 자료를 혼합하거나 상호 덮어쓰지 않는다.

## 2. 완전 격리 원칙

개발 GPT가 만드는 요청·중간자료·로그·Evidence·결과·인수인계·정리 Script는 모두 다음 한 Campaign Root 안에만 생성한다.

```text
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/
└─ <campaign-id>/
   └─ REV-<nnn>/
      ├─ CAMPAIGN_MANIFEST.json
      ├─ CAMPAIGN_CLEANUP_COMMAND.ps1
      ├─ requests/
      └─ sessions/
         └─ <session-id>/
            ├─ REQUEST.md
            ├─ SESSION_ARTIFACT_MANIFEST.csv
            ├─ SESSION_CLEANUP_COMMAND.ps1
            ├─ results/
            ├─ evidence/
            ├─ temp/
            └─ HANDOVER.md
```

제품 Source·SQL·API·Test·Config·Frontend·Script만 공식 Owner Module 경로에 반영한다. 그 외 개발 GPT 산출물을 V9 관리 정본 밖이나 Repository 최상위에 만들지 않는다.

## 3. 중복·덮어쓰기 방지

- 같은 Campaign ID와 Revision 경로가 이미 있으면 생성기는 즉시 실패한다.
- 기존 Campaign, 기존 Session, 기존 Revision을 재사용하지 않는다.
- 다른 Session 디렉터리와 과거 Evidence를 수정·삭제·이동하지 않는다.
- V9 중앙 원장과 Canonical Requirement·Scenario 원장은 개발 Session이 직접 수정하지 않는다.
- Baseline 이후 동일 제품 파일이 변경됐으면 덮어쓰지 않고 Integration Owner에게 요청한다.
- Generated Output은 직접 수정하지 않고 Generator·Template·OpenAPI·DB Canonical 정본에서 재생성한다.

## 4. 개발 요청 자동 축소

다음 상태만 개발 요청에 포함한다.

```text
작업 대상
재개발 대상
재검수 대상
```

다음 상태는 제외한다.

```text
완료 스킵
해당 없음 스킵
소유권 검토
외부환경 차단
```

QA Reopen Feed나 후속 영향 무효화가 있을 때만 완료 항목을 다시 연다.

## 5. Campaign 정리

Campaign 통합·QA Handoff·필요 Evidence 이관이 끝나면 `CAMPAIGN_CLEANUP_COMMAND.ps1`의 exact-path 한 줄 명령으로 해당 Campaign Revision 전체를 정리할 수 있다.

정리 전 필수 확인:

```text
Session Result 중앙 병합 완료
제품 변경 통합 완료
QA Handoff 생성 완료
보존 Evidence 이관 완료
Open Issue 이관 완료
사용자 삭제 승인
```

제품 Source와 중앙 상태 원장은 Campaign 정리 명령의 대상이 아니다.

## 6. 향후 세션 인수인계 필수 내용

사용자가 신규 세션 인수인계를 요청하면 다음을 반드시 포함한다.

- 개발 GPT가 생성·수정한 파일 전수 Manifest
- 제품 필수 파일과 비제품 산출물 분류
- Campaign/Session 고유 격리 경로
- 남겨야 할 Evidence
- 정리 대상 exact path
- 사용자 승인 후 실행할 PowerShell 한 줄 삭제 명령
- 정리 대상이 없으면 `정리 대상 없음`
- Repository 최상위 신규 항목 금지
- 다른 Campaign·Session·중앙 원장 삭제 금지

실제 세션 인수인계 문안은 사용자가 요청할 때 별도로 작성한다.
