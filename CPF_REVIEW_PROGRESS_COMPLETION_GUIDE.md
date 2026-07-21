# CPF_REVIEW_PROGRESS_COMPLETION_GUIDE

## 1. 검수 목적

CPF의 작업자 보고와 실제 제품 상태를 분리하고, 요구사항→구현과 구현→요구사항 양방향 추적을 수행한다.

## 2. 상태값

- 완료
- 부분 구현
- 미구현
- 미검증
- 실패
- 재확인 필요

## 3. 완료 불인정

다음은 단독 완료 근거가 아니다.

- 파일·Class·Package 존재
- 정적 검색
- Swagger 노출
- 일부 테스트
- 자동 생성 Matrix·Inventory
- 과거 Evidence
- Codex/Qwen 성공 보고
- 문서의 완료 표시
- 빈 로그·Mock만의 성공
- 실행하지 않은 Browser/DB/Broker 검증

## 4. 요구사항별 검수

- Source와 계층 연결
- Owner Module·Package
- 실제 Consumer
- API·오류·권한
- SQL·Migration·Install·Rollback
- 정상·오류·경계·부분 실패
- retry·recovery·unknown result
- idempotency·concurrency·multi-instance
- security·audit·masking
- operations
- EDU·OpenAPI·JavaDoc
- Unit·Integration·Runtime·Browser
- 최신 Evidence
- regression

## 5. 작업자 문서와 검수자 판정

작업자는 직접 수행한 사실과 Evidence를 작성할 수 있다. 최종 완료·진행률·Gap 종료는 별도 전수검수 후 확정한다.

## 6. 진행률

최상위 Requirement를 중복 없이 집계한다. 같은 Gap을 다른 이름으로 반복 계산하지 않는다. `재확인 필요`와 `미검증`을 완료율에 포함하지 않는다.

## 7. Evidence 신선도

Evidence는 기준 Commit, 환경, 명령, 시각, 종료 코드, 민감정보 제거 여부가 확인돼야 한다. Source가 바뀌면 관련 Evidence는 stale로 표시한다.

## 8. 중간 Push 대응

작업 중간 Commit이 발생하면:

1. 이전 기준 Commit을 고정
2. 변경 전체를 요구 단위로 분해
3. 문서 완료와 Source 구현을 분리
4. 유효 구현 보존
5. 허위·과장 상태 하향
6. 회귀·삭제·가비지 확인
7. 다음 통합 요청서에 잔여 범위 포함
