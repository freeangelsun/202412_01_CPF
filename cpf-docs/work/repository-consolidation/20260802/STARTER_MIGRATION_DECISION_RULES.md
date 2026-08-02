# Starter 이관·세분화 결정 규칙

## 유지

현재 Starter에 기술 Adapter·AutoConfiguration·실제 Consumer·실패 처리가 있고 Owner 경계가 맞으면 유지한다.

## 세분화

하나의 Starter가 서로 독립적으로 선택되는 Provider 또는 배포 모드를 강제할 때 분리한다.
예: Session JDBC와 Resource Server, Caffeine과 Redis.

## Owner Module로 복귀

기능이 특정 Product 하나의 고유 실행 책임이고 다른 Consumer가 없으면 Starter로 일반화하지 않는다.
예: Batch Lease/Fencing, Gateway Route Ledger, ADM 승인 정책.

## 새 Starter

Core/Common/여러 Product에 중복된 선택 Runtime 구현이 있고 공통 Public Contract와 두 개 이상의 실제 Consumer가 있을 때 검토한다.

## 제거

실제 Product Consumer가 없고 전략적 Reference 필요성도 없으며 Provider·운영·실패 Closure가 없는 Starter는 GA 대상에서 제외한다. 제거 전에 BOM·Generator·Config·Guide 참조 0과 대체 경로를 확인한다.

## 이관 완료 조건

1. Target Starter/Owner Source 구현
2. 모든 Consumer Dependency·Import·Config 이관
3. 정상·오류·부분 실패·다중 인스턴스 검증
4. Optional 제거 Variant PASS
5. 기존 구현·AutoConfiguration·Dependency의 Dual Primary 제거
6. BOM·Publication·SBOM·Generator·Guide·EDU 갱신
7. Fresh Clone과 최종 JAR/WAR 포함/제외 Evidence

파일 이동이나 Dependency 이름 변경만으로 완료 처리하지 않는다.
