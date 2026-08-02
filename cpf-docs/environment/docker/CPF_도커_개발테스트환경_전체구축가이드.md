# CPF Docker 개발·시험 환경 전체 구축 가이드

상위 메뉴: [Docker 문서](README.md)

> **주 독자**: 새 개발 PC 또는 새 검증 PC를 준비하는 Docker 환경 운영자
> **완료 결과**: Repository Source를 기준으로 Docker 실행본을 준비하고, 필요한 Image·Container·Secret·Fixture를 생성한 뒤 Running CPF Container 0과 보존 상태를 확인한다.
> **Source 기준**: `freeangelsun/202412_01_CPF`, `master`, `3b600702502e53877e30cbac594987b371e2186b`

## 1. 기준 경로

```powershell
$repo='C:\dev\projects\jck\202412_01_CPF'
$dockerRoot='C:\dev\Docker'
$runtime=Join-Path $dockerRoot 'CPF'
$secretRoot=Join-Path $dockerRoot 'Secrets'
```

명령은 어느 폴더에서 실행해도 되도록 위 변수를 사용한다.

## 2. 사전 확인

```powershell
if(-not(Test-Path -LiteralPath $repo -PathType Container)){throw "Repository가 없습니다: $repo"}
docker version
docker compose version
git -C $repo remote get-url origin
git -C $repo branch --show-current
git -C $repo rev-parse HEAD
git -C $repo rev-parse origin/master
git -C $repo status --short
```

중단 조건:

- Branch·Commit이 검증 기준과 다름
- Docker Engine 또는 Linux Container Backend 사용 불가
- 기존 CPF Container가 다른 작업으로 실행 중
- Port·Runtime Root·Secret Root가 다른 작업과 충돌
- 설치 Script가 전체 Image·Volume·사용자 DB 삭제를 요구

금지:

```text
docker system prune
docker volume prune
Docker Factory Reset
git clean -fd
git reset --hard
```

## 3. Source 존재 확인

```powershell
$source=Join-Path $repo 'cpf-tools\environment\docker-development-test'
$required=@('compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml')
$missing=@($required|Where-Object{-not(Test-Path -LiteralPath (Join-Path $source $_) -PathType Leaf)})
if($missing.Count){throw "필수 Docker Source가 없습니다: $($missing -join ', ')"}
```

설치 Script 이름은 실제 Source에서 확인한다.

```powershell
Get-ChildItem -LiteralPath $source -File -Filter '*.ps1' | Select-Object Name,FullName
```

문서에 적힌 Script가 없으면 임의로 유사 Script를 실행하지 않는다.

## 4. Compose 정적 검증

Repository Source에서 직접 검증하는 예:

```powershell
$source=Join-Path $repo 'cpf-tools\environment\docker-development-test'
$envFile=Join-Path $secretRoot 'cpf-runtime.env'
if(-not(Test-Path -LiteralPath $envFile -PathType Leaf)){throw "환경변수 파일이 없습니다: $envFile"}
docker compose --env-file $envFile `
  -f (Join-Path $source 'compose.yml') `
  -f (Join-Path $source 'compose.redis.yml') `
  -f (Join-Path $source 'compose.kafka.yml') `
  -f (Join-Path $source 'compose.integration.yml') `
  -f (Join-Path $source 'compose.tooling.yml') config --quiet
```

정상 결과:

- Exit Code 0
- 환경변수 누락 없음
- Secret 파일 경로 존재
- Port·Container Name 중복 없음
- `restart: "no"`
- Host 공개 Port가 `127.0.0.1`로 제한됨

## 5. 설치 Script 실행

실제 Source에 전체 설치 Script가 존재할 때만 실행한다.

```powershell
$installer=Join-Path $source 'CPF_도커_개발테스트환경_전체설치.ps1'
if(-not(Test-Path -LiteralPath $installer -PathType Leaf)){throw "설치 Script가 없습니다: $installer"}
Get-Help $installer -Full
pwsh -NoProfile -ExecutionPolicy Bypass -File $installer -DockerRoot $dockerRoot -RepoRoot $repo
$exit=$LASTEXITCODE
if($exit -ne 0){throw "전체 설치 실패: exit=$exit"}
```

Script가 없는 경우 Compose 파일을 임의 복사·수정하지 않고 `DOCKER-TOOLS-001` 개발 요청으로 전달한다.

## 6. 설치 Script의 필수 행동

전체 설치 Script는 다음을 만족해야 한다.

1. Repository Source와 Runtime Root를 구분한다.
2. Secret을 Repository 밖에 생성하거나 기존 Secret을 보존한다.
3. 기존 Container·Volume·Image를 임의 삭제하지 않는다.
4. 필요한 Image를 Pull하고 Digest를 기록한다.
5. Container는 Created 또는 Stopped 상태로 준비한다.
6. Restart Policy를 `no`로 유지한다.
7. 실행 종료 시 Running CPF Container 0을 확인한다.
8. 같은 입력 재실행 시 Secret·Volume·사용자 데이터를 덮어쓰지 않는다.
9. Runtime Source Hash와 Repository Source Hash를 비교할 수 있게 한다.

## 7. 확장 Fixture 설치

WireMock·SFTP·Vault·Keycloak 등의 증분 설치 Script가 실제로 존재할 때 실행한다.

```powershell
$installer=Join-Path $source 'CPF_도커_확장연동환경_증분설치.ps1'
if(Test-Path -LiteralPath $installer -PathType Leaf){
  Get-Help $installer -Full
  pwsh -NoProfile -ExecutionPolicy Bypass -File $installer -DockerRoot $dockerRoot -RepoRoot $repo
  if($LASTEXITCODE -ne 0){throw "확장 설치 실패: exit=$LASTEXITCODE"}
}else{
  Write-Warning "확장 설치 Script가 없어 Compose Source와 기존 Runtime을 수동 비교해야 합니다."
}
```

## 8. 설치 후 상태 확인

```powershell
$envFile=Join-Path $secretRoot 'cpf-runtime.env'
docker compose --env-file $envFile `
  -f (Join-Path $runtime 'compose.yml') `
  -f (Join-Path $runtime 'compose.redis.yml') `
  -f (Join-Path $runtime 'compose.kafka.yml') `
  -f (Join-Path $runtime 'compose.integration.yml') `
  -f (Join-Path $runtime 'compose.tooling.yml') ps -a
```

필수 판정:

```text
필수 Image 존재
Container Created 또는 Exited(0)·Stopped
Restart Policy no
Running CPF Container 0
Secret 원문 미출력
Volume 보존
```

## 9. Runtime Source Hash 비교

```powershell
$names=@('compose.yml','compose.redis.yml','compose.kafka.yml','compose.integration.yml','compose.tooling.yml')
foreach($n in $names){
  $src=Join-Path $source $n
  $run=Join-Path $runtime $n
  [pscustomobject]@{
    File=$n
    SourceExists=Test-Path -LiteralPath $src -PathType Leaf
    RuntimeExists=Test-Path -LiteralPath $run -PathType Leaf
    SourceHash=if(Test-Path -LiteralPath $src){(Get-FileHash -LiteralPath $src -Algorithm SHA256).Hash}else{$null}
    RuntimeHash=if(Test-Path -LiteralPath $run){(Get-FileHash -LiteralPath $run -Algorithm SHA256).Hash}else{$null}
  }
}
```

Hash 차이는 무조건 Runtime 파일을 덮어쓰지 않고 변경 내용을 검토한다.

## 10. 신규 모듈 증분 편입 표준

신규 Module·Starter·Provider가 개발된 뒤 다음 순서로 편입한다.

1. Product Source·Build·실제 Consumer를 확인한다.
2. Image·Version·Digest·License를 결정한다.
3. Compose Service·Network·Port·Volume·Secret을 정의한다.
4. Named Binding과 Product Config를 연결한다.
5. 초기화 Fixture와 Test Data를 만든다.
6. Health·Readiness와 실제 Contract Test를 실행한다.
7. Timeout·Network Loss·Process Kill·부분 실패를 주입한다.
8. Retry·Reprocess·Reconcile·Rollback을 실행한다.
9. ADM·Log·Metric·Trace·Audit를 확인한다.
10. 작업 종료 후 시작한 Container만 중지하고 데이터를 보존한다.

## 11. 검증 기록

```text
Git Commit
Docker·Compose Version
Repository Source Hash
Runtime Source Hash
Image Tag·Digest
Compose Config Hash
Secret File 존재 여부
Container Restart Policy
Fixture Command·Exit Code
Sanitized Log Hash
Running Container Count
미검증 Provider
```

## 12. Rollback

설치 실패 시:

- 이번 실행에서 새로 만든 Container만 정확한 이름으로 제거한다.
- 기존 Volume·Image·Secret은 보존한다.
- Runtime Source는 설치 전 Backup 또는 Hash가 확인된 파일만 되돌린다.
- 사용자 DB와 다른 작업자의 Container를 건드리지 않는다.
- 광역 `down -v`를 기본 Rollback으로 사용하지 않는다.
