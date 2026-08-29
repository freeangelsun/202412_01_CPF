# CPF 완성품 품질·사용자 관점 Acceptance 표준

CPF는 코드 라이브러리가 아니라 업무 시스템 구축·운영·감사·확장·검증·배포·상용화를 지원하는 Business Platform Framework다. 따라서 완료는 기능 구현 외 다음 품질을 동시에 만족해야 한다.

- **기능 완결성**: 실제 Consumer와 end-to-end task가 존재하고 정상/오류/경계/부분 실패를 처리한다.
- **사용성/DX**: 개발자가 최소 설정으로 발견·선택·실행·검증할 수 있고 오류 메시지가 원인/조치/연관 ID를 제공한다.
- **운영성**: health/readiness, log/metric/trace, transaction/instance/system correlation, 재시도/복구/reconcile, 안전한 운영 제어와 audit.
- **보안/개인정보**: 최소권한, trust boundary, permission/data scope, approval/reason, masking/secret, secure defaults.
- **성능/안정성**: timeout/backpressure/pool/resource limit, load/soak, multi-instance, crash/restart, no leak.
- **호환/확장**: Public API/SPI/Internal 경계, version compatibility, provider 선택, topology parity.
- **Data**: DB3, migration/seed/upgrade/rollback, data ownership, query/index/constraint, backup/recovery 영향.
- **Frontend**: OpenAPI generated client 실제 사용, 검색/paging/detail/status/error/permission/a11y/responsive, 주요 HTTP error states.
- **설치/배포**: Windows/Linux 명령, profile set, bootstrap, artifact/BOM/publication, SBOM/provenance, rollback.
- **문서/교육**: Public API/Config/명령/예제/Sample/EDU가 Source와 일치하고 사용자의 task를 끝까지 안내한다.

완성품 관점 Gap은 기능 자체가 동작하더라도 Requirement/Finding으로 등록한다.
