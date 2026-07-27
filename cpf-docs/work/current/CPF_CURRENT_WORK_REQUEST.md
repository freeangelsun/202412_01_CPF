# CPF Current Work Request

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 현재 기준 SHA: `9e4edaef24dce901fdcf722e2e6d8c0cf0a623ba` (`20260727_02`)
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 현재 상태: ChatGPT 1차 변경 Push 완료, QA 최종 개선요청 대기 중

과거의 “새 기능 개발 없이 통합검증만 수행” 요청은 현재 상태와 맞지 않으므로 더 이상 현재 작업 지시로 사용하지 않는다.
다음 구현 시작 전 최신 master와 QA 최종 요구를 다시 읽고 아래 준비 범위를 통합한다.

## 2. 다음 개발 우선순위

### P0-1 Generated Domain Golden 표준화

- MBR/ACC/신규 Domain을 동일 Generator-owned 구조로 정규화
- Root/ADM/REF/Core의 특정 Generated Domain 고정 의존 제거
- SystemCode/DB/Route/Config/Installer를 명시 Metadata 기반으로 통일
- 삭제/재생성 가능한 Golden Template 확립

### P0-2 CPF Artifact 공급·배포 모델 완성

세 모드를 동일 Dependency 계약으로 지원한다.

- `LOCAL_DEV`: 로컬 Source 변경 → CPF Artifact/Project Dependency 자동 반영
- `REMOTE`: Jenkins/CI → 승인된 Nexus/Artifactory의 고정 CPF Version 사용
- `OFFLINE`: Registry 없음 → manifest/checksum 포함 Offline CPF Library Bundle 사용

CI/STG/PROD에서 Local Repository fallback은 금지하고 fail-closed한다.
업무 Domain의 bootJar/bootWar/lib는 Gradle이 필요한 CPF Library를 자동 포함하며 수동 JAR 복사를 표준으로 사용하지 않는다.

### P0-3 Gate·PowerShell·Tool 제품화/정리

- 전체 Gate/Script/Gradle Task Inventory 작성
- `DEV_ONLY` / `CI_RELEASE` / `PRODUCT_ADMIN_TOOL` 분류
- 중복/Legacy/무호출/임시 Gate는 사용처와 Requirement 대체를 확인한 뒤 통합 또는 삭제
- 개발 대표 Gate를 `QUICK` / `VERIFY` / `FULL` 3단계로 정리
- 공식 Tool의 모든 옵션/Default/환경변수/입출력/Side Effect/실패/복구/예제를 문서화
- 개발/CI Gate를 Runtime 배포물에 포함하지 않음
- 관리자용 설치/Upgrade/Rollback/Generator/Verify Tool만 별도 제품 Tool로 제공

정본 Guide:
`cpf-docs/guides/CPF_GATE_AND_TOOL_LIFECYCLE_GUIDE.md`

### P0-4 DB Canonical / Vendor Lifecycle

- Canonical → Generator/Template → Vendor → Migration/Rollback → Verify/Manifest → Runtime 순서 강제
- Generated Domain DB 변경 Generator-first
- Vendor별 지원 수준과 실제 Runtime 검증 분리
- Drift/Fresh/Upgrade/Rollback/Reapply 검증 강화

### P0-5 BAT 구조 전환 완결

- 삭제된 Legacy Runtime/EDU 기능 parity 확인
- Generated Domain JobPack 표준 정립
- Control/Scheduler/Worker/Center-Cut/Agent 책임 최종 정리

### P1

- Gateway/Service Call failover/timeout/retry/UNKNOWN_RESULT/O-S-B/tracing
- ADM/BZA 상용 운영 UX, 권한/승인/Audit/Masking
- 운영 추적/복구 체계 통합
- 설치/Upgrade/Rollback/배포 제품화
- EDU/OpenAPI/JavaDoc/Guide 지속 보강

## 3. 다음 작업 시작 방식

1. 최신 master 확인
2. 이전 Codex/ChatGPT 변경 품질 리뷰를 사용자에게 먼저 제시
3. QA 최종 개선요청을 위 준비 범위와 병합하고 중복 제거
4. 보호할 기존 성공 기능과 변경 Side Effect를 먼저 산정
5. 그 후 구현
6. 작업 종료 시 Requirement/Review/Handover/Continuity/Evidence 갱신

과거 PASS는 무조건 유지하지 않는다. 이후 변경 영향권에 들어온 PASS는 `재검증 필요`로 다시 연다.
반대로 변경과 무관한 고비용 전체 검증을 습관적으로 반복하지 않는다.

## 4. Codex 관련

Codex는 바로 투입하지 않고 ChatGPT 개발을 몇 차례 더 진행할 예정이다.
현재 Codex Checklist/Handover는 중간 자료이며, 실제 Codex 투입 직전에 최신 master 전체 Diff와 누적 영향도를 기준으로 다시 작성한다.

향후 Codex에는 반드시 다음을 인계한다.

- ChatGPT 변경으로 영향받은 과거 PASS 재검증 대상
- Gate/Tool Inventory와 삭제 후보
- QUICK/VERIFY/FULL 정합성
- DEV/CI Gate의 Runtime 배포 제외 여부
- LOCAL_DEV/REMOTE/OFFLINE Artifact 공급 검증
- 실행하지 않은 검증은 미검증 유지
