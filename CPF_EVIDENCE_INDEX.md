# CPF Evidence Index

## 기준

- 최신 검수 기준 Commit: `a63380e6c736fa9c5ae7e425d0e301d21ef3b848`
- 기존 Evidence 위치: `cpf-docs/evidence/20260722_01/`
- 현재 판정: **재확인 필요**

## Stale 판정 원칙

`20260722_01` Evidence 이후 WIP Commit에서 Module·Package·SystemCode·DB Physical Name·SQL·Config가 광범위하게 변경됐다. 따라서 기존 Evidence는 과거 구현 확인 자료로만 사용하며 최신 제품 완료 Evidence로 자동 승계하지 않는다.

## 다음 Evidence Set 필수 범위

새 Evidence Directory는 실제 작업 완료 시점의 Commit을 이름과 Metadata에 기록하고 다음을 포함한다.

- OS, JDK, Node, npm, PowerShell, MariaDB Version
- Git Commit, Dirty 여부, 실행 시작·종료 시각
- Empty DB 이전 Schema 목록과 CPF Object 부재 확인
- Install·Provision·Seed·Verify 전체 명령과 원문 결과
- 생성 Schema·Table·Index·Constraint·View·Procedure·Trigger 목록
- Meta Seed 범주별 Count와 Consumer 확인
- 다른 Schema 미손상 확인
- 전체 Backend Build·Test
- ADM/BZA lint·typecheck·unit·production build
- 전체 Module Startup과 Health
- Local/Remote Domain Call, Gateway, External, Batch, Center-Cut
- Agent Claim·Lease·Fencing·Failover
- ADM/BZA Browser E2E와 권한·승인·감사
- Reinstall·Upgrade·Rollback·Recovery
- Secret 제거·Masking 확인

## Evidence 완료 조건

명령, Profile, Environment, 기준 Commit, 결과, 민감정보 제거 여부가 없으면 Evidence로 인정하지 않는다. 직접 실행하지 않은 항목은 성공으로 기록하지 않는다.
