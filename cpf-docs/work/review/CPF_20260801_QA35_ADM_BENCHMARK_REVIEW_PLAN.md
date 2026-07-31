# CPF QA35 ADM Batch·Online Benchmark Review Plan

## 기준

- Repository: `https://github.com/freeangelsun/202412_01_CPF`
- Branch: `master`
- Review SHA: `e1f8bef7b7193522f2cd8e36cc6857dd1ff6694a`
- 사용자 제공 최소 기준: `메뉴캡쳐이미지.zip`, Batch 15장 + Online/Common/System/Analysis 29장, 총 44장

## 목적

첨부 화면을 복제 대상으로 사용하지 않는다. 화면에서 확인되는 상용 운영 Capability를 최소 기준으로 추출하고, CPF의 실제 메뉴·Route·Component·API Consumer·권한·감사·오류처리·Runtime 증적과 대조한다. CPF가 더 발전한 기능은 보호하며, 메뉴명이나 Directory 존재만으로 완료 처리하지 않는다.

## 검토 범위

1. ADM 정보구조와 메뉴 정본
2. Batch Job·Schedule·Execution·Agent·Worker·HA·Recovery·Deployment·Audit
3. Online Definition·Deployment·Monitoring·Transaction/Error/Message Log·Analysis
4. System/Common 운영 기능
5. Batch-Online-Gateway-Incident-Audit 통합 추적
6. 상용 UI 품질과 메뉴-소스-API-Evidence 추적

## 판정 기준

- `완료`: 실제 전용 Consumer와 검증 가능한 운영 Workflow가 확인됨
- `부분 구현`: Source 또는 화면은 있으나 운영 Workflow/권한/상세/Runtime 증적이 불완전
- `미구현`: 최소 Capability에 대응하는 실제 Route/Consumer가 확인되지 않음
- `재확인 필요`: Route/Source는 있으나 이번 Source Review만으로 Backend/Runtime 완결을 증명할 수 없음
