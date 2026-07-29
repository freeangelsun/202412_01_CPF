# CPF 20260729_04 Scenario 200 체크포인트 Overlay 적용 가이드

## 기준

- 적용 대상 Root: `C:\dev\projects\jck\202412_01_CPF`
- 기준 SHA: `b8941577b99535ff3e64a4fad99b74bafa544227`
- ZIP은 프로젝트 Root 상대경로 구조다.
- 적용 전 사용자 작업을 별도 보존하고 `git status --short`를 확인한다.

## 한 줄 적용 예시

```powershell
$z="$HOME\Downloads\CPF_20260729_04_SCENARIO_200_CHECKPOINT_ROOT_OVERLAY.zip"; $r="C:\dev\projects\jck\202412_01_CPF"; Expand-Archive -Path $z -DestinationPath $r -Force; python "$r\cpf-tools\verification\20260729_04\check_checkpoint_overlay.py" $r
```

## 적용 후 확인

```powershell
git status --short
```

```powershell
.\gradlew.bat clean test assemble qualityGate --no-daemon --no-build-cache
```

구조 검증 성공은 최종 Runtime PASS가 아니다. Java25/DB/Browser/Redis/Multi-instance/Generator 실행 결과는 별도 Evidence로 남긴다.
