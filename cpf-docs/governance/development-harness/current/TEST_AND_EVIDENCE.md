# CPF TEST AND EVIDENCE — Current

## Source Identity

- Input Working Tree ZIP: `CPF_FULL_SOURCE_FOR_NEXT_QA_20260829_224746.zip`
- Input ZIP SHA-256: `b1daa68a3508cd5dddec90cae25f8aeaaca636bae5703b83a322974c7f5938dc`
- Current Product Source Identity: `bb67ca18fcaceb9ddfda5c082f2f38e8deb76b5a58f30afc6c4ad04c31400d74` / 8,448 files

## DevGPT Fresh 실행 결과

- Development Harness Final Gate: **PASS**, failed=0
- Negative Mutation / False Green regression: **27/27 PASS**
- CPF tools full pytest: **973 PASS / 37 SKIP / 0 FAIL / 15 subtests PASS**
- Final affected regression: **4/4 PASS**
- NXT3 Layout: **87/87 PASS**
- NXT3 Garbage/Hygiene: **PASS**, Current Delete Manifest 246, protected delete 0
- Migration Semantic Closure: **PASS**, 265 rows / delete eligible 246 / protected retain 19
- Product Conformance: **PASS**, findings 0

## 미완료 Physical Acceptance

현재 컨테이너는 Java 21.0.11이며 `pwsh`/Docker가 없고 frontend dependency 설치도 완료할 수 없다. 따라서 Java25 Root Build/Test/Publication, Fresh VS Code Error=0 Warning=0, DB3 Physical, Batch process runtime, One-WAS, Browser E2E/a11y, Performance live/load/soak, Actual Open Git Fresh Release, Same Source Fresh Replay, Independent Reviewer/QA를 PASS로 기록하지 않는다.

Actual Open Git 명령은 현재 JVM에서 직접 실행했으며 Java25 class version 69 CLI를 Java21이 실행하지 못해 `UnsupportedClassVersionError`로 종료됐다. 이는 `BLOCKED_EXTERNAL`이며 Source PASS로 승격하지 않는다.

상세 Evidence는 `cpf-docs/governance/development-harness/evidence/devgpt/current/executions/`를 따른다.
