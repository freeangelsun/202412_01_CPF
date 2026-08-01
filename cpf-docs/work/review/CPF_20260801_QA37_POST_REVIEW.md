# CPF QA37 latest master 독립 검수 결과

## 기준

- 검수 SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- 직전 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 검수 시각: `2026-08-01T18:21:48+09:00`
- Git 쓰기 작업: 수행하지 않음
- Local Clone·Runtime: GitHub DNS 차단으로 미실행
- 원격 GitHub Commit·파일·Blob·문서·Script 대조: 수행

## 최종 판정

| 항목 | 판정 |
|---|---|
| Product Source 개발 | 부분 구현 |
| Root Build Source | 실패 |
| Deterministic Source Closure | 실패 |
| EDU 32 Source Closure | 재확인 필요 |
| Manual EDU 135 | 미구현 |
| Runtime Verification | 미검증 |
| exact-SHA Evidence | 실패 |
| Release/GA | 실패 |

## 확인된 실제 구현

- ADM/BZA Route·Frontend·Controller Source 다수 변경
- Core Network Policy·Transaction/Batch API 변경
- Batch Control/Agent 변경
- 3DB V92·Runtime Query·Generator·검증 Script 추가
- Static Evidence와 Manifest 추가

이는 존재가 확인된 변경 범위이며 Runtime 성공을 의미하지 않는다.

## P0 결함

### 1. Root Build 파손

latest Root `build.gradle`과 `cpf-biz-admin/build.gradle`의 Blob SHA가 동일하다.

```text
fdbb273124e280596a1b8d2aae3842a4e8948c89
```

Root Build가 BZA WAR·Frontend Build Script로 대체됐다. 직전 Root가 소유하던 Platform·Java·Artifact·공통 Project·Publication·Quality 책임이 사라졌다.

### 2. Included Build Source 누락

`settings.gradle`은 `cpf-tools/build/gradle-plugin`, `cpf-tools/build/platform-bom`을 참조하나 latest에서 파일이 존재하지 않는다.

### 3. EDU False Green

EDU verifier는 `release=False`에서 실제 Source/Test/Public Contract Glob을 해석하지 않는다. CI와 Local Evidence 모두 일반 모드다.

Static Evidence의 EDU Gate:

```text
[PASS] CPF EDU executable coverage features=32 canonicalRequirements=162 release=False
```

따라서 `EDU 32/32`는 Source Closure 증거가 아니다.

### 4. latest SHA Evidence 없음

Static Evidence:

```text
sourceSha = 19dd72b5978f2a3c630943c0fff05bee2d2fed34
resultSha = null
```

latest Commit은 `23a16f35a5633ce1317920468a69fef00c1a6a41`다. latest SHA에서 Java·Frontend·DB·Distributed Runtime Evidence가 없다.

### 5. README·Manual 보호 선언 불일치

Completion·Handover는 해당 문서를 수정하지 않았다고 선언하지만 README와 연결 Guide Blob이 직전 Commit과 달라졌다. QA37은 문서 자체를 수정하지 않고 별도 Stream으로 취급해야 한다.

### 6. Manual EDU 135 미편입

135건 Requirement 문서는 latest에 존재하지만 Current Work Request와 113건 완료 원장에 포함되지 않는다. latest Commit에서 `cpf-reference` EDU Product Source 변경도 확인되지 않는다.

## 검수하지 못한 항목

- Java 25 Gradle 실행
- npm clean build
- 3DB 실제 Lifecycle
- Kafka·Redis
- Browser
- Multi-instance·Fault
- Docker Tool
- Local/Remote parity

위 항목은 모두 `미검증`이다.

## 다음 작업 결론

QA37는 P0 Root Build 복구부터 시작한다. Build Gate 통과 후 EDU 32·Manual EDU 135를 실제 Source로 개발하고, Docker 검증·사용자 Push 후 Codex 검수를 수행한다.
