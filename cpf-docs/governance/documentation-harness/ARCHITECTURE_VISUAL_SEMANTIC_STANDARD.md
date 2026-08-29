# Architecture Visual Semantic Standard

## 목적

Architecture Figure는 보기 좋은 배치보다 **Canonical Owner와 실제 호출 경계의 의미가 정확한 것**이 우선입니다. Figure가 Source/정본과 다른 Module 위치·호출 방향·Owner 관계를 보여주면 Geometry가 정상이어도 FAIL입니다.

## Canonical 분류

- `cpf-backoffice-web`: **Channel/BFF Reference**. Browser session/CSRF, Frontend SPA, Public HTTP Contract 소비를 소유합니다. DB/CPF Internal Java/Business Domain Java project dependency는 0이어야 합니다.
- `cpf-gateway`: **Channel/Edge**. 외부 Entry, trust normalization, routing, rate/resilience를 소유합니다.
- `cpf-backoffice`: **Optional Prebuilt Business Domain**. MBW 업무관리·조직·권한·결재·업무 설정을 소유하며 Platform Control Plane이 아닙니다.
- `cpf-admin`: **Platform Operations / Control Plane**.
- `cpf-batch`: **Batch Runtime**. Job/Worker/Scheduler/Center-Cut/복구 실행을 소유합니다.

## Hard Gate

다음은 1건이라도 있으면 FAIL입니다.

- `cpf-backoffice`를 Operations/Edge/Control Plane 영역에 배치
- `cpf-backoffice-web`과 `cpf-backoffice`를 하나의 Node로 합쳐 Owner를 모호하게 표현
- `cpf-gateway`를 Business Domain 또는 Platform Control Plane Owner로 표현
- `cpf-admin`을 고객 업무 Transaction/Business Domain Owner로 표현
- Browser/Channel이 Business Domain Repository/DB를 직접 호출하는 경로
- 내부 Domain → Domain 호출을 Gateway로 재경유하는 화살표
- Figure의 Node/Zone 의미가 `CPF_FINAL_TARGET_REQUIREMENTS.md` Canonical Owner Map과 불일치

## 검증

Architecture Figure는 `architecture-visual-semantics.json`과 이미지 SHA-256을 함께 관리합니다. Visual Asset이 바뀌면 Semantic Model, Manual Review, Negative Fixture를 같이 갱신합니다. 자동 Gate PASS 후에도 실제 README/DOCX/PDF 삽입 결과를 육안 확인합니다.
