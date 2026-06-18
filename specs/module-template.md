# 신규 업무 모듈 템플릿

> 이 문서는 deprecated 보조 문서입니다. 정본은 `specs/개발_가이드.html`, `specs/기능_구현_매트릭스.md`를 참조하세요.

신규 업무 모듈은 `xyz` 교육 모듈과 `acc`/`mbr` 샘플을 기준으로 만듭니다.

## 패키지

```text
cpf.{module}.{domain}.controller
cpf.{module}.{domain}.service
cpf.{module}.{domain}.mapper
cpf.{module}.{domain}.dto
```

## 파일 기준

| 산출물 | 기준 |
| --- | --- |
| Controller | `/api/v1/{module}/{domain}` 신규 API, 기존 호환 API는 별도 표시 |
| Service | 트랜잭션 경계 위치, 권한/감사 사유 검증 |
| Mapper | SQL Injection 방지를 위해 parameter binding만 사용 |
| DTO | Bean Validation, Swagger example |
| SQL | `{module}_{업무명}` 테이블명과 공통 감사 컬럼 |
| Test | validation, service 단위, DB mapper 또는 smoke |

## 참고 샘플

- CRUD/validation: `xyz/src/main/java/cpf/xyz/edu/controller/XyzCrudEducationController.java`
- CMN 업무 공통: `xyz/src/main/java/cpf/xyz/edu/controller/XyzCmnBusinessEducationController.java`
- ACC 외부 연계: `acc/src/main/java/cpf/acc/bse/controller/AccountController.java`
- MBR 기본 CRUD: `mbr/src/main/java/cpf/mbr/bse/controller/MbrController.java`
