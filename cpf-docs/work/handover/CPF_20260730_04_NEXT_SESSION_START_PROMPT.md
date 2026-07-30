# 다음 ChatGPT 세션 시작 메시지

아래 내용을 새 세션 첫 메시지로 전달한다.

---

CPF 개발 인수인계다.

Repository는 `https://github.com/freeangelsun/202412_01_CPF`, Branch는 `master`다. 인수인계 작성 기준 최신 Push SHA는 `8fb30708f4accc189c00c6fbf020ab4b22f6c51f`이지만, 먼저 `origin/master`를 다시 확인하고 더 최신이면 그 SHA를 기준으로 작업해라.

다음 두 문서를 최우선으로 읽어라.

- `cpf-docs/work/handover/CPF_20260730_04_CHATGPT_SESSION_HANDOVER.md`
- `cpf-docs/work/current/CPF_20260730_04_REMAINING_DEVELOPMENT_REQUEST.md`

그리고 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`와 실제 Source·SQL·API·Frontend·Test·Script를 대조해라. 문서의 완료 표시는 신뢰하지 말고 실제 Git 구현을 우선해라.

중요한 현재 판정은 다음과 같다.

- Push와 Overlay 반영은 확인됐다.
- 전체 제품 완료는 아니다.
- Clean Install SQL의 FK 생성 순서와 Batch Audit Identity 불일치가 확인됐다.
- Oracle 빈 문자열 Default 의미를 수정해야 한다.
- Batch Job Definition이 실제 Scheduler/Worker/Agent 실행으로 연결되는지 완전히 검증하고 누락을 개발해야 한다.
- Gateway 승인·Apply·ACK·Connection Test·Drift/Reconcile·Rollback을 실제 Runtime까지 완성해야 한다.
- Runtime Policy Metadata/Row Mapping/다중 인스턴스 전달을 보완해야 한다.
- File/Shell 보안 설정은 실제 Scanner/Signature Verifier/Version Pinning Consumer까지 연결해야 한다.
- Active Current/Handover 문서가 이전 SHA를 가리키며 current 폴더에 과거 문서가 혼재한다.
- QA 신규 요건은 아직 미수신이다. 내가 QA 목록을 주면 기존 개발 요청과 Root Cause 기준으로 병합·중복 제거한 뒤 최종 고유 개발 건수를 먼저 보고해라.

ChatGPT가 직접 개발하고 Codex는 최종 독립 검수자로만 사용한다. 부분 구현·미구현·TODO를 남기지 말고 Source, SQL, API, Test, Generator, Guide, Evidence를 함께 완성해라. 실행하지 않은 Test는 성공으로 기록하지 마라. 내 명시적 승인 없이 Commit, Push, Branch를 생성하지 마라.

먼저 최신 master와 정본을 분석한 뒤 P0 DB 정본 결함부터 개발을 시작해라. 진행 중에는 주기적으로 상태를 알려라.

