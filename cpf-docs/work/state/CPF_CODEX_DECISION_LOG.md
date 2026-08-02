# CPF Codex Decision Log — QA39 Final

1. 교체 비용과 기존 코드량은 Starter 가치 판정 근거가 아니다.
2. OSS 직접 사용 대비 편의성·확장성·표준화·운영성·독립성이 없으면 완전 제거한다.
3. QA 최종 개발요건이 자체 개발요건보다 우선하며 충돌 시 QA 요건을 적용한다.
4. 완전 제거: AOP Service Access, Validation, Resilience, Feature Flag, 미등록 FTPS/gRPC/S3/Realtime/SMB/SOAP/Webhook.
5. 관련 가치 없는 Core Wrapper/API도 함께 제거하고 core로 옮겨 숨기지 않는다.
6. 공개 선택면은 6 Profile+7 Capability Group이다.
7. 유지 Group은 Data, Messaging, Integration, File, Notification, Security, Platform Operations다.
8. OpenAPI는 web-api, Scheduler는 batch-service에 흡수한다.
9. Provider/Codec/Exporter는 내부 Leaf로 유지하며 Generator binding/resolved lock으로 선택한다.
10. 유지 Capability는 간단한 CPF Public API와 고객사 확장 SPI를 제공하고 OSS 타입을 업무 코드에 노출하지 않는다.
11. Security/Cache Aggregate와 Provider/기술별 Profile은 대체 후 삭제한다.
12. 삭제는 exact path Delete Work Items와 reference-zero 검증, 한 줄 명령으로 수행한다.
13. 개발 GPT는 Developer Implementation Report와 독립 Self Review를 남겨 QA 반복 탐색을 줄인다.
14. 보고서는 완료 증거가 아니라 독립 QA의 검수 진입점이며 실제 Evidence와 다르면 완료가 아니다.
15. Commit/Push/Branch/Tag/PR/Release는 사용자 승인 없이 수행하지 않는다.

Reviewed SHA: `9a9634eb1f28071d47c205cc35227b6d013a4536`

## QA39-R4 정리 결정

- QA 산출물의 Repository Root 배치를 금지한다.
- `CPF_QA39_SELF_DEVELOPED_REQUIREMENTS.csv`를 활성 자체요건 정본으로 사용한다.
- 이전 루트 README 및 중복·충돌 정본은 즉시 정리 Manifest에 기록한다.
- 제품 Source 제거 명령과 QA 문서 정리 명령을 분리한다.
