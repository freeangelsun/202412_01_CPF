# CPF QA37 개발 GPT 전달 Prompt

CPF Repository 최신 `origin/master`를 기준으로 QA37 개발을 수행해줘.

먼저 exact SHA, HEAD, Working Tree, Remote를 읽기 전용으로 확인해. 예상 시작 SHA는 `23a16f35a5633ce1317920468a69fef00c1a6a41`지만 최신 SHA가 다르면 최신 기준으로 모든 문서·Matrix·Evidence를 재기준해.

반드시 다음 문서를 순서대로 읽어.

1. `cpf-docs/governance/CPF_FINAL_TARGET_REQUIREMENTS.md`
2. `cpf-docs/work/review/CPF_20260801_QA37_PRE_DEVELOPMENT_REVIEW.md`
3. `cpf-docs/work/current/CPF_20260801_QA37_EDU_SOURCE_CLOSURE_AND_RECOVERY_REQUEST.md`
4. `cpf-docs/quality/CPF_20260801_QA37_REQUIREMENT_MATRIX.csv`
5. `cpf-docs/work/current/CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`
6. QA36·SELF·Canonical·기존 상세 QA/Scenario 정본
7. 실제 Source·SQL·API·Frontend·Test·Config·Script

가장 먼저 Root `build.gradle`이 `cpf-biz-admin/build.gradle`과 동일하게 덮인 결함과, `settings.gradle`이 참조하지만 삭제된 `cpf-tools/build/gradle-plugin`, `cpf-tools/build/platform-bom` Source를 복구해. 이 P0 Build Gate가 통과하기 전에는 대규모 Runtime 검증을 시작하지 마.

기존 EDU 32/32 완료 판정은 승계하지 마. 개발 모드 EDU Gate가 실제 Source/Test/Public Contract 경로를 해석하지 않은 False Closure가 확인됐다. EDU-001~032를 `재확인 필요 / 미검증`에서 시작하여 실제 Source, Class, Method, Consumer, Test, Runtime 명령, 운영 확인 경로를 전수 재판정하고 부족한 Source를 개발해.

`CPF_CUSTOMER_MANUAL_EDU_IMPLEMENTATION_REQUIREMENTS.md`의 135개 원본 ID 전체를 개발 원장에 편입하고 하나의 통합 작업으로 구현해. 온라인 45, Batch 30, ADM 17, BZA 14, Gateway 14, 플랫폼 설치·운영·복구 15의 합계 135를 검증해.

교육 고객 업무는 `cpf-reference`와 허용된 표준 Job Pack에 두고, Product Capability는 정식 Owner Module에 구현해. `cpf-tools`의 Gate·Matrix로 Product Source 개발을 대체하지 마. Oracle·PostgreSQL·MariaDB SQL은 중앙 Vendor Pack만 사용해.

검증 환경이 부족하더라도 가능한 Product Source·EDU Consumer·SQL·Config·Test·Adapter·Test Double은 개발하고, 실행하지 못한 검증만 `verification_status = 미검증`으로 남겨.

작업 전 Docker 문서 5개를 지정 순서로 읽고 `C:\dev\Docker\CPF`, `C:\dev\Docker\Secrets`의 준비된 환경을 사용해. 전체 설치 Script나 Prune·초기화를 실행하지 말고 필요한 Service만 시작해. Toxiproxy, OTel, Trivy, ORT를 용도에 맞게 사용해.

README와 README에서 연결되는 Manual·Guide는 수정하거나 구현 근거로 사용하지 마. 현재 그 문서가 Git에서 변경된 사실은 별도 Stream으로 기록만 하고 개발 Overlay에서 제외해.

사용자 승인 없이 Commit·Push·Branch·Tag·PR·Release·Reset·Restore·Stash·삭제를 수행하지 마.

최종 결과는 Repository Root Overlay ZIP 하나로 제공하고 SHA-256, 파일 수, 기준 SHA, 포함·제외 범위, Delete Manifest 기반 정리 한 줄 명령을 함께 줘. Codex 검수는 이번 개발 완료와 사용자 Commit·Push 이후에 수행한다.
