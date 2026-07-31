# CPF Guide R2 Expert Review

- Repository: `freeangelsun/202412_01_CPF`
- Branch: `master`
- SHA: `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`)
- 작성일: `2026-07-31`
- Git Commit·Push·Branch·Tag·PR: 수행하지 않음

## 이전 R1 문제

- 역할별 구조는 정리됐으나 실무 절차가 충분하지 않았다.
- ADM 운영 문서가 실제 Route Registry를 전수 반영하지 않았다.
- 플랫폼 운영 문서가 정확한 Property Key·Default·Source를 제공하지 않았다.
- 개발·Batch·ADM 문서가 수직 Slice와 실패·복구를 충분히 연결하지 않았다.
- 구조·링크 Gate를 내용 완성도로 오판할 위험이 있었다.

## R2 보강

| 문서 | 보강 |
|---|---|
| 00 | 기준 SHA Module·Starter·Batch 구조, 목표/검증 분리 |
| 01 | Generator Parameter, E2E Reference, Transaction·DB·Kafka·File·Security·Test |
| 02 | Spring Batch Property·API·JobOperator·Restart·Remote·Fencing |
| 03 | OSS Version, ADM Route Inventory, Permission API, Vertical Slice |
| 04 | 59개 Route별 목적·조회·조치·오류·복구·감사 |
| 05 | 41개 Property, 18개 Runbook |
| 90 | 26개 BZA Route별 사용·권한·오류·복구 |
| 91 | SCG Handler·Target·Ledger·Retry·Unknown·게시·Rollback |

## 역할 적합성

| 역할 | 수행 범위 | 판정 |
|---|---|---|
| 신규 개발자 | 생성→계층→DB→비동기→보안→Test→복구 | Source 기준 완료, Runtime 미검증 |
| Batch 개발자 | Job→Stop→Restart→Remote→Fencing→대사 | Source 기준 완료, 다중 Runtime 미검증 |
| ADM 개발자 | Owner Port→OpenAPI→Frontend→Permission→Playwright | Source 기준 완료, Browser 미검증 |
| ADM 운영자 | 실제 Route 전수 사용·오류·대사·감사 | Source 기준 완료, Browser 미검증 |
| 플랫폼 운영자 | Property·설치·DB·배포·Runbook | Source 기준 완료, 실제 환경 미검증 |
| BZA | 선택·Bootstrap·전체 Route·확장·복구 | Source 기준 완료, Browser/DB 미검증 |
| Gateway | SCG 요청·Target·Retry·Ledger·적용·Rollback | Source 기준 완료, Scale-out/Fault 미검증 |

## 완료 선언 제한

이 패키지는 **Guide R2 산출물**로서 완료다. CPF 제품 Runtime의 상용 완료를 선언하지 않는다. Java 25·Browser·3DB·Kafka·다중 인스턴스·Supply-chain 미검증 상태를 그대로 보존한다.

## 적용 후 검증

```powershell
python .\cpf-tools\scripts\verify-cpf-guide-content.py --root .

pwsh -NoProfile -File .\cpf-tools\scripts\verify-cpf-guide-system.ps1 `
  -Root . `
  -RequireLegacyRemoved

git diff --check
```
