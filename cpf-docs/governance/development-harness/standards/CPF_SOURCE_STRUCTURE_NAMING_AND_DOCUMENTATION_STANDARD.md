# CPF Source 구조·Naming·JavaDoc·UTF-8 표준

## Architecture/Folder
현재 CPF Product Contract의 Owner Map과 Generated Domain IA가 최상위다. Harness 도입은 폴더/패키지/헤더/호출규격을 재정의하지 않는다. 구조 변경은 반드시 Product Contract→Generator/Template→Consumer→Test→Runtime 순으로 함께 변경한다.

## Naming
- Module/folder 이름은 settings.gradle과 canonical owner 역할을 일치시킨다.
- Java package는 현재 Source의 canonical root를 유지하며 lowercase package convention을 사용한다.
- Type은 역할이 드러나는 이름, method는 행위를 드러내는 이름을 사용한다. `Manager/Util/Helper/Common` 같은 광범위 이름으로 Owner 의미를 숨기지 않는다.
- DTO/Request/Response/Event/Command/Result/Properties/AutoConfiguration/Provider 등 역할 suffix는 프로젝트 기존 규칙과 일치시킨다.
- 파일명과 public top-level type 불일치 금지.
- Generated 영역과 user-owned 영역 경계를 이름/경로로 식별 가능하게 유지한다.

## JavaDoc/한국어 주석
Public API/SPI/Annotation/ConfigurationProperties와 외부 Consumer가 사용하는 공개 type/member는 JavaDoc 필수다. 의미 있는 `@param`, `@return`, `@throws`, thread-safety, transaction, retry/idempotency, security, side effect를 기술한다. 중요 Runtime의 복구·동시성·보안·상태전이 의도는 한국어 주석으로 남긴다. 구현 코드의 자명한 문장을 번역한 주석은 금지한다.

## UTF-8
텍스트 Source는 UTF-8, 파일명은 NFC를 표준으로 한다. invalid byte, mojibake, NUL/Backspace 등 비정상 제어문자, NFC collision을 Fail-Closed 한다.
