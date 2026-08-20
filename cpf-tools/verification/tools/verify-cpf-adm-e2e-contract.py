#!/usr/bin/env python3
"""Fail-closed static contract for the current exhaustive ADM browser suite."""
from __future__ import annotations
import argparse, os, re, sys
from pathlib import Path

class ContractError(RuntimeError): pass
ENTRY = re.compile(r'^\s*"(?P<route_id>[^"]+)": \{ routeId: "(?P=route_id)", path: "(?P<path>[^"]+)"', re.MULTILINE)

def require(text: str, token: str, label: str) -> None:
    if token not in text: raise ContractError(f"{label} missing token={token}")

def validate(root: Path) -> dict:
    routes_file = root / "cpf-admin/frontend/src/app/routes.ts"
    routes_dir = root / "cpf-admin/frontend/src/app/routes"
    generated = root / "cpf-admin/frontend/src/generated/adm-route-operation-contract.ts"
    config = root / "cpf-admin/frontend/playwright.config.ts"
    route_spec = root / "cpf-admin/frontend/e2e/adm-route-contract.spec.ts"
    state_spec = root / "cpf-admin/frontend/e2e/adm-route-error-states.spec.ts"
    api = root / "cpf-admin/frontend/src/shared/cpfApi.ts"
    package = root / "cpf-admin/frontend/package.json"
    for path in (routes_file, generated, config, route_spec, state_spec, api, package):
        if not path.is_file(): raise ContractError(f"missing {path.relative_to(root)}")

    route_text = routes_file.read_text(encoding="utf-8")
    if routes_dir.is_dir():
        route_text += "\n" + "\n".join(p.read_text(encoding="utf-8") for p in sorted(routes_dir.glob("*.ts")) if p.name != "types.ts")
    rows = list(ENTRY.finditer(route_text))
    ids = [m.group("route_id") for m in rows]; paths = [m.group("path") for m in rows]
    if not rows: raise ContractError("route registry empty")
    if len(set(ids)) != len(rows) or len(set(paths)) != len(rows): raise ContractError("route id/path duplicates")
    expected_override = os.getenv("CPF_EXPECTED_ADM_ROUTE_COUNT", "").strip()
    if expected_override:
        expected = int(expected_override)
        if len(rows) != expected: raise ContractError(f"route registry drift expected={expected} actual={len(rows)}")
    generated_text = generated.read_text(encoding="utf-8")
    generated_ids = set(re.findall(r'^\s*"([^"]+)": \[', generated_text, re.MULTILINE))
    route_ids = set(ids)
    if generated_ids != route_ids:
        missing = sorted(route_ids - generated_ids)
        stale = sorted(generated_ids - route_ids)
        raise ContractError(f"route/generated registry mismatch missing={missing} stale={stale}")

    config_text = config.read_text(encoding="utf-8")
    required_projects=("chromium-desktop","firefox-desktop","webkit-desktop","chromium-mobile","webkit-mobile")
    for project in required_projects: require(config_text, f'name: "{project}"', "playwright project")
    require(config_text, "CPF_E2E_AUTH_STATE", "authenticated storage state")
    require(config_text, "CPF_E2E_RELEASE", "release fail-closed mode")

    route_contract = route_spec.read_text(encoding="utf-8")
    for token in ('admCapabilityRegistry', 'admRouteOperationContract', 'data-route-id', '.adm-sidebar button.active', 'resolveCpfOperation', 'expectedOperations'):
        require(route_contract, token, "route E2E")
    if "20260801" in route_contract: raise ContractError("route E2E references retired campaign artifact")
    state_text = state_spec.read_text(encoding="utf-8")
    require(state_text, "admCapabilityRegistry", "error-state E2E")
    for status in ("401", "403", "404", "409", "429", "500", "503"): require(state_text, status, "error-state E2E")
    require(state_text, "**/adm/api/**", "backend state interception")
    if "20260801" in state_text: raise ContractError("error-state E2E references retired campaign artifact")

    api_text = api.read_text(encoding="utf-8")
    require(api_text, "resolveCpfOperation", "generated management operation resolution")
    if "X-CPF-Operation-Id" in api_text or "X-Target-Operation-Id" in api_text: raise ContractError("ADM browser must not inject business transaction operation headers")
    package_text = package.read_text(encoding="utf-8")
    for token in ('"test:e2e"', '"test:a11y"'): require(package_text, token, "package browser command")
    return {"routes": len(rows), "browsers": 5, "mandatoryStatuses": 7}

def main() -> int:
    ap = argparse.ArgumentParser(); ap.add_argument("--root", type=Path, default=Path.cwd()); args = ap.parse_args()
    result = validate(args.root.resolve())
    print(f"[PASS] ADM E2E contract routes={result['routes']} browsers={result['browsers']} statuses={result['mandatoryStatuses']}")
    return 0

if __name__ == "__main__":
    try: raise SystemExit(main())
    except ContractError as error:
        print(f"[FAIL] {error}", file=sys.stderr); raise SystemExit(1)
