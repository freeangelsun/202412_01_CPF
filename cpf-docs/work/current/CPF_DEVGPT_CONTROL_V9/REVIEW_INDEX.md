# CPF DevGPT Control V9 — Review Index

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 생성 기준: `09dd686c5ae0826594b9c5e1f871d95d95d3ce1c` (`04_08`)
- 관리 정본: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/`
- 격리 Workspace: `cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/`
- 상태: **V8과 비중복 신규 Namespace 패키지**
- Repository 최상위 신규 항목: 0개

## 핵심 변화

1. V8 경로는 현재 작업 중인 기존 개발 GPT용으로 그대로 둔다.
2. 이후 개발 요청은 V9 Namespace만 사용한다.
3. 요청·중간자료·로그·Evidence·결과·인수인계는 Campaign Revision 한 경로에 격리한다.
4. Campaign Revision 경로가 존재하면 생성기를 실패시켜 덮어쓰지 않는다.
5. Campaign 종료 후 exact-path 한 줄 명령으로 비제품 산출물 전체를 정리할 수 있다.
6. 제품 Source는 공식 Module에만 남기고 Campaign Cleanup 대상에서 제외한다.
7. 향후 세션 인수인계에는 세션 산출물 전수 Manifest와 한 줄 정리 명령을 의무화한다.

## 첫 실행

```powershell
powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/initialize-development-management.ps1

powershell -ExecutionPolicy Bypass -File cpf-tools/scripts/devgpt-control-v9/generate-development-requests.ps1 `
  -CampaignId DEV-YYYYMMDD-R01 `
  -AssignmentRevision 1 `
  -MaxItemsPerSession 8
```

## 삭제 경계

V9 Push·검증과 현재 V8 기반 개발 GPT 작업 통합이 끝난 뒤에만 `DELETE_OLD_V8_AFTER_MIGRATION.ps1`을 사용자 승인으로 실행한다.
