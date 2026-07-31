# CPF 역할별 매뉴얼

> 기준 Repository `freeangelsun/202412_01_CPF` · Branch `master` · SHA `1536a0d59004ebade7dcb29383cbe2e758547f8e` (`20260731_03`) · 기준일 `2026-07-31`

CPF Guide는 기능 이름을 나열하는 문서가 아니라 **해당 역할의 사용자가 문서만으로 실제 일을 수행하도록 만드는 정본**이다.

| 역할 | 매뉴얼 | 끝까지 수행해야 하는 일 |
|---|---|---|
| 제품·Architecture | [00 프레임워크 안내](00_프레임워크안내.md) | 제품 범위·Module Ownership·Topology·공통 계약 |
| 일반 개발 | [01 개발자 매뉴얼](01_개발자매뉴얼.md) | Generator→API→DB→Kafka→보안→Test→복구 |
| Batch 개발 | [02 배치 개발 매뉴얼](02_배치개발매뉴얼.md) | Job·Step→중단→Restart→Remote→대사 |
| ADM 개발 | [03 ADM 개발자 매뉴얼](03_ADM개발자매뉴얼.md) | Owner Port→OpenAPI→Frontend→권한→Playwright |
| ADM 운영 | [04 ADM 운영자 매뉴얼](04_ADM운영자매뉴얼.md) | 전체 Route 조회·조치·승인·Unknown·감사 |
| 플랫폼 운영 | [05 플랫폼 운영 매뉴얼](05_플랫폼운영매뉴얼.md) | Property→설치→DB→배포→Backup→Runbook |
| 선택형 BZA | [90 BZA 매뉴얼](90_BZA매뉴얼.md) | 설치·Bootstrap→전체 Route→확장→복구 |
| 선택형 Gateway | [91 Gateway 매뉴얼](91_게이트웨이매뉴얼.md) | Route→Target→게시→ACK/NACK→Rollback |

## 판정 원칙

- Source 경로·Property·Route·API가 실제 기준 SHA와 일치해야 한다.
- 현재 구현과 제품 목표를 구분한다.
- Runtime 검증을 실행하지 않은 기능은 `미검증`이다.
- 역할 사용자가 Source를 다시 뒤져야 핵심 절차를 알 수 있다면 `부분 구현`이다.
- 모든 주요 절차는 정상·오류·부분 실패·복구·Evidence를 포함한다.
