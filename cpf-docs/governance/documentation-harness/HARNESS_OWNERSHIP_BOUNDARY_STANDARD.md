# Harness Ownership Boundary Standard

- Documentation Harness의 적용·삭제·자체검증 소유범위는 `cpf-docs/governance/documentation-harness/**` 하나다.
- 다른 담당이 관리하는 Requirement, Source, README, Guide, Deliverable, Evidence의 존재·고정 경로·고정 Hash·Source Identity 일치를 Harness 적용 전제조건으로 사용하지 않는다.
- 외부 Source/정본은 산출물 작성·QA 시점의 **읽기 전용 동적 Context 입력**이며 Harness 자체 유효성과 분리한다.
- Source Alignment/Currentization은 산출물 작성/검증 단계에서 명시적으로 실행하며 `VERIFY_HARNESS.ps1`의 Harness self-validation에는 포함하지 않는다.
- APPLY/DELETE는 Harness 소유경로 밖 파일을 생성·수정·삭제하지 않는다.
