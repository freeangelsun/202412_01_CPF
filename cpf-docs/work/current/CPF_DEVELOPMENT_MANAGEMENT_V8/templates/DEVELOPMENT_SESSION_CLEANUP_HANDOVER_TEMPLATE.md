# 개발 GPT 세션 종료·정리 인수인계 Template

## 세션 기준

- Campaign:
- Assignment Revision:
- Session:
- Baseline SHA:
- Result Root:

## 보존 대상

`PRODUCT_REQUIRED`, `EVIDENCE_RETAINED` 파일을 나열한다.

## 정리 가능 대상

`SESSION_TEMPORARY`, `GENERATED_REGENERABLE`, `REJECTED_ARTIFACT` 중 사용자 승인 후 삭제 가능한 exact path만 나열한다.

## PowerShell 한 줄 명령

정리 대상이 있으면:

```powershell
$paths=@("<exact-path-1>","<exact-path-2>"); $paths | ForEach-Object { if (Test-Path -LiteralPath $_) { Remove-Item -LiteralPath $_ -Recurse -Force } }
```

정리 대상이 없으면:

```text
정리 대상 없음
```

제품 Source·SQL·API·Test·Config·Frontend·Script, 중앙 원장, 다른 Session/Campaign 결과와 승인된 Evidence는 삭제 명령에 포함하지 않는다.
