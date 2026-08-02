# CPF 문서 통제·보존 정책

- 기준 Repository: `freeangelsun/202412_01_CPF`
- 분석 기준 Branch/SHA: `master` / `1eda8e12fe123281748a4388938c62f11819da1e`
- 목적: 문서 수를 늘리지 않고 정본·현재 작업·검증·이력을 분리한다.

## 1. 문서 계층

| 계층 | 역할 | 유지 원칙 |
|---|---|---|
| Canonical | 최상위 목표, Architecture, Specification, 정책 | 날짜 없는 정본 파일 1개를 유지 |
| Current | 현재 작업 요청과 현재 상태 | 통합 Primary 1개와 현재 작업에 필요한 보조 문서만 유지 |
| Review | 작업 전·후 독립 검수 | 활성 작업 단위 1세트만 Current로 취급 |
| Quality | Requirement·Gap·Defect·Result Matrix | 같은 역할의 Matrix를 회차별로 복제하지 않음 |
| Evidence | exact SHA 실행 근거 | Commit·환경·명령별 보존, 정본과 분리 |
| History | 완료 회차 요약 | 회차별 한 줄 Ledger로 압축, 상세는 Git History로 추적 |
| Codex Package | 검수 시작점·Manifest·Evidence Index | 활성 회차 폴더만 Current 취급 |

## 2. 정본 규칙

1. 제품 목표 정본은 `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`다.
2. 현재 작업 Primary는 `cpf-docs/work/current/CPF_CURRENT_WORK_REQUEST.md`다.
3. 활성 Codex QA37 경로는 동시 작업 보호 대상으로 유지한다.
4. 완료된 요구사항은 Current 문서에 반복 설명하지 않고 Requirement ID·상태·최종 Evidence·회귀 Gate만 한 줄로 남긴다.
5. 과거 Prompt·Review Request·Package Index·Completion Report를 `work/current`에 누적하지 않는다.
6. 상세 이력은 Git History로 보존하고 Repository에는 회차별 History Ledger만 유지한다.
7. README·고객 매뉴얼·산출물은 QA 작업일지나 완료 보고 저장소로 사용하지 않는다.

## 3. 날짜 문서 허용 기준

날짜가 붙은 문서는 다음에만 허용한다.

- 변경 불가능한 Evidence
- 특정 Release/Commit Manifest
- 외부 제출용 Delivery Package
- 감사상 원문 보존이 필요한 승인 기록

작업 Prompt, 중간 보고, 다음 세션 요청, Review Ready, Package Index는 활성 회차 종료 후 History Ledger로 압축하고 삭제 후보로 전환한다.

## 4. 완료 항목 축약 형식

```text
<Requirement ID> | 완료 | Owner | 최종 검증 SHA | Evidence 경로 | 회귀 Gate
```

구현 설명을 여러 문서에 복제하지 않는다. Source·API·SQL·Test·Evidence 상세는 링크로 연결한다.

## 5. 삭제 안전 규칙

- 삭제는 승인된 `DELETE_MANIFEST.txt`의 Root-relative 파일만 대상으로 한다.
- Wildcard, `git clean`, `reset`, `restore`, `stash`를 사용하지 않는다.
- 후보 파일에 Working Tree 변경이 있으면 전체 삭제를 중단한다.
- 활성 Codex, 고객 매뉴얼, 산출물, Product Source, Evidence는 삭제 대상에 넣지 않는다.
- 삭제 후 Broken Link, Current Primary, Matrix/Evidence 참조를 재검증한다.

## 6. 회차 종료 체크

1. Current Primary 갱신
2. Requirement와 Result Matrix 병합
3. Review·Handover·Codex Package 최신화
4. 완료 회차 한 줄 History 기록
5. Stale 문서 Delete Manifest 작성
6. 사용자 승인 후 명시 경로만 삭제
7. Fresh Clone 문서 Link·Path Gate 실행
