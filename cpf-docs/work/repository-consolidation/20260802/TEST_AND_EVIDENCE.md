# Test와 Evidence

## 기준

- exact SHA: `1eda8e12fe123281748a4388938c62f11819da1e`
- Source 변경: 없음
- Git Commit/Push: 없음
- 파일 삭제: 없음

## 수행

- 최신 원격 master SHA 확인
- settings.gradle의 7개 Starter Project 확인
- Starter별 build.gradle·AutoConfiguration·주요 Consumer 정적 확인
- Core/Common/Generated Domain/Reference 주요 Dependency 정적 확인
- Platform BOM의 현재 제약 범위 확인
- Root Overlay 경로·ZIP 무결성·SHA-256 검증
- Delete Manifest 50개와 wildcard 없는 PowerShell 명령 유지 확인

## 미수행

- Gradle Build/Test/Publication
- DependencyInsight 전체 Graph
- JAR/WAR Packaging
- Kafka/Redis/OTLP/RabbitMQ Runtime
- 3 Vendor DB Lifecycle
- Browser/Frontend
- 실제 삭제 명령

따라서 Source와 Runtime의 전체 상태는 `미검증`이다. 이번 Evidence는 Architecture/Documentation package 생성과 무결성에 한정된다.

## Profile·Bundle 검증

- 문서 구조·CSV ID 중복·ZIP Hash: 실행
- Profile Catalog↔Generator↔Aggregate POM↔BOM Runtime 검증: 미구현/미검증
- JAR/WAR 포함·제외 및 Footprint: 미검증
- 최상위 목표 정본/기존 Guide 직접 반영: 수행하지 않음

## 최종 적용 Script

- Package 내부 SHA-256 검증 구조: 정적 확인
- Git 안전 Guard·exact-path 삭제·일반 Push 구조: 정적 확인
- PowerShell 실제 실행: 미검증
- 실제 Commit·Push: 수행하지 않음


## Missing delete candidate 멱등성

- 삭제 후보 부재 시 `SKIP_MISSING` 후 계속하도록 Script 수정: 정적 확인
- 존재하는 수정 후보만 충돌 처리: 정적 확인
- PowerShell 실제 실행: 미검증
