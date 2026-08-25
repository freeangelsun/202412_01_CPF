# CPF Developer GPT 재검수 요청

현재 Overlay는 Source 개발 및 정적검증 결과물이다. QA 최종 완료 요청이 아니라 **필수 로컬 Runtime 후 재검수 대상**이다.

재검수 순서:
1. Overlay + Delete Manifest 적용.
2. Java25/Docker Full Runtime 한 줄 실행.
3. 오류 발생 시 전체 로그를 Developer GPT에 전달하고 Root Cause 보정 후 같은 전체 시나리오 재실행.
4. 오류 0 이후 Codex 독립 cross-check.
5. Developer GPT Runtime/Codex 상태를 현행화한 다음 QA 최종 검수.
