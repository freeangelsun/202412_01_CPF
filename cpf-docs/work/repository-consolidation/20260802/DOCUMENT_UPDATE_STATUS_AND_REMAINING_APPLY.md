
# Core 경량화·Starter 문서 반영 상태

## 결론

현재 Overlay는 Architecture 정책과 다음 QA 기준선은 갱신했지만, 제품 정본과 기존 역할별 Guide 전체를 직접 교체한 상태는 아니다.

| 대상 | 이번 Overlay 상태 | 최종 완료 조건 |
|---|---|---|
| `CPF_STARTER_ARCHITECTURE_AND_LIFECYCLE_POLICY.md` | 갱신 완료 | 다음 QA Source와 재대조 |
| `CPF_REPOSITORY_SURFACE_INDEX.md` | Starter Container 반영 완료 | Final Root Gate와 일치 |
| `CPF_FINAL_TARGET_REQUIREMENTS.md` | **직접 수정하지 않음** | Source/BOM/Generator 변경과 동일 Commit에서 Amendment 반영 |
| 기존 `cpf-docs/guides/**` | **직접 수정하지 않음** | 확정 Artifact/Profile 이름과 실제 사용법으로 갱신 |
| 기존 Deliverable | **직접 수정하지 않음** | Release Artifact/BOM/SBOM 결과로 갱신 |
| 다음 QA Request·Requirements | Core 경량화·Starter·Profile/Bundle 요건 반영 완료 | 최신 master 착수 시 재기준화 |
| Starter 사용 Guide 초안 | 갱신 완료 | 실제 구현 후 역할별 정본으로 승격 |

따라서 “관련 문서가 모두 최종 갱신됐다”는 판정은 아직 할 수 없다.

```text
development_status = 부분 구현
verification_status = 미검증
```

## 이번 보완에서 추가한 사항

- 개별 Leaf Starter 선택
- Generator Capability Profile
- 선택적 Aggregate Starter
- Platform BOM의 역할 구분
- Profile을 해석한 최종 Leaf Dependency의 Manifest 기록
- Profile Version·Resolved Lock
- Provider 상호 배타 충돌 Gate
- Mega Starter 금지
- Profile/BOM/POM/Guide Drift Gate
- 다음 QA 개발요건과 Codex 검수 항목

## 다음 QA의 문서 완료 순서

1. 최신 목표 정본과 Requirement ID 확인
2. Starter 분리·세분화 Source와 Artifact 이름 확정
3. Generator Profile과 Manifest Schema 구현
4. BOM·Publication·Packaging 검증
5. 최상위 목표 정본 갱신
6. 개발자·운영자·설치·Generator Guide 갱신
7. Reference/EDU·Deliverable 갱신
8. Source→Guide와 Guide→Source Drift Gate 실행

## Core·Base Starter 추가 상태

- Core 독립 계약 유지: Architecture 기준선 반영
- Base Starter: 다음 QA ADR·구현 요건 반영
- Common 선택 업무공통화: 다음 QA 요건 반영
- 최상위 목표 정본 직접 갱신: 여전히 Amendment 제안 상태
