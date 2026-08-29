# Source Currentization Standard

산출물은 과거 Evidence나 이전 세션의 Source 설명을 자동 승계하지 않는다. 매 작성 주기마다 `preAuthoring`과 `finalValidation` 두 시점에 현재 제품 Source를 다시 Fingerprint한다.

- 최상위 `CPF_FINAL_TARGET_REQUIREMENTS.md`, Root Gradle, Starter Catalog를 항상 포함한다.
- Public API/Annotation, Starter/Profile/Config, DB3, Generator, Batch/Gateway, OpenAPI/Frontend, 실제 Consumer, Test/Sample/EDU를 Source Inventory에서 재확인한다.
- 두 Fingerprint가 다르면 영향 문서를 재현행화하고 기존 Render/Evidence를 폐기한다.
- Deprecated Public Contract를 현행 API처럼 문서화하면 FAIL한다.

## Documentation self-exclusion

- Root `README.md` and generated documentation artifacts are documentation outputs, not development-source inputs.
- Source fingerprinting MUST exclude those outputs so documentation edits cannot create a false source-drift failure.
- Product Source, SQL, Config, Generator, OpenAPI, Frontend, Test, Sample/EDU and canonical governance inputs remain fingerprinted.
