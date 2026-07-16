# Account 주제영역 반영 순서

1. settings.gradle.patch 내용을 검토하고 모듈 include를 반영합니다.
2. ${module}/ 모듈과 pplication-acc.yml 설정을 반영합니다.
3. sql/Vxx__acc_domain.sql의 Flyway version을 확정합니다.
4. sql/40_business_modules_schema.acc.candidate.sql을 split SQL과 all_install에 반영합니다.
5. sql/50_framework_seed.acc.candidate.sql의 PFW module registry seed를 반영합니다.
6. sql/60_adm_seed.acc.candidate.sql의 ADM 메뉴/API/버튼 권한 seed를 반영합니다.
7. sql/99_smoke_check.acc.candidate.sql을 smoke check에 반영합니다.
8. smoke-acc.ps1의 포트와 API path를 확인합니다.

후보 파일은 자동 적용하지 않으며 검토 후 정본 SQL과 설정에 반영합니다.