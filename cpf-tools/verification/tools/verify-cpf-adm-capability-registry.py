#!/usr/bin/env python3
"""Fail-closed source gate for the modular ADM Route/Menu/Component capability registry."""
from __future__ import annotations
import argparse, os, re, sys
from pathlib import Path

ENTRY = re.compile(
    r'^\s*"(?P<route_id>[^"]+)": \{ routeId: "(?P=route_id)", path: "(?P<path>[^"]+)", menuId: "(?P<menu_id>[^"]+)".*?'
    r'featureFlag: "(?P<feature_flag>[^"]+)".*?component: defineAsyncComponent\(\(\) => import\("(?P<component>[^"]+)"\)\)',
    re.MULTILINE,
)
GENERATED_ENTRY = re.compile(r'^\s*"(?P<route_id>[^"]+)": \[', re.MULTILINE)


def fail(message: str) -> None:
    print(f"[FAIL] {message}", file=sys.stderr)
    raise SystemExit(1)


def route_sources(root: Path) -> list[Path]:
    route_dir = root / "cpf-admin/frontend/src/app/routes"
    files = sorted(p for p in route_dir.glob("*.ts") if p.name != "types.ts")
    if not files:
        fail(f"ADM modular route registry is empty: {route_dir.relative_to(root)}")
    return files


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    expected_env = os.getenv("CPF_EXPECTED_ADM_ROUTE_COUNT", "").strip()
    parser.add_argument("--expected-routes", type=int, default=int(expected_env) if expected_env else None,
                        help="Optional compatibility assertion; modular route registry cardinality is otherwise authoritative")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    routes = root / "cpf-admin/frontend/src/app/routes.ts"
    generated = root / "cpf-admin/frontend/src/generated/adm-route-operation-contract.ts"
    app = root / "cpf-admin/frontend/src/App.vue"
    router = root / "cpf-admin/frontend/src/app/router.ts"
    for path in (routes, generated, app, router):
        if not path.is_file():
            fail(f"required file missing: {path.relative_to(root)}")

    files = route_sources(root)
    actual: dict[str, dict[str, str]] = {}
    source_file_by_route: dict[str, Path] = {}
    all_text: list[str] = []
    for source_file in files:
        route_text = source_file.read_text(encoding="utf-8")
        all_text.append(route_text)
        for match in ENTRY.finditer(route_text):
            route_id = match.group("route_id")
            if route_id in actual:
                fail(f"duplicate ADM routeId in capability registry: {route_id}")
            actual[route_id] = match.groupdict()
            source_file_by_route[route_id] = source_file
    if not actual:
        fail("ADM modular route registry contains no capability entries")
    if args.expected_routes is not None and len(actual) != args.expected_routes:
        fail(f"route cardinality mismatch expected={args.expected_routes} source={len(actual)}")
    if len({row['path'] for row in actual.values()}) != len(actual):
        fail("duplicate ADM route path")

    generated_ids = set(GENERATED_ENTRY.findall(generated.read_text(encoding="utf-8")))
    if generated_ids != set(actual):
        fail(f"generated route contract drift missing={sorted(set(actual)-generated_ids)} extra={sorted(generated_ids-set(actual))}")

    for route_id, source in actual.items():
        if not source["feature_flag"].startswith("adm.route.") or not source["feature_flag"].endswith(".enabled"):
            fail(f"invalid feature flag contract: {route_id}={source['feature_flag']}")
        component_path = (source_file_by_route[route_id].parent / source["component"]).resolve()
        if not component_path.is_file():
            fail(f"component missing route={route_id} path={source['component']}")

    compact = "\n".join(all_text).replace(" ", "")
    for pattern in ('||"home"', "||'home'", 'return"dashboard"'):
        if pattern in compact:
            fail(f"silent fallback pattern detected in modular route registry: {pattern}")

    app_text = app.read_text(encoding="utf-8")
    if "<RouterView" not in app_text:
        fail("App.vue does not render RouterView")
    if "activeFeatureComponent" in app_text or "componentForMenu(this.activeMenu)" in app_text:
        fail("App.vue still bypasses Vue Router with manual component rendering")

    router_text = router.read_text(encoding="utf-8")
    for status in ("forbidden", "feature-disabled", "lazy-load-failure", "not-found"):
        if f'name: "{status}"' not in router_text:
            fail(f"status route missing: {status}")
    if "admRouter.onError" not in router_text:
        fail("lazy-load failure handler missing")

    print(f"[PASS] ADM capability registry closure: routeFiles={len(files)} routes={len(actual)} generated={len(generated_ids)} statusRoutes=4 silentFallback=0")


if __name__ == "__main__":
    main()
