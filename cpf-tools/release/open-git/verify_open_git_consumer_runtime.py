#!/usr/bin/env python3
"""Fresh Open Git Consumer 가 공개 진입점만으로 Runtime 을 실제 기동하는지 검증한다.

Binary Repository 와 Generator 가 PASS 여도 사용자가 화면을 열지 못하면 Release 가 아니다.
이 검증기는 Development Master 를 참조하지 않고, 공개 checkout 안의 launcher 만 사용한다.

검증 순서(사용자 여정 그대로):

1. Fresh checkout 확인      - 공개 트리에 내부 Source/Tooling 이 없어야 한다.
2. Runtime Target 해석      - `cpf runtime targets` 가 admin / backoffice / backoffice-web 를 보여야 한다.
3. ADM 기동                 - 공개 launcher 로 기동하고 SPA 진입 화면과 login endpoint 가 살아야 한다.
4. MBW + Backoffice Web 기동 - Backoffice Web 은 ADM 이 아니라 MBW Channel Front 다.
5. 실제 API 거래             - Backoffice Web 을 통해 MBW API 를 호출하고 Header/Auth/CSRF 계약을 확인한다.
6. status / stop / cleanup   - 같은 공개 launcher 로 상태 확인과 정지가 되어야 한다.

사용자가 npm 명령을 직접 치거나 내부 Gradle project path, Development Master script,
private source 를 알아야 실행된다면 FAIL 이다.
"""

from __future__ import annotations

import argparse
import io
import json
import os
import platform
import subprocess
import sys
import time
import urllib.error
import urllib.request
import uuid
from pathlib import Path
from http.cookiejar import CookieJar

for _stream in (sys.stdout, sys.stderr):
    try:
        _stream.reconfigure(encoding="utf-8", errors="replace")
    except (AttributeError, ValueError):
        pass

# 공개 checkout 에 있으면 안 되는 내부 경로. 하나라도 있으면 "공개 배포본만으로 실행"이 아니다.
FORBIDDEN_INTERNAL = ("cpf-core", "cpf-common", "cpf-admin", "cpf-starters", "cpf-tools", "cpf-batch")

# Consumer 가 반드시 기동할 수 있어야 하는 Runtime.
REQUIRED_TARGETS = ("admin", "backoffice", "backoffice-web")

# Fresh Consumer Runtime은 실제 최초 운영자 생성·로그인까지 해야 한다. 값은 명령행이나
# evidence JSON에 절대 쓰지 않고, verifier process의 environment에서만 읽는다.
REQUIRED_CREDENTIAL_ENV = (
    "CPF_ADM_BOOTSTRAP_PASSWORD",
    "CPF_ADM_BOOTSTRAP_OPERATOR_ID",
    "CPF_ADM_BOOTSTRAP_OPERATOR_NAME",
    "CPF_MBW_BOOTSTRAP_PASSWORD",
    "CPF_MBW_INITIAL_OPERATOR_LOGIN_ID",
    "CPF_MBW_INITIAL_OPERATOR_NAME",
    "CPF_MBW_INITIAL_OPERATOR_ROLE_CODE",
    "CPF_MBW_JWT_SECRET",
)

WINDOWS = platform.system().lower().startswith("win")

# 운영 시간값의 정본은 소스가 아니라 설정 파일이다. 검증기는 읽기만 하고 기본값으로 대체하지 않는다.
POLICY_PATH = Path(__file__).resolve().parent / "open-git-consumer-runtime-policy.yml"


class ConsumerRuntimeError(RuntimeError):
    pass


def load_policy(path: Path) -> dict[str, int]:
    """평문 key: value 정책을 의존성 없이 읽는다.

    선택 의존성(PyYAML)이 없을 때 조용히 기본값으로 넘어가면 정책이 있으나 마나 해진다.
    파일이나 값이 없으면 fail-closed 한다.
    """
    if not path.is_file():
        raise ConsumerRuntimeError(f"consumer runtime policy missing: {path}")
    values: dict[str, int] = {}
    for line in io.open(path, encoding="utf-8").read().splitlines():
        stripped = line.strip()
        if not stripped or stripped.startswith("#") or ":" not in stripped:
            continue
        key, raw = stripped.split(":", 1)
        raw = raw.split("#", 1)[0].strip()
        if raw.isdigit():
            values[key.strip()] = int(raw)
    return values


def seconds(policy: dict[str, int], key: str) -> int:
    value = policy.get(key)
    if not isinstance(value, int) or value < 1:
        raise ConsumerRuntimeError(
            f"consumer runtime policy has no positive value for '{key}': {POLICY_PATH}")
    return value


POLICY = load_policy(POLICY_PATH)
HTTP_PROBE_SECONDS = seconds(POLICY, "httpProbeSeconds")
HTTP_POLL_SECONDS = seconds(POLICY, "httpPollSeconds")
HTTP_POLL_INTERVAL_SECONDS = seconds(POLICY, "httpPollIntervalSeconds")
ADM_READY_SECONDS = seconds(POLICY, "admReadySeconds")
SOURCE_READY_SECONDS = seconds(POLICY, "sourceReadySeconds")
LAUNCHER_START_SECONDS = seconds(POLICY, "launcherStartSeconds")
LAUNCHER_COMMAND_SECONDS = seconds(POLICY, "launcherCommandSeconds")
LAUNCHER_STOP_SECONDS = seconds(POLICY, "launcherStopSeconds")
BOOTSTRAP_SECONDS = seconds(POLICY, "bootstrapSeconds")


def step(name: str, detail: str = "") -> None:
    print(f"[CPF][OPEN-GIT][CONSUMER] {name} {detail}".rstrip(), flush=True)


def launcher(checkout: Path, command: str) -> list[str]:
    """공개 launcher 만 사용한다. 내부 Gradle task 나 Master script 를 부르지 않는다."""
    if WINDOWS:
        script = checkout / "bin" / f"cpf-{command}.ps1"
        if not script.is_file():
            raise ConsumerRuntimeError(f"public launcher missing: {script}")
        return ["pwsh", "-NoProfile", "-File", str(script)]
    script = checkout / "bin" / f"cpf-{command}.sh"
    if not script.is_file():
        raise ConsumerRuntimeError(f"public launcher missing: {script}")
    return ["sh", str(script)]


def target_argument(name: str) -> list[str]:
    return ["-Target", name] if WINDOWS else ["--target", name]


def run(command: list[str], cwd: Path, *, timeout: int, env: dict[str, str] | None = None,
        check: bool = True) -> subprocess.CompletedProcess:
    merged = dict(os.environ)
    if env:
        merged.update(env)
    done = subprocess.run(command, cwd=str(cwd), env=merged, text=True, encoding="utf-8",
                          errors="replace", capture_output=True, timeout=timeout)
    if check and done.returncode != 0:
        raise ConsumerRuntimeError(
            f"consumer command failed rc={done.returncode} command={command[:3]}\n"
            f"{done.stdout}\n{done.stderr}")
    return done


def http(url: str, *, method: str = "GET", timeout: int = HTTP_PROBE_SECONDS,
         headers: dict[str, str] | None = None, body: bytes | None = None,
         opener: urllib.request.OpenerDirector | None = None):
    request = urllib.request.Request(url, method=method, data=body)
    for key, value in (headers or {}).items():
        request.add_header(key, value)
    try:
        client = opener or urllib.request.build_opener()
        with client.open(request, timeout=timeout) as response:
            return response.status, response.read(), dict(response.headers)
    except urllib.error.HTTPError as failure:
        return failure.code, failure.read(), dict(failure.headers)


def wait_http(url: str, *, seconds: int, accept: tuple[int, ...]) -> int:
    deadline = time.monotonic() + seconds
    last = -1
    while time.monotonic() < deadline:
        try:
            status, _, _ = http(url, timeout=HTTP_POLL_SECONDS)
            last = status
            if status in accept:
                return status
        except (urllib.error.URLError, OSError, TimeoutError):
            last = -1
        time.sleep(HTTP_POLL_INTERVAL_SECONDS)
    raise ConsumerRuntimeError(f"endpoint did not become reachable: {url} lastStatus={last}")


def verify_fresh_checkout(checkout: Path) -> None:
    step("01 Fresh checkout", str(checkout))
    if not checkout.is_dir():
        raise ConsumerRuntimeError(f"consumer checkout missing: {checkout}")
    leaked = [name for name in FORBIDDEN_INTERNAL if (checkout / name).exists()]
    if leaked:
        raise ConsumerRuntimeError(f"internal source present in public checkout: {leaked}")
    for required in ("bin", "README.md", "binary-repository",
                     "config/cpf-runtime-target-catalog.json"):
        if not (checkout / required).exists():
            raise ConsumerRuntimeError(f"public consumer entry missing: {required}")


def verify_bootstrap(checkout: Path) -> dict[str, str]:
    """공식 bootstrap 경로로 prerequisite 를 준비한다.

    사용자는 Development Master 없이 이 명령 하나로 Java/Container Runtime 확인, DB 기동,
    스키마 적용까지 끝낼 수 있어야 한다. 이 단계를 건너뛰고 Runtime 만 띄우면 실제
    사용자 여정을 검증한 것이 아니다.
    """
    step("02 bootstrap", "prerequisite / DB / schema")
    # README 가 안내하는 진입점 그대로 호출한다(bin/cpf bootstrap).
    if WINDOWS:
        entry = checkout / "bin" / "cpf.ps1"
        command = ["pwsh", "-NoProfile", "-File", str(entry), "bootstrap"]
    else:
        entry = checkout / "bin" / "cpf"
        command = ["sh", str(entry), "bootstrap"]
    if not entry.is_file():
        raise ConsumerRuntimeError(f"public bootstrap entry missing: {entry}")
    run(command, checkout, timeout=BOOTSTRAP_SECONDS)
    return {"status": "PASS"}


def verify_targets(checkout: Path) -> dict[str, dict]:
    step("03 Runtime Target 해석")
    catalog = json.loads((checkout / "config/cpf-runtime-target-catalog.json").read_text(encoding="utf-8"))
    entries = {str(item.get("target")): item for item in catalog.get("runtimes", [])}
    missing = [name for name in REQUIRED_TARGETS if name not in entries]
    if missing:
        raise ConsumerRuntimeError(f"required runtime target missing from published catalog: {missing}")
    listed = run(launcher(checkout, "help"), checkout, timeout=LAUNCHER_COMMAND_SECONDS, check=False)
    output = listed.stdout + listed.stderr
    unlisted = [name for name in REQUIRED_TARGETS if name not in output]
    if unlisted:
        raise ConsumerRuntimeError(
            f"public launcher does not offer required targets: {unlisted}\n{output[:2000]}")
    return entries


def port_of(entries: dict[str, dict], name: str) -> int:
    value = entries[name].get("port")
    if not isinstance(value, int):
        raise ConsumerRuntimeError(f"runtime target has no canonical port: {name}")
    return value


def start_target(checkout: Path, name: str, env: dict[str, str] | None = None) -> None:
    run(launcher(checkout, "start") + target_argument(name), checkout, timeout=LAUNCHER_START_SECONDS, env=env)


def stop_target(checkout: Path, name: str) -> None:
    run(launcher(checkout, "stop") + target_argument(name), checkout, timeout=LAUNCHER_STOP_SECONDS, check=False)


def json_object(body: bytes, *, context: str) -> dict[str, object]:
    try:
        value = json.loads(body.decode("utf-8"))
    except (UnicodeDecodeError, json.JSONDecodeError) as failure:
        raise ConsumerRuntimeError(f"{context} did not return a JSON object") from failure
    if not isinstance(value, dict):
        raise ConsumerRuntimeError(f"{context} did not return a JSON object")
    return value


def required_credentials() -> dict[str, str]:
    missing = [key for key in REQUIRED_CREDENTIAL_ENV if not os.environ.get(key)]
    if missing:
        # secret의 값은 물론 identifier의 값도 출력하지 않는다. 필요한 key 이름만 알려 준다.
        raise SystemExit("required Fresh Consumer Runtime environment is missing: " + ", ".join(missing))
    return {key: os.environ[key] for key in REQUIRED_CREDENTIAL_ENV}


def cookie_names(jar: CookieJar) -> set[str]:
    return {cookie.name for cookie in jar}


def verify_adm(checkout: Path, entries: dict[str, dict], credentials: dict[str, str]) -> dict:
    port = port_of(entries, "admin")
    step("04 ADM 기동", f"port={port}")
    start_target(checkout, "admin", env={
        "CPF_ADM_BOOTSTRAP_OPERATOR_ID": credentials["CPF_ADM_BOOTSTRAP_OPERATOR_ID"],
        "CPF_ADM_BOOTSTRAP_OPERATOR_NAME": credentials["CPF_ADM_BOOTSTRAP_OPERATOR_NAME"],
        "CPF_ADM_BOOTSTRAP_PASSWORD": credentials["CPF_ADM_BOOTSTRAP_PASSWORD"],
    })
    base = f"http://127.0.0.1:{port}"
    wait_http(f"{base}/adm/api/health", seconds=ADM_READY_SECONDS, accept=(200,))

    # ADM 콘솔은 Browser BFF 다. 실제 사용자 여정과 같은 방식(Cookie Jar + CSRF + Origin)으로 확인한다.
    # Bearer token 을 기대하면 Browser 세션 계약을 검증하지 못하고, 오히려 token 이 body 로
    # 새는 구현을 통과시킨다.
    jar = CookieJar()
    browser = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(jar))

    # 화면이 실제로 실려 있어야 한다. Runtime 만 뜨고 bundle 이 없으면 사용자는 아무것도 못 본다.
    status, body, _ = http(f"{base}/adm/", opener=browser)
    if status != 200 or b"<div id=" not in body and b"<script" not in body:
        raise ConsumerRuntimeError(f"ADM SPA entry is not served: status={status} bytes={len(body)}")
    csrf_token = next((cookie.value for cookie in jar if cookie.name == "XSRF-TOKEN"), None)
    if not csrf_token:
        raise ConsumerRuntimeError("ADM SPA entry did not issue the XSRF-TOKEN cookie")

    # health/SPA만으로는 Consumer가 실제로 운영 콘솔을 쓸 수 있는지 증명하지 못한다.
    login_status, login_body, _ = http(
        f"{base}/adm/api/auth/login", method="POST",
        headers={"Content-Type": "application/json", "X-XSRF-TOKEN": csrf_token,
                 "Origin": base, "Referer": f"{base}/adm/"},
        body=json.dumps({
            "operatorId": credentials["CPF_ADM_BOOTSTRAP_OPERATOR_ID"],
            "password": credentials["CPF_ADM_BOOTSTRAP_PASSWORD"],
        }).encode("utf-8"), opener=browser)
    if login_status != 200:
        raise ConsumerRuntimeError(f"ADM initial operator login did not succeed: status={login_status}")
    login = json_object(login_body, context="ADM login")
    if "accessToken" in login or "refreshToken" in login:
        raise ConsumerRuntimeError("ADM login exposed an upstream credential in the Browser body")
    if "CPFSESSION" not in cookie_names(jar):
        raise ConsumerRuntimeError("ADM login did not establish the HttpOnly Browser session cookie")
    if not login.get("menus"):
        raise ConsumerRuntimeError("ADM login did not return an authorized console composition")

    me_status, _, _ = http(f"{base}/adm/api/auth/me", opener=browser)
    if me_status != 200:
        raise ConsumerRuntimeError(f"ADM authenticated operation did not succeed: status={me_status}")
    return {"port": port, "spa": "PASS", "health": "PASS", "login": "PASS", "authenticatedOperation": "PASS"}


def verify_backoffice(checkout: Path, entries: dict[str, dict], credentials: dict[str, str]) -> dict:
    online_port = port_of(entries, "backoffice")
    web_port = port_of(entries, "backoffice-web")
    step("05 MBW + Backoffice Web 기동", f"mbw={online_port} web={web_port}")
    start_target(checkout, "backoffice", env={
        "CPF_MBW_BOOTSTRAP_PASSWORD": credentials["CPF_MBW_BOOTSTRAP_PASSWORD"],
        "CPF_MBW_INITIAL_OPERATOR_LOGIN_ID": credentials["CPF_MBW_INITIAL_OPERATOR_LOGIN_ID"],
        "CPF_MBW_INITIAL_OPERATOR_NAME": credentials["CPF_MBW_INITIAL_OPERATOR_NAME"],
        "CPF_MBW_INITIAL_OPERATOR_ROLE_CODE": credentials["CPF_MBW_INITIAL_OPERATOR_ROLE_CODE"],
        "CPF_MBW_JWT_SECRET": credentials["CPF_MBW_JWT_SECRET"],
    })
    wait_http(f"http://127.0.0.1:{online_port}/actuator/health", seconds=SOURCE_READY_SECONDS, accept=(200,))
    # local Consumer E2E의 HTTP endpoint/secure-cookie 값만 다르며, bootstrap/auth/approval
    # 보안 의미는 profile에 따라 달라지지 않는다. Gateway를 별도 기동하지 않는 이 여정은
    # BFF의 공식 DIRECT local transport를 사용한다.
    start_target(checkout, "backoffice-web", env={
        "MBW_WEB_MODE": "DIRECT",
        "MBW_DIRECT_BASE_URI": f"http://127.0.0.1:{online_port}",
        "MBW_WEB_SECURE_COOKIES": "false",
    })
    web = f"http://127.0.0.1:{web_port}"
    wait_http(f"{web}/actuator/health", seconds=SOURCE_READY_SECONDS, accept=(200,))

    status, body, _ = http(f"{web}/mbw/")
    if status != 200 or (b"<script" not in body and b"<div id=" not in body):
        raise ConsumerRuntimeError(
            f"Backoffice Web SPA entry is not served: status={status} bytes={len(body)}")

    step("06 실제 API 거래", "Backoffice Web -> MBW, login / Cookie / CSRF / business operation")
    # 인증 전 상태에서 보호 자원은 반드시 거절되어야 한다(권한 계약이 살아 있는지 확인).
    protected, _, _ = http(f"{web}/api/v1/backoffice/backoffice/organizations")
    if protected not in (401, 403):
        raise ConsumerRuntimeError(
            f"Backoffice Web protected resource is not guarded: status={protected}")
    # 로그인 전 CSRF 발급은 public bootstrap endpoint여야 한다. 이 token/cookie 쌍이 없으면
    # Browser가 session mutation을 안전하게 시작할 수 없다.
    cookies = CookieJar()
    browser = urllib.request.build_opener(urllib.request.HTTPCookieProcessor(cookies))
    csrf_status, csrf_body, _ = http(f"{web}/api/v1/backoffice/security/csrf", opener=browser)
    if csrf_status != 200:
        raise ConsumerRuntimeError(f"Backoffice Web pre-login CSRF bootstrap failed: status={csrf_status}")
    csrf = json_object(csrf_body, context="Backoffice Web CSRF bootstrap")
    csrf_header = csrf.get("headerName")
    csrf_token = csrf.get("token")
    if not isinstance(csrf_header, str) or not csrf_header or not isinstance(csrf_token, str) or not csrf_token:
        raise ConsumerRuntimeError("Backoffice Web CSRF bootstrap returned no usable header/token")
    if "XSRF-TOKEN" not in cookie_names(cookies):
        raise ConsumerRuntimeError("Backoffice Web CSRF bootstrap did not issue XSRF-TOKEN cookie")

    login_status, login_body, _ = http(
        f"{web}/api/v1/backoffice/auth/login", method="POST",
        headers={"Content-Type": "application/json", csrf_header: csrf_token,
                 "Idempotency-Key": "cpf-open-git-consumer-" + uuid.uuid4().hex},
        body=json.dumps({
            "loginId": credentials["CPF_MBW_INITIAL_OPERATOR_LOGIN_ID"],
            "password": credentials["CPF_MBW_BOOTSTRAP_PASSWORD"],
        }).encode("utf-8"), opener=browser)
    if login_status != 200:
        raise ConsumerRuntimeError(f"MBW initial operator login did not succeed: status={login_status}")
    login = json_object(login_body, context="Backoffice Web login")
    if login.get("authenticated") is not True:
        raise ConsumerRuntimeError("Backoffice Web login did not establish an authenticated Browser session")
    if "accessToken" in login or "refreshToken" in login:
        raise ConsumerRuntimeError("Backoffice Web login exposed an upstream credential in the Browser body")
    session_cookies = cookie_names(cookies)
    if not {"CPF_MBW_ACCESS", "CPF_MBW_REFRESH"}.issubset(session_cookies):
        raise ConsumerRuntimeError("Backoffice Web login did not establish both HttpOnly session cookies")

    me_status, _, _ = http(f"{web}/api/v1/backoffice/auth/me", opener=browser)
    if me_status != 200:
        raise ConsumerRuntimeError(f"Backoffice Web authenticated session query failed: status={me_status}")
    business_status, _, business_headers = http(
        f"{web}/api/v1/backoffice/backoffice/organizations", opener=browser)
    if business_status != 200:
        raise ConsumerRuntimeError(f"Backoffice Web -> MBW business transaction did not succeed: status={business_status}")
    transaction_id = business_headers.get("X-Transaction-Id") or business_headers.get("x-transaction-id")
    return {
        "onlinePort": online_port, "webPort": web_port, "spa": "PASS",
        "protectedStatus": protected, "preLoginCsrf": "PASS", "login": "PASS",
        "authenticatedSession": "PASS", "businessOperation": "MBW_BACKOFFICE_FIND_ORGANIZATIONS",
        "businessStatus": business_status,
        # 식별자는 secret이 아니며 File/DB Log correlation의 검색 key다. upstream이 회신하지
        # 않으면 runtime correlation 단계가 별도 trace에서 같은 key를 확보한다.
        "transactionId": transaction_id or "NOT_RETURNED",
    }


def verify_status_and_stop(checkout: Path) -> dict:
    step("07 status / stop")
    result = {}
    for name in REQUIRED_TARGETS:
        done = run(launcher(checkout, "status") + target_argument(name), checkout,
                   timeout=LAUNCHER_COMMAND_SECONDS, check=False)
        result[name] = "PASS" if done.returncode == 0 else f"rc={done.returncode}"
    for name in reversed(REQUIRED_TARGETS):
        stop_target(checkout, name)
    return result


def main() -> int:
    parser = argparse.ArgumentParser(description="Open Git Fresh Consumer Runtime verification")
    parser.add_argument("--checkout", required=True, help="공개 Open Git checkout 경로")
    parser.add_argument("--result", help="결과 JSON 경로")
    args = parser.parse_args()
    checkout = Path(args.checkout).resolve()

    credentials = required_credentials()

    result: dict[str, object] = {"status": "FAIL", "checkout": str(checkout)}
    try:
        verify_fresh_checkout(checkout)
        result["bootstrap"] = verify_bootstrap(checkout)
        entries = verify_targets(checkout)
        result["adm"] = verify_adm(checkout, entries, credentials)
        result["backoffice"] = verify_backoffice(checkout, entries, credentials)
        result["lifecycle"] = verify_status_and_stop(checkout)
        result["status"] = "PASS"
        code = 0
    except Exception as failure:  # noqa: BLE001 - 결과를 항상 증적으로 남긴다
        result["message"] = str(failure)
        code = 1
        try:
            verify_status_and_stop(checkout)
        except Exception:  # noqa: BLE001 - cleanup 실패가 원인을 가리지 않게 한다
            pass
    if args.result:
        out = Path(args.result)
        out.parent.mkdir(parents=True, exist_ok=True)
        out.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
