# POST-QA37 작업 연속성

## Current

- Reviewed remote SHA: `38089a96e3f4c7c2ba05cda549785b47f67cd462`
- Local HEAD/Working Tree: 재확인 필요
- Source: 부분 구현
- Verification: 미검증
- Next package: QA38 integrated development and verification

## Must remember

- `cpf-starters/` is official.
- Keep core minimal.
- A can bring B through aggregate transitive dependencies, but Generator Profile + resolved lock is primary.
- MQ/JMS/IBM MQ/RabbitMQ/TCP are canonical requirements.
- TPC text is preserved and provisionally mapped to TCP.
- DB is always fresh and generator-first before Codex validation.
- Existing user DB and Docker assets are protected.
- QA37 focused PASS is history, not current exact-SHA final evidence.
- Continuously update verification and defect history.
- Remove garbage safely; never delete a tracked `build` source directory by name alone.

## 타 GPT 전담 보호 경로

다음 경로는 Read Only다.

```text
cpf-docs/deliverables/**
cpf-docs/guides/**
cpf-docs/environment/docker/**
cpf-tools/environment/docker-development-test/**
```

이 작업과 다음 Codex 작업은 해당 경로를 참조할 수 있지만 수정·추가·삭제·이동·이름 변경·자동 포맷·일괄 치환·Stage하지 않는다.
변경 필요성이 발견되면 실제 파일을 건드리지 않고 담당 GPT용 영향도와 작업요건만 기록한다.
Overlay·Delete Manifest·Cleanup 대상에도 포함하지 않는다.
