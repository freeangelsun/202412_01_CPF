# CPF QA32 독립 코드 검수 요청

## 검수 기준

개발 완료 보고서를 신뢰하지 말고 Source, resolved dependency graph, DB migration, runtime result, browser trace, final artifact를 독립 확인한다.

## 필수 검수

1. Requirement 62건과 Scenario 202건 ID Coverage
2. Defect 60건 Closure와 오판 기각 근거
3. OSS Migration 23건 Consumer 수·Legacy 제거·Final Artifact
4. Core/Common Public API Dependency Leakage
5. Element Plus/TanStack/Router/Pinia/Query/Zod/Orval 실제 화면 이관
6. BFF Session·CSRF·Cookie·권한 변경·Multi-instance
7. SCG Web MVC가 실제 Gateway Data Plane인지, 기존 Forwarder가 남았는지
8. Kafka 실제 Integration과 In-memory Test 분리
9. FULL_OFFLINE, Artifact Provenance, Release Ref, Source Identity
10. Process/File/HTTP/SQL/Parser/Crypto/Bootstrap/Deployment/Probe 공통 Gate
11. CycloneDX/ORT/Syft/Grype와 Final Artifact 상호 대조
12. latest exact SHA Evidence와 CI

## 판정 원칙

- 실행하지 않은 것은 미검증
- Legacy가 Primary로 남으면 부분 구현
- Dependency만 추가됐으면 미구현
- 파일 존재만으로 완료 금지
- 현재 Source와 다른 SHA Evidence는 무효
