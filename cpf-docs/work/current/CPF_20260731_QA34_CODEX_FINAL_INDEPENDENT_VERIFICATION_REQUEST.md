# CPF QA34 Codex 최종 독립 검증 요청서

## 목적

개발을 다시 분석하거나 임의 수정하지 말고, GPT Overlay가 적용·Commit·Push된 **최종 exact SHA**를 Fresh Clone에서 한 번 검증한다. 반복 검증으로 크레딧을 낭비하지 않도록 아래 순서를 변경하지 않는다.

## 시작 입력

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Expected SHA: 사용자가 Push 후 전달한 40자리 SHA
- Java 25, Node 22.16.0, npm 10.9.2
- 승인 npm Registry
- Oracle/PostgreSQL/MariaDB clean/upgrade Profile
- 승인 Backup Manifest
- ADM/BZA Runtime URL과 E2E Fixture 6종
- Kafka/Redis/Process Kill 가능 환경
- ORT/Syft/Grype

## 단일 실행

`cpf-tools/scripts/verify-cpf-qa34-independent-review.ps1`을 한 번 실행한다. 이 Wrapper가 다음을 순서대로 실행한다.

1. exact SHA와 clean Fresh Clone 확인
2. QA34 Source Closure
3. Java 25 empty-cache aggregate Build와 Included Build Test
4. ADM/BZA approved Registry clean `npm ci`, OpenAPI/Orval, lint/typecheck/test/build
5. Chromium/Firefox/WebKit 전체 Route·오류·BFF Security E2E
6. Oracle/PostgreSQL/MariaDB install, V83/V86~V91 upgrade/rollback/reapply/drift/runtime query
7. Kafka/Batch/Scheduler/Gateway/Deployment multi-instance 및 Process Kill 회귀
8. ORT/Syft/Grype/SBOM/Artifact Hash
9. QA33 138/414/552 direct-ID 재판정 `--strict`
10. QA34 Independent Review Evidence 생성

## Codex 수정 금지 원칙

- 검증 중 Source를 임의 수정하지 않는다.
- 실패 시 첫 실패만 보고 끝내지 말고 Wrapper가 수집한 전체 단계별 실패를 그대로 전달한다.
- 환경 미설치는 Source 결함으로 오판하지 말고 `ENVIRONMENT_BLOCKER`로 분류한다.
- Static 문자열 존재만으로 PASS하지 않는다.
- 실행하지 않은 항목을 완료로 기록하지 않는다.
- 검증 자체가 Source Tree를 변경하면 실패다.

## 최종 PASS 조건

- Independent Evidence `sourceSha=resultSha=Expected SHA`
- `sourceDirty=false`, `exitCode=0`, `releaseEligible=true`
- QA33 Requirement 138, Scenario 414, Result 552 정본 Count 일치
- QA33 Unresolved Row 0
- QA34 20 Requirement 모두 완료 Evidence 연결
- Evidence Artifact SHA-256 재계산 일치

Codex는 위 조건이 모두 충족된 경우에만 `독립 검증 완료`라고 보고한다.
