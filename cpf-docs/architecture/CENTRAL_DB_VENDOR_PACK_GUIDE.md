# CPF Central DB Vendor Pack Guide

## 정본

Vendor별 Physical SQL은 `cpf-tools/db/vendor/<vendor>` 하나만 제품 정본으로 사용한다. Module `src/main/resources`에 Vendor SQL을 복제하지 않는다.

## Runtime 선택

`cpf.db.vendor`는 논리 Vendor를, `cpf.db.resource-root`는 선택 Vendor Pack의 filesystem root를 지정한다. `pack.json` vendor/schemaVersion/status 검증 후 `runtime/<owner>/mybatis`와 `runtime/<owner>/repository`를 읽는다. 설정 누락/불일치는 fail-fast다.

## Lifecycle

Provision / Install / Product Seed / Optional Sample / Test Seed / Migration / Verify / Rollback을 분리한다. Split DDL이 Schema 설계 정본이며 generated bundle과 central MariaDB mirror는 build script로 재생성한다.

## Generated Domain

DomainName/SystemCode metadata와 Vendor `domain-template`을 조합한다. 신규 PAY/INS 등을 위해 CPF Java source에 switch/if를 추가하거나 Domain마다 5 Vendor SQL을 복제하면 안 된다.

## 지원 표기

MariaDB처럼 실제 설치/runtime을 실행한 Vendor만 검증 완료로 기록한다. Resource/template 존재만으로 Oracle/PostgreSQL/SQL Server 지원 완료라고 쓰지 않는다.

## Fail-fast 전환

과도기 module-local `sql/vendor`/`mybatis/vendor`를 삭제한 뒤 발생하는 실패는 중앙 Pack 연결 누락을 드러내는 Gap이다. 오류 회피를 위해 삭제 resource를 복구하지 않는다. Test도 temp central pack 또는 실제 selected pack을 사용해야 한다.
