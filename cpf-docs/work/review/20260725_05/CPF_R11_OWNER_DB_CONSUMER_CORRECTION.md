# CPF R11 Owner DB Consumer / Repository Root Hygiene Correction

## 원인

R11 Owner DB 경계 보정에서 `AdmJdbcConfig`의 Owner datasource bean은 제거했지만, `AdmHealthController`, `AdmDownloadService`, `AdmObservabilityService`가 해당 JdbcTemplate bean을 계속 소비하고 있었다. 또한 이전 ChatGPT patch ZIP의 적용 안내와 manifest가 Repository Root에 잘못 배치되었다.

## 보정

- ADM Health의 MBR 상태 확인은 `CpfOwnerAdminOperationsPort`의 `system/health` 조회로 이관한다.
- ADM Batch CSV 및 Observability의 Batch 실행 조회는 `CpfBatchOperationsPort`를 사용한다.
- BAT Owner는 실행 조회의 from/to date 조건을 자신의 `bat_execution.created_at`에 적용한다.
- BAT Remote Adapter는 public ServiceCall API를 사용한다.
- 거래 Context 누락 count와 masking/truncate는 public API facade로 노출한다.
- 과거 잘못 Root에 배치된 `APPLY_*.md`, `MANIFEST*`, `PATCH_MANIFEST.txt`만 정확한 이름으로 cleanup한다. 정식 Root 문서는 삭제하지 않는다.
- Common capability gate가 Root hygiene 및 ADM owner consumer 직접 DB 회귀를 검출한다.

## 완료 판정

소스/정적 Gate 보정은 완료. Full Gradle/Spring/DB/Remote E2E는 적용 장비 통합검증 전까지 미검증이다.
