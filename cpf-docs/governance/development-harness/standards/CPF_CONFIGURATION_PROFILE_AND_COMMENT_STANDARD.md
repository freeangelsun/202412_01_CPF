# CPF Configuration Profile·설명 주석 표준

## Profile 세트
운영 가능한 Spring Boot Runtime은 공통 `application.yml`과 `local/dev/stg/test/prod` Profile 세트를 가진다. 시스템 특화 prefix를 사용하는 기존 모듈은 동일 5개 환경 의미를 유지한 채 기존 naming convention을 보존한다. 새로운 profile 이름을 임의로 추가해 canonical 환경을 대체하지 않는다.

## 설정 분리
- 공통 default와 구조는 base YAML.
- 환경별 endpoint/timeout/pool/feature toggle/log level/resource sizing 등은 profile YAML.
- Secret/credential/token/private key는 Source에 저장하지 않고 environment/secret provider로 주입한다.
- 동일 키를 Java 상수와 YAML에 중복 소유하지 않는다. `@ConfigurationProperties` 등 typed binding을 우선한다.
- 환경별 차이를 코드의 `if (profile == ...)` 하드코딩으로 구현하지 않는다.

## 한글 주석
사람이 설정하는 scalar/list/map entry는 인접한 한글 주석으로 **용도**를 알 수 있어야 한다. 단위(ms/sec/bytes/개수), 허용범위/enum, 기본·미지정 의미, 운영 주의, 보안 민감성을 필요에 따라 설명한다. 자동 생성/표준 library metadata처럼 사람이 수정하지 않는 산출물은 policy registry에서 제외 근거를 등록한다.

## 검증
Harness는 runtime surface registry에서 대상 Runtime을 발견하고 profile-set completeness, UTF-8, secret pattern, 설명 주석 coverage를 검증한다. 현재 Source가 미달이면 Harness 자체 실패가 아니라 **Product Conformance Finding**을 생성하며 해당 Product Requirement는 완료로 닫을 수 없다.
