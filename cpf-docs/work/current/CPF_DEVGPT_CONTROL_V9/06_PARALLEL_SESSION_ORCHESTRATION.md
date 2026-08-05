# V9 병렬 개발과 격리 Workspace

## Namespace 분리

```text
정적 관리 정본:
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/

개발 GPT Campaign 비제품 산출물:
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/<campaign>/REV-<nnn>/

제품 구현:
공식 Owner Module/**
```

현재 작업 중인 V8 기반 개발 GPT 결과와 신규 V9 Campaign은 경로가 완전히 다르다.

## 배정 원칙

- 같은 State Owner·Consumer·호출 경로·Test Harness를 Connected Functional Slice로 묶는다.
- 일반 Module 제품 파일은 한 Session에만 Exclusive 배정한다.
- Public API/SPI, Root Build, DB Canonical, Generator, OpenAPI Source는 Integration Owner가 반영한다.
- Session은 V9 중앙 원장을 직접 수정하지 않고 결과 파일을 제출한다.
- 기존 Campaign/Revision이 존재하면 새로운 Revision 또는 Campaign ID를 사용한다.

## 정리 단위

가장 작은 정리 단위는 Session Root이고, 전체 정리 단위는 Campaign Revision Root다.

```text
Session 정리:
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/<campaign>/REV-<nnn>/sessions/<session>/

Campaign 정리:
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/<campaign>/REV-<nnn>/
```

사용자 승인 전에는 삭제하지 않는다.
