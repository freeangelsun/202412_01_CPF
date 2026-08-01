# CPF QA37 Codex 크레딧 최적화 사전 리뷰

## 기준

- 원격 `master`: `eaab5108575b76a492703b00f3e050e8dc25cffb`
- 선행 복구: `cpf-tools/build/**` 정식 Source 6개 복구 완료
- 검수 수준: 전수 검수 유지
- 목표: 범위를 줄이지 않고 반복 분석·반복 실행만 제거

## 확인된 비용 낭비 원인

- 긴 요청서와 여러 보조 문서를 세션마다 다시 읽음
- Git·Docker·Tool 상태를 개별 명령으로 반복 수집
- PASS Stage도 세션 재시작 후 다시 실행
- 실패 원인 수정 전에 같은 Build를 반복
- Source 안정화 전 Browser·Supply-chain 실행
- 실행 로그와 Evidence를 매번 수작업 정리

## 개선 방안

1. 단일 Preflight Script로 상태 수집
2. 단일 Stage Wrapper로 로그·Exit Code·Hash·원장 자동 기록
3. PASS Stage 자동 Skip
4. FAIL Stage의 무근거 반복 실행 차단
5. 한 장짜리 Start 문서
6. 세션 재개 정책
7. Git 추적 파일 보호형 가비지 정리 Script

## 완료 조건

- 전수 검수 범위 축소 없음
- 첫 읽기 문서 2개
- Preflight 명령 1개
- Stage 결과 외부 원장 자동 기록
- 추적 Source 삭제 방지
- Git·Docker 파괴적 작업 0건
