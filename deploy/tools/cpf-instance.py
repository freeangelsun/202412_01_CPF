#!/usr/bin/env python3
"""CPF deployment-manifest 기반 단일 Runtime Instance 설치·기동 도구.

Jenkins와 수동 배포가 같은 계약을 사용하도록 설치/기동 상태를 OS-neutral Python 한 곳에서 관리한다.
Secret은 manifest/instance.json에 쓰지 않고 외부 env file 또는 Process 환경으로만 주입한다.
"""
from __future__ import annotations
import argparse, hashlib, json, os, shlex, shutil, signal, socket, subprocess, sys, time, urllib.request
from pathlib import Path


def load_json(path: Path):
    return json.loads(path.read_text(encoding="utf-8-sig"))


def sha256(path: Path) -> str:
    h = hashlib.sha256()
    with path.open("rb") as f:
        for chunk in iter(lambda: f.read(1024 * 1024), b""):
            h.update(chunk)
    return h.hexdigest()


def safe_name(value: str, label: str) -> str:
    value = (value or "").strip()
    if not value or any(ch not in "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789._-" for ch in value):
        raise SystemExit(f"{label} 형식이 올바르지 않습니다: {value!r}")
    return value


def service_from_manifest(manifest: dict, service_name: str) -> dict:
    rows = [x for x in manifest.get("services", []) if x.get("serviceName") == service_name]
    if len(rows) != 1:
        raise SystemExit(f"serviceName은 manifest에서 정확히 1건이어야 합니다: {service_name}")
    row = rows[0]
    if row.get("artifactStatus") != "READY" or not row.get("artifact") or not row.get("sha256"):
        raise SystemExit(f"배포 가능한 artifact가 아닙니다: {service_name}")
    return row


def instance_dir(root: Path, service: str, instance_id: str) -> Path:
    return root / service / "instances" / instance_id


def install(ns) -> int:
    manifest_path = Path(ns.manifest).resolve()
    manifest = load_json(manifest_path)
    service = safe_name(ns.service, "service")
    instance_id = safe_name(ns.instance_id, "instance-id")
    row = service_from_manifest(manifest, service)
    artifact = (manifest_path.parent / row["artifact"]).resolve()
    if manifest_path.parent not in artifact.parents or not artifact.is_file():
        raise SystemExit(f"manifest artifact를 찾을 수 없습니다: {artifact}")
    actual = sha256(artifact)
    if actual.lower() != row["sha256"].lower():
        raise SystemExit(f"artifact SHA-256 불일치: {service}")
    profile = (ns.profile or row.get("profile") or manifest.get("environment") or "").strip()
    if not profile:
        raise SystemExit("profile은 필수입니다.")
    if not (1 <= ns.port <= 65535):
        raise SystemExit("port는 1~65535여야 합니다.")
    install_root = Path(ns.install_root).expanduser().resolve()
    release_id = actual[:16]
    release_dir = install_root / service / "releases" / release_id
    target_jar = release_dir / "app.jar"
    inst = instance_dir(install_root, service, instance_id)
    config = {
        "schemaVersion": 1,
        "serviceName": service,
        "instanceId": instance_id,
        "environment": manifest.get("environment"),
        "topology": manifest.get("topology"),
        "targetGroup": row.get("targetGroup"),
        "releaseId": release_id,
        "artifactSha256": actual,
        "profile": profile,
        "port": ns.port,
        "envFile": str(Path(ns.env_file).expanduser()) if ns.env_file else "",
    }
    print(json.dumps({"action": "INSTALL", "apply": ns.apply, "installRoot": str(install_root), "config": config}, ensure_ascii=False, indent=2))
    if not ns.apply:
        return 0
    release_dir.mkdir(parents=True, exist_ok=True)
    inst.mkdir(parents=True, exist_ok=True)
    (inst / "logs").mkdir(exist_ok=True)
    (inst / "work").mkdir(exist_ok=True)
    if not target_jar.exists() or sha256(target_jar) != actual:
        shutil.copy2(artifact, target_jar)
    (inst / "instance.json").write_text(json.dumps(config, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    return 0


def read_instance(ns):
    root = Path(ns.install_root).expanduser().resolve()
    service = safe_name(ns.service, "service")
    instance_id = safe_name(ns.instance_id, "instance-id")
    inst = instance_dir(root, service, instance_id)
    path = inst / "instance.json"
    if not path.is_file():
        raise SystemExit(f"instance 설정이 없습니다: {path}")
    return root, service, instance_id, inst, load_json(path)


def load_env_file(path_value: str) -> dict[str, str]:
    env = dict(os.environ)
    if not path_value:
        return env
    path = Path(path_value).expanduser()
    if not path.is_file():
        raise SystemExit(f"instance env file이 없습니다: {path}")
    for line_no, raw in enumerate(path.read_text(encoding="utf-8-sig").splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        if "=" not in line:
            raise SystemExit(f"env file 형식 오류 line={line_no}")
        key, value = line.split("=", 1)
        key = key.strip()
        if not key or not all(ch.isalnum() or ch == "_" for ch in key):
            raise SystemExit(f"env key 형식 오류 line={line_no}")
        env[key] = value.strip()
    return env


def pid_alive(pid: int) -> bool:
    try:
        os.kill(pid, 0)
        return True
    except (ProcessLookupError, PermissionError, OSError):
        return False


def require_port_available(port: int) -> None:
    """기동 직전 loopback bind로 포트 충돌을 fail-fast합니다."""
    family = socket.AF_INET
    with socket.socket(family, socket.SOCK_STREAM) as sock:
        sock.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 0)
        try:
            sock.bind(("127.0.0.1", int(port)))
        except OSError as ex:
            raise SystemExit(f"port가 이미 사용 중입니다: {port} ({ex})") from ex


def start(ns) -> int:
    root, service, instance_id, inst, cfg = read_instance(ns)
    pid_file = inst / "app.pid"
    if pid_file.is_file():
        try:
            old = int(pid_file.read_text().strip())
        except ValueError:
            old = -1
        if old > 0 and pid_alive(old):
            raise SystemExit(f"이미 실행 중입니다. pid={old}")
        pid_file.unlink(missing_ok=True)
    jar = root / service / "releases" / cfg["releaseId"] / "app.jar"
    if not jar.is_file() or sha256(jar) != cfg["artifactSha256"]:
        raise SystemExit("설치 JAR이 없거나 SHA-256이 변경되었습니다.")
    require_port_available(int(cfg["port"]))
    env = load_env_file(cfg.get("envFile", ""))
    java = env.get("CPF_JAVA", "java")
    java_opts = shlex.split(env.get("CPF_JAVA_OPTS", "-Xms256m -Xmx768m"), posix=os.name != "nt")
    cmd = [java, *java_opts, "-Dfile.encoding=UTF-8", "-jar", str(jar),
           f"--spring.profiles.active={cfg['profile']}", f"--server.port={cfg['port']}",
           f"--cpf.environment={cfg.get('environment') or cfg['profile']}", f"--cpf.instance-id={instance_id}"]
    stdout = (inst / "logs" / "app.out.log").open("ab")
    stderr = (inst / "logs" / "app.err.log").open("ab")
    kwargs = {"cwd": str(inst / "work"), "env": env, "stdin": subprocess.DEVNULL, "stdout": stdout, "stderr": stderr}
    if os.name == "nt":
        kwargs["creationflags"] = subprocess.CREATE_NEW_PROCESS_GROUP | subprocess.DETACHED_PROCESS
    else:
        kwargs["start_new_session"] = True
    proc = subprocess.Popen(cmd, **kwargs)
    pid_file.write_text(str(proc.pid) + "\n", encoding="ascii")
    print(f"STARTED service={service} instance={instance_id} pid={proc.pid} port={cfg['port']} profile={cfg['profile']}")
    if ns.wait_health:
        deadline = time.time() + ns.health_timeout
        url = f"http://127.0.0.1:{cfg['port']}{ns.health_path}"
        while time.time() < deadline:
            if proc.poll() is not None:
                raise SystemExit(f"기동 직후 종료했습니다. exit={proc.returncode}")
            try:
                with urllib.request.urlopen(url, timeout=ns.health_request_timeout) as response:
                    if 200 <= response.status < 300:
                        print(f"HEALTHY {url}")
                        return 0
            except Exception:
                time.sleep(1)
        raise SystemExit(f"health timeout: {url}")
    return 0


def stop(ns) -> int:
    _, service, instance_id, inst, _ = read_instance(ns)
    pid_file = inst / "app.pid"
    if not pid_file.is_file():
        print(f"STOPPED service={service} instance={instance_id}")
        return 0
    pid = int(pid_file.read_text().strip())
    if pid_alive(pid):
        os.kill(pid, signal.SIGTERM)
        deadline = time.time() + ns.timeout
        while time.time() < deadline and pid_alive(pid):
            time.sleep(0.5)
        if pid_alive(pid):
            if not ns.force:
                raise SystemExit(f"graceful stop timeout. pid={pid}; --force를 명시해야 강제 종료합니다.")
            os.kill(pid, signal.SIGKILL if os.name != "nt" else signal.SIGTERM)
    pid_file.unlink(missing_ok=True)
    print(f"STOPPED service={service} instance={instance_id} pid={pid}")
    return 0


def status(ns) -> int:
    _, service, instance_id, inst, cfg = read_instance(ns)
    pid_file = inst / "app.pid"
    if not pid_file.is_file():
        print(f"STOPPED service={service} instance={instance_id} port={cfg['port']}")
        return 1
    try:
        pid = int(pid_file.read_text().strip())
    except ValueError:
        print("STALE invalid pid file")
        return 2
    alive = pid_alive(pid)
    print(f"{'RUNNING' if alive else 'STALE'} service={service} instance={instance_id} pid={pid} port={cfg['port']}")
    return 0 if alive else 2


def parser():
    ap = argparse.ArgumentParser(description="CPF Runtime Instance install/start/stop/status")
    sub = ap.add_subparsers(dest="command", required=True)
    p = sub.add_parser("install")
    p.add_argument("--manifest", required=True); p.add_argument("--service", required=True); p.add_argument("--instance-id", required=True)
    p.add_argument("--install-root", required=True); p.add_argument("--port", type=int, required=True); p.add_argument("--profile"); p.add_argument("--env-file")
    p.add_argument("--apply", action="store_true"); p.set_defaults(func=install)
    p = sub.add_parser("start")
    p.add_argument("--install-root", required=True); p.add_argument("--service", required=True); p.add_argument("--instance-id", required=True)
    p.add_argument("--wait-health", action="store_true"); p.add_argument("--health-path", default="/actuator/health")
    p.add_argument("--health-timeout", type=int, default=60); p.add_argument("--health-request-timeout", type=float, default=2.0); p.set_defaults(func=start)
    p = sub.add_parser("stop")
    p.add_argument("--install-root", required=True); p.add_argument("--service", required=True); p.add_argument("--instance-id", required=True)
    p.add_argument("--timeout", type=int, default=30); p.add_argument("--force", action="store_true"); p.set_defaults(func=stop)
    p = sub.add_parser("status")
    p.add_argument("--install-root", required=True); p.add_argument("--service", required=True); p.add_argument("--instance-id", required=True); p.set_defaults(func=status)
    return ap


if __name__ == "__main__":
    ns = parser().parse_args()
    raise SystemExit(ns.func(ns))
