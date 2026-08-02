# 작업 전 리뷰

## 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch/SHA: `master` / `1eda8e12fe123281748a4388938c62f11819da1e`
- 작업 성격: Repository·문서·Gate 정리 + Starter Architecture 정식화 + 다음 QA 요구 도출

## 해결할 Requirement

- Root·문서·Gate·Generated Domain Lifecycle 정리
- 과거 Current 문서 안전 삭제 후보 확정
- `cpf-starters`의 정식 Root/Owner/Artifact 역할 확정
- Core 경량화와 Domain의 필요한 Starter만 선택하는 목표 정본화
- Framework 전체 기능에 대한 Starter 적합성·이관 후보 전수 평가
- RabbitMQ 지원 방식과 현재 7개 Starter Closure를 다음 QA 요구로 고정
- Guide·Deliverable 갱신 범위 작성

## 현재 상태

- `cpf-starters`: 실제 Gradle Project·JAR Publication·Product Consumer가 있어 삭제 불가
- Root/Final Gate: Starter Container 등록 불일치 위험
- Core: MyBatis·AspectJ·Web/OpenAPI·OTel 등 선택 Runtime 성격 Dependency가 존재
- Cache: Starter와 Common Runtime Ownership 혼재
- Security: Starter와 ADM/BZA 경로 정책 혼재
- BOM/Generator/Reference/Guide: Starter 정식 제품 구조 반영이 불충분
- RabbitMQ: 미구현이며 Kafka Primary만 정본화
- Runtime/DB/Browser 검증: 이 문서 작업 범위에서는 미실행

## 변경 예상

제품 Source를 수정하지 않는다. Governance, Review, Requirement, Guide 갱신 요청, Delete Manifest와 Codex 검수 패키지만 Root Overlay로 제공한다.
