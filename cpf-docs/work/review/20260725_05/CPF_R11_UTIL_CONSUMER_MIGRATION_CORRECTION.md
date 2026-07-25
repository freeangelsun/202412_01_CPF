# CPF R11 Utility Consumer Migration Correction

- 기준 SHA: `b6db56f5ee745558a59ce511ad681216004b9672`
- 원인: 최초 R11 overlay가 `cpf-common.utils` 전체 Consumer 이관을 완료하지 못했고 cleanup safety gate가 이를 차단함
- 판정: 최초 "Consumer 이관 완료" 보고는 정정함

## 보정 범위

ADM/BZA/CMN/MBR/REF의 잔존 Consumer 31개 Source를 `cpf-core.api.util` 또는 공개 Security API로 이관했습니다. 기존 R11 overlay에서 이미 이관된 `AdmLogQueryService`는 중복 수정하지 않습니다.

`ValidationUtils`는 `HeaderDTO`에 종속되어 Core 범용 utility로 승격하지 않고, CMN 소유 `HeaderValidator`에 검증 책임을 귀속했습니다.

## 정적 확인

- correction overlay legacy util import/call: 0
- Public utility javac: PASS
- Public masking javac: PASS
- compatibility smoke (`defaultIfBlank`, `normalizeCode`, `uuid32`, date/time format): PASS

전체 Gradle/Runtime/DB/Browser 검증은 사용자의 통합 검증 단계에서 수행하며 이 문서에서 성공으로 간주하지 않습니다.
