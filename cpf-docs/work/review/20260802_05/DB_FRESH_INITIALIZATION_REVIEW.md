# Fresh DB·Generator-first 검토

## 현재 판정

QA37에서 Canonical 192 Table과 Vendor artifact static pipeline이 PASS했다는 기록은 있다.
실제 Oracle/PostgreSQL/MariaDB 설치·Upgrade·Rollback·Reapply는 미검증이다.
공식 lifecycle tool이 optional migration/rollback, expected schema overlay, REF runtime query, different-hash conflict를 완전히 연결하지 못한 결함을 보완하던 중 중단됐다.

## 영구 절차

1. Canonical schema/metadata/runtime query contract를 수정한다.
2. Generator와 Golden Template을 동기화한다.
3. 3 Vendor source/lifecycle/runtime pack을 생성한다.
4. Static checksum/drift/parity를 통과한다.
5. Vendor 하나만 시작한다.
6. 전용 QA DB/Schema의 CPF Object count가 0인지 증명한다.
7. Fresh install과 mandatory metadata/seed를 수행한다.
8. arbitrary generated domain을 bootstrap한다.
9. runtime query, upgrade, rollback, reapply, different-hash conflict를 검증한다.
10. cleanup과 object count를 기록하고 이번 작업에서 시작한 service만 중지한다.

기존 사용자 DB를 reset/drop하지 않는다. 공식 초기화 경로가 없으면 그것을 먼저 구현한다.
