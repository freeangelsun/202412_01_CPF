# Repository Garbage Cleanup Review

Use the existing safe cleanup script only after a clean Working Tree check:

`cpf-tools/scripts/cleanup-cpf-generated-artifacts-safe.ps1`

It previews first and excludes paths containing tracked files.

Never use a recursive rule that deletes every directory named `build`.
`cpf-tools/build/gradle-plugin` and `cpf-tools/build/platform-bom` are tracked product tooling source.

Tracked stale documents are not garbage until:
1. conclusions are absorbed,
2. active references are zero,
3. ownership is reviewed,
4. user approves deletion.

The Delete Manifest records candidates and does not delete them.

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
