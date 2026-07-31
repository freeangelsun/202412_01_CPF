# CPF QA34 GPT 최종 개발 인수인계

## 기준

- Base SHA: `c2e1680fcf42467d445df97f1a3a0c36dab783ef`
- 적용 방식: 프로젝트 Root에 Overlay ZIP을 덮어쓴다.
- Commit/Push는 사용자가 수행한다.

## 적용 후 순서

1. `git diff --check`, `git status --short` 확인
2. QA34 Source Gate 6종 실행
3. Java 25/Frontend/3DB/Runtime/Supply-chain 환경 변수를 준비
4. 변경분 Commit·Push
5. 새로운 Fresh Clone에서 `verify-cpf-qa34-independent-review.ps1`을 **한 번만** 실행
6. 외부 Evidence 디렉터리에서 QA33 138/414/552 재판정 결과와 QA34 Final Closure를 확인

## 중요한 Architecture 결정

- Canonical Plugin: `com.cpf.platform-conventions`, publication group `com.cpf.gradle`
- Canonical BOM: `com.cpf:cpf-platform-bom`
- Git 추적 Frontend Marker에 Commit SHA를 기록하지 않는다.
- exact Git SHA는 Commit 후 외부 Release/Independent Evidence에만 기록한다.
- Runtime OpenAPI Release 검증은 tracked 파일을 수정하지 않는다.
- 검증 생성물은 임시 외부 staging에 만들고 성공 후 Evidence로만 보존한다.
- 공식 DB는 Oracle/PostgreSQL/MariaDB만 사용한다.

## 완료 금지 조건

Fresh Clone independent wrapper가 exit 0을 반환하고 QA33 unresolved row가 0이 되기 전에는 QA34 전체 완료를 선언하지 않는다.
