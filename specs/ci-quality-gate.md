# CI 품질 Gate

CI는 최소한 다음 항목을 통과해야 합니다.

| 단계 | 명령/기준 |
| --- | --- |
| 컴파일 | `.\gradlew.bat :pfw:compileJava :cmn:compileJava :adm:compileJava :acc:compileJava :mbr:compileJava :xyz:compileJava --offline` |
| 테스트 | `.\gradlew.bat :cmn:test --offline` 및 변경 모듈 테스트 |
| SQL 표준 | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-sql-standard.ps1` |
| UTF-8 | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1` |
| Mojibake | `powershell -NoProfile -ExecutionPolicy Bypass -File scripts/check-utf8.ps1 -CheckMojibake` |
| 통합 gate | `.\gradlew.bat qualityGate --offline` |

추가 권장 gate:

- 앱 기동 후 `/v3/api-docs` OpenAPI JSON schema 검증
- dependency 취약점 점검과 SBOM 생성
- MariaDB `00_all_install_and_smoke.sql` 실DB 실행
- prod profile secret 기본값 검사
- VSCode Problems 기준 null/static/encoding 경고 점검
