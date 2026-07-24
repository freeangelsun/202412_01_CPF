# CPF Cross-PC / Cross-Account Codex Handover Guide

## 원칙

CPF는 회사 PC, 집 PC와 여러 Codex 계정/세션에서 작업될 수 있다. Git은 Source 공유 수단이고 Continuity State는 실행/중단 문맥 공유 수단이다. 미커밋 Worktree와 Local DB 상태는 PC 사이에 자동 공유되지 않는다.

## 시작

`git status → HEAD/origin/master → Final Target → Requirement Continuity Ledger → Current Request → Decision Log → Continuity State → Evidence → 실제 Source/Diff` 순으로 확인한다.

Continuity에 없는 과거 구현을 Git에서 발견하면 누락으로 보강한다. Continuity에 완료라고 적혀 있지만 Source/Evidence가 없으면 완료를 유지하지 않는다.

## 작업 중

Requirement를 시작할 때 ID, 기존 구현 판정, 목표 Architecture를 기록한다. 의미 있는 Checkpoint마다 변경 파일, 실제 실행, DB 상태, 미검증, 다음 작업을 갱신한다.

## PC별 환경

HOME/COMPANY를 분리하고 Java/Gradle/Node/MariaDB, DB Schema 설치 상태, 마지막 Empty Install/Runtime, Blocker를 기록한다. Credential 값은 기록하지 않는다.

## 중단/크레딧 종료

새 범위를 확장하지 않고 현재 파일을 안전한 상태로 저장한다. Continuity State에 정확한 중단 파일/Requirement/실패 명령/다음 첫 명령을 기록한다. reset/clean/revert 금지.

## 종료

완료/부분 구현/미구현/미검증/실패/재확인 필요를 구분하고 실제 검증 명령과 Evidence를 남긴다. 사용자 승인 없이는 commit/push하지 않는다.
