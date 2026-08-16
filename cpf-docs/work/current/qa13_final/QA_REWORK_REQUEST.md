# QA13 재검수 요청

개발 GPT는 `CPF_LOCAL_VALIDATION_20260816_171430.zip`의 145 Stage 결과와 추가 Capability Management/ADM 요구를 반영해 Source/Verifier/Runtime/Frontend/SQL/Test를 재개발했다.

QA 재검수 시 특히 아래를 독립 확인한다.

- Starter/Profile → Public API → Runtime registration → Health/Failure/Recovery → ADM common read model/owner command 연결.
- 신규 Public Starter/Provider 추가 시 별도 ADM Top-Level 메뉴/수기 등록 없이 자동 편입.
- 시스템 메타데이터(system/domain/application/instance/starter/capability/provider/version/transaction/trace/execution/operation) 자동 수집·전파.
- 로그 Header/Body는 policy 기반 capture/masking/size/sampling이며 Secret 원문 저장 불가.
- Common operational query와 high-risk owner command 분리.
- Runtime Control/Maintenance mutating path의 Permission/Reason/Approval/Audit.
- DB3 verifier-owned isolated lifecycle이 사용자 기존 DB/volume을 건드리지 않음.
- 실제 FileLog/DB Log/ADM Timeline correlation.
