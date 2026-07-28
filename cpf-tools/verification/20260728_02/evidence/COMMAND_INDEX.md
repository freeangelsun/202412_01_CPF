# CPF 20260728_02 Evidence Command Index

- 기준 Commit: `ecaddd581a88ede22b63116effd61313744b3fbe`
- 실행 시각: `2026-07-28T21:19:43+09:00`
- Profile: 독립 compile/static overlay validation
- 환경: OpenJDK 21.0.10, Node 22.16.0, TypeScript 5.8.3

## 실행 명령 범주

1. `python3 verify-final-overlay.py --root <overlay> --report STATIC_VALIDATION_RESULT.json`
2. Runtime API source + smoke harness `javac`, `java`
3. Gateway transport source + Spring HTTP 최소 stub + smoke harness `javac`, `java`
4. Gateway API Client policy + Public Principal source `javac`, `java`
5. Batch Runtime Policy/Applier + Runtime API source + smoke harness `javac`, `java`
6. External Institution applier + registry source/stub `javac`, `java`
7. ADM extracted TypeScript compile harness `tsc -p tsconfig.json --noEmit`

실제 Java 25/Gradle, DB, Browser, 다중 인스턴스 검증은 수행하지 않았으며 성공으로 기록하지 않는다.
