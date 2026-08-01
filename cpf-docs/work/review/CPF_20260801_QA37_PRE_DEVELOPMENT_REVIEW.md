# CPF QA37 작업 전 독립 리뷰

## 1. 기준

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- 검수 기준 exact SHA: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- Commit Message: `20260801 CPF integrated development checkpoint`
- 직전 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- 최상위 정본: `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
- 신규 개발 입력: `cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`
- 검수 방식: GitHub 원격 Commit·파일 직접 대조
- 로컬 Fresh Clone·Build·Runtime: 현재 ChatGPT 실행 환경의 GitHub DNS 차단으로 미실행

README와 README에서 연결되는 Manual·Guide는 제품 구현 정본으로 사용하지 않으며, QA37 개발자가 수정하거나 완료 근거로 사용하지 않는다.

## 2. 총괄 판정

```text
development_status                  = 부분 구현
verification_status                 = 실패(정적 Source 구조) / Runtime 미검증
deterministic_source_closure         = 실패
gradle_configuration_source_status   = 실패
EDU_32_development_status            = 재확인 필요
EDU_32_verification_status           = 미검증
Customer_Manual_EDU_135_status       = 미구현
release_status                       = 실패
GA_status                            = 실패
```

현재 Commit에는 ADM·BZA·Core·Batch·DB·검증 Source가 다수 포함돼 있으나, Root Build 구조가 깨져 있고 EDU 완료 판정이 실제 Source Closure를 보장하지 않으므로 전체 완료를 승인할 수 없다.

## 3. P0 발견 결함

### QA37-P0-001 Root `build.gradle` 오배치

latest master의 Root `build.gradle`은 `group = 'com.cpf.bizadmin'`, `war` Plugin, BZA Frontend Task를 포함하며 `cpf-biz-admin/build.gradle`과 동일한 Blob이다.

직전 SHA의 Root `build.gradle`은 다음 플랫폼 Root 책임을 보유했다.

- CPF Platform·Stack Version
- `cpfJavaVersion`
- Source exact SHA
- Artifact Mode
- `allprojects`·`subprojects`
- Dependency Management
- Publication
- 공통 Test·Quality Gate

따라서 Root Overlay 생성 또는 적용 과정에서 BZA Module Build Script가 Repository Root에 잘못 배치된 것으로 판정한다.

영향:

- `rootProject.ext.cpfJavaVersion` 초기화 상실
- 전 Module 공통 Repository·Dependency·Toolchain 정책 상실
- Quality Gate·Publication·Artifact 계약 상실
- Java 25 Build 진입 전 Gradle 설정 실패 가능
- 113건 완료 및 47/47 Gate를 latest SHA의 유효 근거로 사용할 수 없음

### QA37-P0-002 Dangling Included Build

latest `settings.gradle`은 다음 경로를 `includeBuild`한다.

- `cpf-tools/build/gradle-plugin`
- `cpf-tools/build/platform-bom`

그러나 latest Commit에서 해당 Build Source가 제거됐다. Root Build 복구와 함께 Ownership을 재확정해야 한다.

금지:

- `settings.gradle`에서 단순히 참조를 삭제하여 기능을 숨기는 방식
- Build Source를 Generated Artifact로 대체
- `cpf-tools/build/**`를 일반 `build/` 산출물로 오인하여 Ignore·삭제

### QA37-P0-003 EDU 32 False Closure

`verify-cpf-edu-executable-coverage.py`는 일반 개발 모드에서는 Catalog와 Matrix의 필드·ID·문자열만 확인한다. 실제 `referenceSources`, `tests`, `publicContracts` Glob 해석은 `--release`에서만 실행한다.

현재 CI와 Local Static Evidence는 `--release` 없이 실행됐고 Evidence에도 `release=False`가 기록돼 있다. 따라서 다음은 증명되지 않았다.

- 실제 Source 파일 존재
- 실제 Test 파일 존재
- Public API/SPI 존재
- Class·Method·Bean·실행 Entry Point
- 실제 `cpf-reference` Consumer
- Runtime 명령 실행 가능성

`EDU-001~032 완료` 및 Canonical 162 Coverage는 재판정해야 한다.

### QA37-P0-004 exact-SHA Evidence Drift

- Current Work Request 기준 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- Completion·Handover·Static Evidence 기준 SHA: `19dd72b5978f2a3c630943c0fff05bee2d2fed34`
- latest result Commit: `23a16f35a5633ce1317920468a69fef00c1a6a41`
- Static Evidence `resultSha`: `null`
- GitHub Commit Status·Workflow Run: 확인된 결과 없음

latest Commit에서 실행된 Build·Runtime Evidence가 없으므로 이전 정적 결과를 latest 성공으로 승계하지 않는다.

### QA37-P0-005 README·Manual 보호 선언과 실제 Commit 불일치

Completion Report와 Handover는 README·연결 Manual·Guide를 수정하지 않았다고 선언한다. 그러나 `19dd72b5978f2a3c630943c0fff05bee2d2fed34` → `23a16f35a5633ce1317920468a69fef00c1a6a41` 비교에서 README와 연결 Guide가 실제 변경됐다.

QA37에서는 해당 문서를 수정·되돌림·보강하지 않는다. 대신:

- 별도 문서 작업 Stream의 변경임을 기록
- Product Source 완료 근거에서 배제
- 개발 Overlay·Manifest에 포함 금지
- 완료 보고의 사실 관계를 최신 Git과 일치시킴

## 4. EDU·고객 매뉴얼 요구 검토

`CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`는 공식 Manual이 아니라 개발 입력 문서이며 다음 135건을 확정한다.

| 영역 | 수량 |
|---|---:|
| 온라인·공통·외부 연계 | 45 |
| Batch | 30 |
| ADM 업무 연동 | 17 |
| BZA 적용·운영 | 14 |
| Gateway | 14 |
| 플랫폼 설치·운영·복구 | 15 |
| 합계 | 135 |

latest Checkpoint에는 이 문서가 추가됐으나, Current Work Request와 113건 완료 범위에는 135건이 포함되지 않았다. 또한 latest Commit 비교에서 `cpf-reference` Product/EDU Source 변경이 확인되지 않았다.

따라서 135건은 소급 완료가 아니라 QA37 신규 개발 범위다.

## 5. 왜 이전 결과가 이렇게 됐는가

확인된 직접 원인은 다음과 같다.

1. `기존 Source 재사용`을 검증 결과가 아닌 기본 완료 가정으로 사용했다.
2. EDU 개발 모드 Gate가 실제 파일 해석을 하지 않았다.
3. Matrix·Catalog·Gate의 비어 있지 않은 문자열을 Source Closure로 승격했다.
4. Product Source와 검증 Tool 변경량을 분리하지 않았다.
5. 최종 병합 Root가 아니라 Overlay·기준 Source를 혼용했다.
6. Root Overlay Manifest가 잘못된 Root `build.gradle`을 정상 파일로 Hash 처리했다.
7. Runtime 환경 차단 상태에서 `부분 구현 0·미구현 0`을 선언했다.
8. Manual EDU 135건이 같은 Commit에 추가됐지만 Current Work Request와 개발 원장에 편입되지 않았다.

## 6. QA37 개발 방향

개발 순서는 반드시 다음을 따른다.

1. Root Build·Included Build 복구
2. latest exact-SHA Fresh Clone Java 25 Configuration·Build Gate
3. EDU 32 완료 상태 해제 및 실제 Source Closure
4. Manual EDU 135건 원본 ID 전수 편입
5. 올바른 Module·Package에 Product Source와 EDU Consumer 개발
6. 중앙 3DB Vendor Pack·운영·복구·관측 연결
7. Docker 실제 Runtime 검증
8. latest exact-SHA Evidence
9. 사용자 Commit·Push
10. 그 후 Codex 독립 검수

P0 Source Gate가 실패한 상태에서 대규모 Runtime·Codex 검수로 넘어가지 않는다.
