<div align="center">

# Core Platform Framework

**설계, 개발, 실행, 운영, 복구와 다음 변화를 하나의 제품 기준으로 연결합니다.**

Modular Monolith · MSA · 동일 JVM · 분리 WAS · 다중 인스턴스 · Spring Batch · Kafka · ADM Control Plane

[프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) ·
[개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) ·
[배치 개발](cpf-docs/guides/02_배치개발매뉴얼.md) ·
[ADM 운영](cpf-docs/guides/04_ADM운영자매뉴얼.md) ·
[플랫폼 운영](cpf-docs/guides/05_플랫폼운영매뉴얼.md)

</div>

---

CPF는 공통 Library나 특정 프로젝트 Sample이 아니라 시스템의 전체 생명주기를 일관된 계약으로 관리하는 **Core Platform Framework**입니다.

- `cpf-core` — Topology-independent Public API·SPI
- `cpf-common` — 선택형 공통 기능
- `cpf-admin` — 플랫폼 운영 Control Plane
- 생성 Domain — 자신의 API·Transaction·Data Owner
- `cpf-batch` — Spring Batch 기반 실행 제품
- `cpf-biz-admin` — 선택형 조직·권한·결재 제품
- `cpf-gateway` — 선택형 Spring Cloud Gateway MVC 제품
- Generator·DB Vendor Pack·설치·검증·Supply-chain Tool

```text
Requirement
→ Owner·Public Contract
→ Source·DB·Config·Frontend
→ Runtime Consumer
→ Security·Approval·Audit
→ Failure·Recovery
→ Test·Evidence
→ Install·Upgrade·Rollback
```

같은 JVM에서는 Local Adapter가, 분리된 실행 환경에서는 Remote Adapter가 같은 Public Contract를 실행합니다. CPF는 응답 유실이나 부분 실패를 단순 실패로 축약하지 않고 `UNKNOWN_RESULT`를 대사해 성공·실패·재처리·보상을 확정합니다.

| 목적 | 문서 |
|---|---|
| 제품·Architecture | [00 프레임워크 안내](cpf-docs/guides/00_프레임워크안내.md) |
| 일반 개발 | [01 개발자 매뉴얼](cpf-docs/guides/01_개발자매뉴얼.md) |
| Batch 개발 | [02 배치 개발 매뉴얼](cpf-docs/guides/02_배치개발매뉴얼.md) |
| ADM 개발 | [03 ADM 개발자 매뉴얼](cpf-docs/guides/03_ADM개발자매뉴얼.md) |
| ADM 운영 | [04 ADM 운영자 매뉴얼](cpf-docs/guides/04_ADM운영자매뉴얼.md) |
| 플랫폼 운영 | [05 플랫폼 운영 매뉴얼](cpf-docs/guides/05_플랫폼운영매뉴얼.md) |
| BZA | [90 BZA 매뉴얼](cpf-docs/guides/90_BZA매뉴얼.md) |
| Gateway | [91 Gateway 매뉴얼](cpf-docs/guides/91_게이트웨이매뉴얼.md) |

> 기준 문서 SHA는 `1536a0d59004ebade7dcb29383cbe2e758547f8e`입니다. Source 구현과 실행 Evidence가 다르면 실제 최신 Git과 exact-SHA Evidence를 우선합니다.
