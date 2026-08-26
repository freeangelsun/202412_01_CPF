# QA REWORK REQUEST

개발GPT Source 보정은 완료했으나 필수 physical runtime이 sandbox 환경 제약으로 미검증이다. QA 최종 검수 전에 사용자 로컬 Java25/Docker/PowerShell/VSCode 환경에서 `run-cpf-required-full-runtime-validation.ps1` 전체 재실행이 필요하다. 기대 기준은 FAIL=0, mandatory SKIP_ENV=0, mandatory NOT_EXECUTED=0, unresolved UNKNOWN=0, Source/Managed identity drift=0이다.
