# CPF Repository / Source / Release Governance

- master 직접 Push 금지, 보호 Branch와 Review 사용
- Core/Batch/Security/Deployment 변경은 Code Owner Review 대상
- Source Merge 권한과 Artifact Promotion/Production Deployment 권한 분리
- 위험 운영조치 Requestor와 Approver 분리
- Machine account 최소 scope + Rotation + 감사
- Artifact에 Git SHA, checksum, SBOM, provenance, signature 연결
- Domain Repository별 접근권한 독립 부여 가능
- Deployment Manifest에는 Secret Reference만 저장
- Break-glass는 TTL, 사유, 사후 Review/Audit 필수

실제 GitHub Branch Protection/Team 설정은 Repository 관리자 실행 Evidence가 있어야 PASS로 판정한다.
