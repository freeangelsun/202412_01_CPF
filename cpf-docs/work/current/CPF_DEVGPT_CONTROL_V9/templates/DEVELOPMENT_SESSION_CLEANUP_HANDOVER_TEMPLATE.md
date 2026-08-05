# 개발 GPT 세션 정리 인수인계 Template

실제 인수인계는 사용자가 요청할 때 현재 결과를 기준으로 작성한다.

## 필수 포함

- Campaign·Revision·Session
- Baseline SHA
- 제품 변경 파일
- 비제품 산출물
- 보존 Evidence
- 정리 가능 exact path
- 사용자 승인 후 PowerShell 한 줄 삭제 명령
- 정리 대상이 없으면 `정리 대상 없음`

모든 비제품 산출물은 다음 경로 아래여야 한다.

```text
cpf-docs/work/current/CPF_DEVGPT_CONTROL_V9/_session_workspace/<campaign>/REV-<nnn>/sessions/<session>/
```
