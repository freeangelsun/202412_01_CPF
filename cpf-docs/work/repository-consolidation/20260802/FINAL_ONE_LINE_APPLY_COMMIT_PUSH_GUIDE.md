
# 최종 Overlay 일괄 적용·Commit·Push 안내

## 목적

Codex 작업 종료 후 현재 Working Tree 전체와 이 Overlay를 한 Commit으로 묶어 `master`에 일반 Push한다.

## 주의

이 Script는 다음을 Commit한다.

- Codex가 남긴 현재 추적·비추적 변경
- 이 Overlay의 Governance·Review·Requirement·Script
- Delete Manifest가 지정한 삭제

Source ZIP 자체가 Repository 안에 있어도 Git Staging에서는 제외한다.

## 사전 조건

- Repository Root에서 실행
- 현재 Branch가 `master`
- Codex 작업이 종료됨
- `origin/master`가 Local HEAD보다 앞서 있지 않음
- Overlay 대상과 Codex 변경이 충돌하지 않음
- Git 사용자 이름·이메일과 Push 권한 설정 완료

## 안전 동작

- Package 내부 Hash 검증
- 동일 경로 수정 충돌 시 적용 전 전체 중단
- 삭제 후보가 이미 없으면 `SKIP_MISSING`으로 기록하고 계속 진행
- 삭제 후보가 실제로 존재하면서 수정되어 있을 때만 적용 전 전체 중단
- exact-path Backup을 `%TEMP%`에 생성
- force push·reset·restore·stash·clean 사용 안 함
- Push 실패 시 Local Commit과 Backup 경로 출력

## 실행

최종 답변과 함께 제공되는 `*_ONE_LINE_COMMAND.txt`의 한 줄을 Repository Root에서 그대로 실행한다.

## 실행하지 못한 검증

현재 환경에는 PowerShell Runtime이 없어 실제 실행은 미검증이다.
Script 구조·필수 Guard·괄호·Package Hash는 정적으로 검증했다.


## 이미 삭제된 가비지 처리

다른 GPT 또는 사용자 셸이 Delete Manifest의 파일을 먼저 삭제했더라도 오류로 처리하지 않는다.

```text
파일 존재
  → 정확한 경로만 삭제
  → [DELETE] 기록

파일 없음
  → 삭제 완료와 같은 멱등 상태로 간주
  → [SKIP_MISSING] 기록
  → 나머지 Overlay·Commit·Push 계속
```

단, 삭제 후보가 아직 존재하면서 내용이 수정된 경우에는 사용자 작업 보호를 위해 적용 전에 중단한다.
