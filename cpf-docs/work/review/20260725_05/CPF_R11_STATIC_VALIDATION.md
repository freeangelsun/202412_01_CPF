# CPF R11 Static Validation

- Base SHA: `b6db56f5ee745558a59ce511ad681216004b9672`
- Environment: ChatGPT isolated Linux container, JDK `javac 21.0.10`
- PowerShell: 설치되지 않아 `.ps1` Runtime 실행은 **미검증**
- Full repository checkout: 제공되지 않아 전체 Gradle/npm 실행은 **미검증**

## 실제 수행 결과

| 검증 | 결과 |
|---|---|
| Golden Generator `com.cpf.core.common` 참조 | **PASS: 0건** |
| Generator PowerShell here-string open/close | **PASS: 63 / 63** |
| Generator `CpfSortDirection direction` 중복 | **PASS: 1건** |
| BAT Center-Cut `core.common` import | **PASS: 0건** |
| REF Batch/Center-Cut `core.common` import (overlay 범위) | **PASS: 0건** |
| Overlay `com.cpf.common.utils` import | **PASS: 0건** |
| Overlay cpf-core 외 `core.common` import | **PASS: 0건** |
| Public Center-Cut API/SPI isolated `javac` | **PASS** |
| Public ServiceCall API isolated `javac` | **PASS** |
| Public Execution Annotation isolated `javac` | **PASS** |
| CpfStrings/Lists/Maps/Ids/Values + existing CpfDates isolated `javac` | **PASS** |
| Paging helper + existing Page/Sort dependencies isolated `javac` | **PASS** |
| Overlay 개발 찌꺼기 (`*.tmp/*.orig/*.rej/*~`) | **PASS: 0건** |

Paging isolated compile의 첫 두 시도는 기존 `CpfSort`, `CpfSortDirection`을 test source set에 넣지 않아 실패했다. 해당 dependency를 포함한 최종 isolated compile은 PASS했으며 Source 오류로 판정하지 않는다.

## 통합검증에서 실행할 Gate

```powershell
pwsh -File .\cpf-tools\scripts\verify-r11-source-product.ps1 -RunBuild
```

세부 Gate:

- `check-r11-public-boundary.ps1`
- `check-r11-common-capabilities.ps1`
- `check-r11-runtime-entrypoints.ps1`
- `check-r11-admin-ux-security.ps1`
- 기존 Repository hygiene / R10 / Source documentation / Frontend route Gate

실제 실행하지 않은 Gate는 이 문서에서 성공으로 기록하지 않는다.
