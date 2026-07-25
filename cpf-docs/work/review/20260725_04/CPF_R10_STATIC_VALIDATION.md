# CPF R10 Static Validation

기준: master `7dcccafe4445c10a148a7f45473de25c396aebd3` + R10 overlay

## 직접 수행한 정적 검증

- Core Public API(page/util/transaction) 28 Java source: `javac` PASS.
- CMN Calendar dependency-free core source: `javac` PASS.
- BAT Job log path: `ServerInstanceIdentity` contract stub를 이용한 syntax/type-shape `javac` PASS.
- 전체 R10 Java overlay: 외부 Spring/JUnit/프로젝트 전체 classpath 부재로 전체 compile은 실패했으나 parser-level syntax diagnostic 0.
- ADM/BZA overlay TypeScript/Vue script parser: 5 files, parse error 0.
- PowerShell APPLY + R10 scripts 11 files: quote/here-string/bracket structural heuristic error 0. 실제 `pwsh` parser는 현재 실행환경에 없어 미수행.
- 중요 JavaDoc/OpenAPI heuristic: 51 Java files, violation 0 after correction.
- secret/credential heuristic: hit 0.
- MariaDB R10 canonical: `20_cmn_schema.sql` calendar CREATE 1, `35_bat_schema.sql` BAT legacy calendar 0, canonical source 중복 CREATE 0(각 파일 기준).
- Generator: custom `${FeatureClassPrefix}Slice.java` 생성 제거, `CpfPageRequest/CpfSlice` 사용 확인.
- Project instruction: 6,647자, 7,800자 제한 충족.

## 정적검증으로 완료 처리하지 않은 항목

PowerShell APPLY, Gradle, npm, MariaDB, Flyway, Browser, Runtime, Multi-instance, EXS/Generated Domain lifecycle은 이 환경에서 직접 실행하지 않았다. 따라서 결과는 `미검증`이며 최종 통합 검증에서 수행한다.
