# Open Git Stage 06 Generator Distribution — 입력 계약 확정 및 재실행 조건 (WP-R02.07 병합)

## 1. 판정

Stage 06 실패는 Stage 조건 결함이 아니다. `--generator-artifacts` 라는 정규 주입 경로가 Source 에
명시되어 있고, cross-OS artifact 를 fabricate/substitute 하지 않는 것은 의도된 fail-closed 설계다.

> `publish-cpf-public-repository.py:148-155`
> *A private build agent can build its native classifier. Cross-OS artifacts are intentionally
> accepted only from an explicit prebuilt directory (for example, a CI matrix aggregation
> directory); the release driver never fabricates or silently substitutes a classifier.*

정확한 판정 문구는 다음과 같다.

**현재 Windows native 실행환경만으로는 `linux-x64` classifier 를 생성할 수 없다.**

`publish-cpf-public-repository.py:20`

```python
REQUIRED_GENERATOR_CLASSIFIERS = ("windows-x64", "linux-x64")
```

## 2. PyInstaller 의 정확한 역할

`publish_generator_distributions` 는 host classifier 가 prebuilt directory 에 이미 존재하면
native build 를 수행하지 않는다 (`publish-cpf-public-repository.py:166-177`).

```python
if host_classifier in REQUIRED_GENERATOR_CLASSIFIERS:
    host_prebuilt_complete=False
    if workspace is not None:
        try:
            _verify_generator_distribution(workspace,version,host_classifier)
            host_prebuilt_complete=True
        except PublishError:
            host_prebuilt_complete=False
    archive,checksum,manifest=_generator_distribution_files(native_build,version,host_classifier)
    if not host_prebuilt_complete and not (archive.is_file() and checksum.is_file() and manifest.is_file()):
        builder=root/'cpf-tools/generator/distribution/build-cpf-generator-binary.py'
        run([sys.executable,str(builder),...])
```

계약 정본 테스트도 이를 명시한다
(`cpf-tools/release/public/tests/test_publish_cpf_public_repository.py:116`).

```python
# Prebuilt matrix is complete, so no PyInstaller/native build is needed.
```

따라서 **PyInstaller 는 `windows-x64` 를 로컬에서 생성할 때만 필요한 prerequisite 이며,
Stage 06 전체의 해결책이 아니다.** 두 classifier 를 모두 prebuilt 로 주입하면 PyInstaller 는
전혀 필요하지 않다.

## 3. `--generator-artifacts` 입력 계약 (Source/Test 확정)

`--generator-artifacts <DIR>` 은 **flat directory** 이며 하위 디렉터리 구조를 갖지 않는다.
각 classifier 마다 정확히 3개 파일이 있어야 한다
(`_generator_distribution_files`, `_verify_generator_distribution`).

| 파일 | 형식 |
| --- | --- |
| `cpf-generator-cli-<version>-<classifier>.zip` | ZIP_DEFLATED, 내부에 실행파일 1개 (`cpf-generator.exe` / `cpf-generator`) |
| `cpf-generator-cli-<version>-<classifier>.zip.sha256` | ASCII, `<hex digest>` + newline |
| `cpf-generator-cli-<version>-<classifier>.json` | UTF-8 manifest |

manifest 필수 필드

```json
{
  "schemaVersion": 1,
  "artifactId": "cpf-generator-cli",
  "version": "<version>",
  "classifier": "<classifier>",
  "archive": "cpf-generator-cli-<version>-<classifier>.zip",
  "sha256": "<zip 의 sha256>",
  "canonicalEngine": "cpf-tools/generator/engine/cpf_domain_generator.py"
}
```

fail-closed 검증 규칙 (`_verify_generator_distribution`)

1. 3개 파일 모두 존재해야 한다.
2. `sha256(zip)` 과 `.zip.sha256` 의 첫 토큰(소문자)이 일치해야 한다.
3. manifest 의 `artifactId` / `version` / `classifier` 가 좌표와 정확히 일치해야 한다.
4. manifest 의 `sha256` 이 실제 zip digest 와 일치해야 한다.

`version` 은 `gradle/cpf-platform.properties` 의 `platformVersion` 이며 현재 값은
**`1.0.0-SNAPSHOT`** 이다 (`cpf_open_git.py:814-820`).

provenance 는 `canonicalEngine` 필드로 표기되지만 verifier 가 이 값을 검증하지는 않는다.
Source Identity 와의 직접 연계 필드는 계약에 존재하지 않는다.

보조 경로로 `CPF_GENERATOR_NATIVE_BUILD_DIR` 환경변수가 host native build 출력 위치를 지정한다
(기본값 `<binary-repository-raw>/../generator-native`).

## 4. linux-x64 native build 경로 판정

`build-cpf-generator-binary.py` 의 `classifier()` 는 `platform.system()` 과 `platform.machine()`
만 검사하며, container/VM/WSL 을 배제하는 조건은 Source 어디에도 없다.

| 후보 | 이 환경에서의 가용성 | 계약 만족 여부 |
| --- | --- | --- |
| Linux CI matrix | 미구성 | 만족 (정식 경로) |
| 별도 Linux host | 없음 | 만족 |
| WSL Linux 배포판 | **불가** — `wsl -l -v` 결과 `docker-desktop` 배포판만 존재하고 상태는 Stopped | 배포판 설치 시 만족 |
| Linux container | **가능** — Docker version 29.7.2 설치됨 | 만족. `platform.system()` 이 `Linux`, `platform.machine()` 이 `x86_64` 를 반환하고 PyInstaller 가 실제 ELF 를 생성하며 artifact 3종을 동일 스크립트가 만든다 |

단, container 로 만든 ELF 는 base image 의 glibc 버전에 종속된다. Product Contract 에 base image
규정은 발견되지 않았으므로 배포 대상 호환성은 release 운영 판단 사항으로 남긴다.

## 5. 현재 상태

| 항목 | 상태 |
| --- | --- |
| Gradle 산출 단계 (Stage 01-05) | PASS — BUILD SUCCESSFUL in 7m 10s / 864 actionable tasks |
| Stage 06 Generator Distribution | FAIL |
| `windows-x64` | 미생성 — PyInstaller prerequisite 없음 |
| `linux-x64` | 미생성 — Windows native 실행환경에서 생성 불가 |
| Public Leakage | 0 |
| commitExecuted / pushExecuted | false / false |
| Open Git WP 상태 | **BLOCKED_EXTERNAL** (14/14 전까지 CLOSED 금지) |

Stage 조건 완화, classifier 대체, Source Contract 약화는 수행하지 않았다.

## 6. 재실행 명령 (Source 확인 기반)

`<MATRIX_DIR>` 은 Repository 밖의 aggregate directory 로 둔다 (Repository garbage 방지).

### A. windows-x64 artifact 생성 (Windows PowerShell)

```powershell
python -m pip install pyinstaller
python cpf-tools\generator\distribution\build-cpf-generator-binary.py `
  --root . --output <MATRIX_DIR> --version 1.0.0-SNAPSHOT
```

### B. linux-x64 artifact 생성 (Linux container)

```powershell
docker run --rm `
  -v "${PWD}:/src:ro" -v "<MATRIX_DIR>:/out" -w /src `
  python:3.13-slim `
  sh -c "pip install --no-cache-dir pyinstaller && python cpf-tools/generator/distribution/build-cpf-generator-binary.py --root /src --output /out --version 1.0.0-SNAPSHOT"
```

### C. Fresh Open Git Release 14/14

```powershell
$ErrorActionPreference='Stop'
$env:CPF_OPEN_GIT_REMOTE='https://github.com/cpf-team/cpf-framework.git'
.\cpf-tools\runtime\cli\cpf.ps1 release open-git build --profile binary --generator-artifacts <MATRIX_DIR>
```

`CpfCli.internalRelease` 가 잔여 인자를 `cpf_open_git.py` 로 그대로 전달하므로
`--generator-artifacts` 는 CLI 경유로 유효하다 (`CpfCli.java:258-264`, `cpf_open_git.py:1244`).

Stage 01 이 이전 `cpf-release/` 를 전량 재생성하므로 이전 실패 산출물 재사용은 발생하지 않는다.

## 7. Source Identity

| 항목 | 값 |
| --- | --- |
| productContentSha256 | `7a676246928631b014b8356149a3ec3f077e8b975458641df209ba66de14605b` |
| productContentSha1 | `65b2ec18fb73118f5c95f077ed5b0fe6fad0c3f6` |
| fileCount / totalBytes | 8453 / 46145070 |

## 8. 12개 확정 항목 (Source/Test 근거, 추측 없음)

| # | 항목 | 확정 내용 | 근거 |
| --- | --- | --- | --- |
| 1 | directory 구조 | flat directory 1개. 하위 디렉터리·classifier별 서브폴더 없음 | `_generator_distribution_files` 가 `directory/(stem+ext)` 로만 조회 |
| 2 | 파일명 | `cpf-generator-cli-1.0.0-SNAPSHOT-windows-x64.{zip,zip.sha256,json}`, `cpf-generator-cli-1.0.0-SNAPSHOT-linux-x64.{zip,zip.sha256,json}` | `stem=f'cpf-generator-cli-{version}-{classifier}'` |
| 3 | checksum / manifest | `.zip.sha256` 는 ASCII hex digest + newline. manifest 는 `schemaVersion/artifactId/version/classifier/archive/sha256/canonicalEngine` | `build-cpf-generator-binary.py` 생성부, `_fake_generator_distribution` 계약 테스트 |
| 4 | provenance 검증 방식 | 파일 존재 + zip sha256 일치 + manifest 좌표 일치 + manifest sha256 일치. **서명·GPG·발급자 검증은 없음** | `_verify_generator_distribution`, Stage 07/09 의 `_verify_generator_distributions` |
| 5 | Source Identity 연계 | **없음.** manifest 의 `canonicalEngine` 은 경로 문자열이며 verifier 가 검사하지 않고, release identity 와의 바인딩 필드도 없음 | `_verify_generator_distribution` 이 `canonicalEngine`/`schemaVersion` 미검사 |
| 6 | `host_prebuilt_complete` 판정 | prebuilt directory 에 대해 `_verify_generator_distribution(workspace, version, host_classifier)` 가 예외 없이 통과하면 `True`. `True` 이면 PyInstaller native build 를 호출하지 않음 | `publish-cpf-public-repository.py:167-177` |
| 7 | complete matrix 판정 | `REQUIRED_GENERATOR_CLASSIFIERS` 의 모든 classifier 가 `sources`(= prebuilt directory → native build directory 순) 중 한 곳에서 검증 통과 | `publish-cpf-public-repository.py:178-199` |
| 8 | validation 실패 조건 | 3개 파일 중 하나라도 없음 / zip digest 불일치 / manifest 의 `artifactId`·`version`·`classifier` 불일치 / manifest `sha256` 불일치. 실패 시 해당 directory 는 건너뛰고, 모든 source 에서 실패하면 `PublishError` | 동일 함수 |
| 9 | windows-x64 생성 공식 명령 | `python cpf-tools\generator\distribution\build-cpf-generator-binary.py --root . --output <MATRIX_DIR> --version 1.0.0-SNAPSHOT` (PyInstaller 필요) | `build-cpf-generator-binary.py` `main()` 인자 |
| 10 | linux-x64 생성 공식 명령 | 동일 스크립트를 Linux 실행환경에서 실행. `classifier()` 가 `platform.system()`/`platform.machine()` 으로 `linux-x64` 를 판정 | `build-cpf-generator-binary.py` `classifier()` |
| 11 | 집계 공식 방식 | **전용 aggregate 도구는 존재하지 않는다.** 스크립트가 `--output` 디렉터리에 3개 파일을 직접 생성하므로, 두 빌드가 같은 `--output` 을 가리키면 그 디렉터리가 곧 aggregate directory 다. 파일 복사·이름 변경은 불필요하며 금지 | `cpf-tools/generator/distribution/` 에 builder 와 requirements.txt 만 존재 |
| 12 | 최종 명령 | `.\cpf-tools
untime\cli\cpf.ps1 release open-git build --profile binary --generator-artifacts <MATRIX_DIR>` | `CpfCli.internalRelease` 잔여 인자 passthrough, `cpf_open_git.py:1244` |

### 8.1 Stage 07/09 재검증

publish 이후 Stage 07(`Raw Binary Repository 검증`)과 Stage 09(`최종 Binary Repository 검증`)에서
`_verify_generator_distributions(repository, version)` 가 Maven layout
`com/cpf/tooling/cpf-generator-cli/<version>/` 에 대해 동일 4항목을 재검증한다. 따라서 prebuilt
주입으로 Stage 06 을 통과해도 Stage 07/09 에서 checksum/manifest 무결성이 한 번 더 강제된다.

### 8.2 Fresh Release 전제

Stage 01 `Release Root 안전 확인` 이 이전 생성물을 전량 재생성하므로, 이전 실패한 `cpf-release/`
재사용은 발생하지 않는다. complete matrix 준비 후에는 Stage 01~14 전체를 다시 실행해야 한다.
