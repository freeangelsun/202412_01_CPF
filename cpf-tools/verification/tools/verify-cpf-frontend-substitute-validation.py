#!/usr/bin/env python3
"""Validate ADM/BZA shared API actor trust controls with local Node/TypeScript/Chromium.

The script uses the real cpfApi.ts files, creates only temporary dependency stubs,
runs strict TypeScript compilation, Node runtime contract tests, and Chromium browser
contract tests. It does not write node_modules, dist, logs, or caches into the repository.
"""
from __future__ import annotations

import argparse
import contextlib
import functools
import http.server
import importlib.metadata
import json
from pathlib import Path
import re
import shutil
import socketserver
import subprocess
import tempfile
import threading
from typing import Any

FRONTENDS = {
    "ADM": Path("cpf-admin/frontend/src/shared/cpfApi.ts"),
    "BZA": Path("cpf-biz-admin/frontend/src/shared/cpfApi.ts"),
}
IMPORT_RE = re.compile(r"^import\s+.*?;\s*$", re.MULTILINE)

PREFIX = r'''
class MutationObserver<T, E, V, C> {
  constructor(private readonly client: any, private readonly options: any) {}
  async mutate(value: unknown): Promise<T> { return await this.options.mutationFn(value); }
  reset(): void {}
}
class CpfOrvalError extends Error { status = 500; payload: unknown = null; }
async function cpfOrvalRequest<T>(config: unknown): Promise<T> { return { ok: true, config } as T; }
type CpfOperationId = string;
const cpfOperationDescriptors = [{ operationId: "op", template: "/x", method: "GET" }];
function resolveCpfOperation(_method: string, _path: string): { operationId: CpfOperationId } { return { operationId: "op" }; }
const cpfQueryClient = {
  async fetchQuery<T>(options: any): Promise<T> { return await options.queryFn(); },
  async invalidateQueries(_options: unknown): Promise<void> {}
};
function createTransactionId(): string { return "CPF-20260803-000000000000000000000000"; }
const defaultHeaders: HeadersInit = {};
function isValidTransactionId(value: string | null): boolean { return Boolean(value); }
'''

NODE_HARNESS = r'''
(globalThis as any).window = { location: { origin: "https://cpf.local" } };
(globalThis as any).document = { cookie: "" };
import { admQuery, admMutation, cpfGeneratedRequest, createAdmHeaders } from "./cpfApi";
const aliases = ["requestUser", "requestedBy", "actorId", "operatorId", "operatorIdOverride"];
async function expectReject(label: string, action: () => Promise<unknown>, fragment: string): Promise<void> {
  try { await action(); throw new Error(label + " accepted"); }
  catch (failure) {
    const text = String(failure);
    if (text.includes(" accepted")) throw failure;
    if (!text.includes(fragment)) throw new Error(label + " wrong error: " + text);
  }
}
(async () => {
  for (const alias of aliases) {
    await expectReject("query " + alias, () => admQuery("/x?" + alias + "=browser"), "Browser actor");
    await expectReject("params " + alias, () => cpfGeneratedRequest({ url: "/x", method: "GET", params: { [alias]: "browser" } }), "Browser actor");
    await expectReject("body " + alias, () => admMutation("/x", "POST", { nested: [{ [alias]: "browser" }] }), "Browser actor");
  }
  try { createAdmHeaders({ Authorization: "Bearer browser" }); throw new Error("authorization accepted"); }
  catch (failure) { if (!String(failure).includes("Bearer Token")) throw failure; }
  const allowed = await admMutation<any>("/x", "POST", { targetId: "BAT-01", reason: "test" });
  if (!allowed || allowed.ok !== true) throw new Error("allowed mutation did not reach stub consumer");
  console.log("FRONTEND_NODE_RUNTIME_PASS aliases=5 paths=3 allowed=1");
})().catch((failure) => { console.error(failure); (globalThis as any).process.exit(1); });
'''

BROWSER_HARNESS = r'''
import * as api from "./cpfApi.js";
const aliases = ["requestUser", "requestedBy", "actorId", "operatorId", "operatorIdOverride"];
async function expectReject(label, action, fragment) {
  try { await action(); throw new Error(label + " accepted"); }
  catch (failure) {
    const text = String(failure);
    if (text.includes(" accepted")) throw failure;
    if (!text.includes(fragment)) throw new Error(label + " wrong error: " + text);
  }
}
export async function run() {
  for (const alias of aliases) {
    await expectReject("query " + alias, () => api.admQuery("/x?" + alias + "=browser"), "Browser actor");
    await expectReject("params " + alias, () => api.cpfGeneratedRequest({ url: "/x", method: "GET", params: { [alias]: "browser" } }), "Browser actor");
    await expectReject("body " + alias, () => api.admMutation("/x", "POST", { nested: [{ [alias]: "browser" }] }), "Browser actor");
  }
  try { api.createAdmHeaders({ Authorization: "Bearer browser" }); throw new Error("authorization accepted"); }
  catch (failure) { if (!String(failure).includes("Bearer Token")) throw failure; }
  const allowed = await api.admMutation("/x", "POST", { targetId: "BAT-01", reason: "test" });
  if (!allowed || allowed.ok !== true) throw new Error("allowed mutation did not reach stub consumer");
  return { status: "PASS", aliases: aliases.length, rejectedPathsPerAlias: 3, allowedMutations: 1 };
}
'''


def run(command: list[str], cwd: Path | None = None) -> dict[str, Any]:
    completed = subprocess.run(command, cwd=cwd, text=True, capture_output=True, check=False)
    return {
        "command": command,
        "exitCode": completed.returncode,
        "stdout": completed.stdout,
        "stderr": completed.stderr,
    }


def prepare_source(source: Path) -> str:
    text = source.read_text(encoding="utf-8")
    imports = IMPORT_RE.findall(text)
    if len(imports) < 5:
        raise RuntimeError(f"unexpected import structure: {source}")
    body = IMPORT_RE.sub("", text)
    required = ["CLIENT_ACTOR_FIELDS", "assertNoClientActor", "assertNoClientActorQuery", "cpfGeneratedRequest"]
    missing = [token for token in required if token not in body]
    if missing:
        raise RuntimeError(f"required frontend trust controls missing in {source}: {missing}")
    return PREFIX + "\n" + body


def write_tsconfig(path: Path, module: str, out_dir: str) -> None:
    path.write_text(json.dumps({
        "compilerOptions": {
            "target": "ES2022",
            "module": module,
            "moduleResolution": "Node",
            "strict": True,
            "outDir": out_dir,
            "lib": ["ES2022", "DOM", "DOM.Iterable"],
            "types": [],
            "skipLibCheck": False,
            "noEmitOnError": True,
        },
        "include": ["src/**/*.ts", "src/**/*.d.ts"],
    }, indent=2), encoding="utf-8")


class QuietHandler(http.server.SimpleHTTPRequestHandler):
    def log_message(self, format: str, *args: object) -> None:
        return


@contextlib.contextmanager
def serve(directory: Path):
    handler = functools.partial(QuietHandler, directory=str(directory))
    server = socketserver.TCPServer(("127.0.0.1", 0), handler)
    thread = threading.Thread(target=server.serve_forever, daemon=True)
    thread.start()
    try:
        yield f"http://127.0.0.1:{server.server_address[1]}"
    finally:
        server.shutdown()
        server.server_close()
        thread.join(timeout=5)


def validate_one(label: str, source: Path, tsc: str, node: str) -> dict[str, Any]:
    with tempfile.TemporaryDirectory(prefix=f"cpf-{label.lower()}-frontend-") as temp:
        root = Path(temp)
        # Node/CommonJS compile and runtime
        node_project = root / "node"
        (node_project / "src").mkdir(parents=True)
        (node_project / "src/cpfApi.ts").write_text(prepare_source(source), encoding="utf-8")
        (node_project / "src/harness.ts").write_text(NODE_HARNESS, encoding="utf-8")
        (node_project / "src/globals.d.ts").write_text("declare const process: { exit(code: number): never };\n", encoding="utf-8")
        write_tsconfig(node_project / "tsconfig.json", "CommonJS", "dist")
        typecheck = run([tsc, "--project", str(node_project / "tsconfig.json"), "--noEmit"], cwd=node_project)
        if typecheck["exitCode"] != 0:
            raise RuntimeError(json.dumps(typecheck, ensure_ascii=False))
        compile_node = run([tsc, "--project", str(node_project / "tsconfig.json")], cwd=node_project)
        if compile_node["exitCode"] != 0:
            raise RuntimeError(json.dumps(compile_node, ensure_ascii=False))
        node_check = run([node, "--check", str(node_project / "dist/harness.js")])
        if node_check["exitCode"] != 0:
            raise RuntimeError(json.dumps(node_check, ensure_ascii=False))
        node_runtime = run([node, str(node_project / "dist/harness.js")])
        if node_runtime["exitCode"] != 0 or "FRONTEND_NODE_RUNTIME_PASS" not in str(node_runtime["stdout"]):
            raise RuntimeError(json.dumps(node_runtime, ensure_ascii=False))

        # Browser/ES module compile and Chromium runtime
        browser_project = root / "browser"
        (browser_project / "src").mkdir(parents=True)
        (browser_project / "src/cpfApi.ts").write_text(prepare_source(source), encoding="utf-8")
        write_tsconfig(browser_project / "tsconfig.json", "ES2022", "dist")
        compile_browser = run([tsc, "--project", str(browser_project / "tsconfig.json")], cwd=browser_project)
        if compile_browser["exitCode"] != 0:
            raise RuntimeError(json.dumps(compile_browser, ensure_ascii=False))
        import base64
        module_source = (browser_project / "dist/cpfApi.js").read_bytes()
        module_url = "data:text/javascript;base64," + base64.b64encode(module_source).decode("ascii")

        from playwright.sync_api import sync_playwright
        with sync_playwright() as playwright:
            chromium_executable = shutil.which("chromium") or shutil.which("chromium-browser") or playwright.chromium.executable_path
            if not chromium_executable or not Path(chromium_executable).is_file():
                raise RuntimeError("Chromium executable is unavailable")
            browser = playwright.chromium.launch(headless=True, executable_path=chromium_executable, args=["--no-sandbox"])
            page = browser.new_page()
            page.set_content("<!doctype html><meta charset='utf-8'><title>CPF frontend validation</title>")
            browser_result = page.evaluate(
                """async (moduleUrl) => {
                  Object.defineProperty(Document.prototype, "cookie", { configurable: true, get: () => "", set: () => true });
                  const NativeURL = globalThis.URL;
                  globalThis.URL = new Proxy(NativeURL, {
                    construct(target, args) {
                      const normalized = [...args];
                      if (normalized.length > 1 && normalized[1] === "null") normalized[1] = "https://cpf.local";
                      return new target(...normalized);
                    }
                  });
                  const api = await import(moduleUrl);
                  const aliases = ["requestUser", "requestedBy", "actorId", "operatorId", "operatorIdOverride"];
                  async function expectReject(label, action, fragment) {
                    try { await action(); throw new Error(label + " accepted"); }
                    catch (failure) {
                      const text = String(failure);
                      if (text.includes(" accepted")) throw failure;
                      if (!text.includes(fragment)) throw new Error(label + " wrong error: " + text);
                    }
                  }
                  for (const alias of aliases) {
                    await expectReject("query " + alias, () => api.admQuery("/x?" + alias + "=browser"), "Browser actor");
                    await expectReject("params " + alias, () => api.cpfGeneratedRequest({ url: "/x", method: "GET", params: { [alias]: "browser" } }), "Browser actor");
                    await expectReject("body " + alias, () => api.admMutation("/x", "POST", { nested: [{ [alias]: "browser" }] }), "Browser actor");
                  }
                  try { api.createAdmHeaders({ Authorization: "Bearer browser" }); throw new Error("authorization accepted"); }
                  catch (failure) { if (!String(failure).includes("Bearer Token")) throw failure; }
                  const allowed = await api.admMutation("/x", "POST", { targetId: "BAT-01", reason: "test" });
                  if (!allowed || allowed.ok !== true) throw new Error("allowed mutation did not reach stub consumer");
                  return { status: "PASS", aliases: aliases.length, rejectedPathsPerAlias: 3, allowedMutations: 1 };
                }""",
                module_url,
            )
            browser.close()
        if browser_result.get("status") != "PASS":
            raise RuntimeError(f"browser validation failed: {browser_result}")

        return {
            "label": label,
            "source": str(source),
            "typeCheck": typecheck,
            "nodeCompile": compile_node,
            "nodeSyntaxCheck": node_check,
            "nodeRuntime": node_runtime,
            "browserCompile": compile_browser,
            "chromiumRuntime": browser_result,
        }


def validate(repository_root: Path) -> dict[str, Any]:
    tsc = shutil.which("tsc")
    node = shutil.which("node")
    if not tsc or not node:
        raise RuntimeError("node and tsc are required")
    node_version = run([node, "--version"])
    tsc_version = run([tsc, "--version"])
    results = []
    for label, relative in FRONTENDS.items():
        source = repository_root / relative
        if not source.is_file():
            raise FileNotFoundError(f"required source missing: {relative}")
        results.append(validate_one(label, source, tsc, node))
    return {
        "status": "PASS",
        "repositoryRoot": str(repository_root),
        "nodeVersion": node_version,
        "typescriptVersion": tsc_version,
        "playwrightPythonVersion": importlib.metadata.version("playwright"),
        "validatedFrontends": results,
        "scope": "ADM/BZA shared cpfApi actor trust boundary; strict type check, Node unit-style runtime, Chromium browser runtime",
        "repositoryBuildOutputsCreated": False,
    }


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--repository-root", type=Path, default=Path.cwd())
    parser.add_argument("--output", type=Path)
    args = parser.parse_args()
    result = validate(args.repository_root.resolve())
    payload = json.dumps(result, ensure_ascii=False, indent=2) + "\n"
    if args.output:
        args.output.parent.mkdir(parents=True, exist_ok=True)
        args.output.write_text(payload, encoding="utf-8")
    print(payload, end="")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
