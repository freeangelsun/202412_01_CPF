# CPF QA37 Codex Docker 통합 요청서 인수인계

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 문서 작성 기준 SHA: `866b2ff8bbc2a7aaecf91a617b58d79e9a1308a2`
- 실제 Codex 기준: 검수 시작 시 `HEAD == origin/master`
- Local Repo: `C:\dev\projects\jck412_01_CPF`
- Docker Runtime: `C:\dev\Docker\CPF`
- Secret: `C:\dev\Docker\Secrets`
- Main Request:
  `cpf-docs/work/codex/qa37/CPF_CODEX_QA37_FINAL_INDEPENDENT_VERIFICATION_REQUEST.md`
- 전체 설치 Script 재실행 금지
- CPF Schema는 Repository Source로 Codex가 생성
- DB Vendor는 한 번에 하나씩 실행
- Toxiproxy는 정상 경로 성공 후 사용
- 초기 Running Container는 보호
- 이번 실행에서 시작한 Service만 중지
- Docker prune·초기화·Volume 삭제 금지
- Git Commit·Push 금지
- 전체 CPF 검증 상태: 미검증
