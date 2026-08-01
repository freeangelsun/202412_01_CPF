#!/usr/bin/env python3
"""Fail-closed source gate for ADM Route/Menu/Component capability closure."""
from __future__ import annotations
import argparse, csv, re, sys
from pathlib import Path

ENTRY = re.compile(
    r'^\s*"(?P<route_id>[^"]+)": \{ routeId: "(?P=route_id)", path: "(?P<path>[^"]+)", menuId: "(?P<menu_id>[^"]+)".*?'
    r'featureFlag: "(?P<feature_flag>[^"]+)".*?component: defineAsyncComponent\(\(\) => import\("(?P<component>[^"]+)"\)\)',
    re.MULTILINE,
)


def fail(message: str) -> None:
    print(f"[FAIL] {message}", file=sys.stderr)
    raise SystemExit(1)


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    args = parser.parse_args()
    root = Path(args.root).resolve()
    routes = root / "cpf-admin/frontend/src/app/routes.ts"
    baseline = root / "cpf-docs/quality/CPF_20260801_QA36_ADM_CURRENT_59_ROUTE_BASELINE.csv"
    app = root / "cpf-admin/frontend/src/App.vue"
    router = root / "cpf-admin/frontend/src/app/router.ts"
    for path in (routes, baseline, app, router):
        if not path.is_file():
            fail(f"required file missing: {path.relative_to(root)}")

    route_text = routes.read_text(encoding="utf-8")
    actual = {m.group("route_id"): m.groupdict() for m in ENTRY.finditer(route_text)}
    with baseline.open(encoding="utf-8-sig", newline="") as handle:
        expected_rows = list(csv.DictReader(handle))
    expected = {row["route_id"]: row for row in expected_rows}
    if len(expected) != 59 or len(actual) != 59:
        fail(f"route cardinality mismatch: baseline={len(expected)} source={len(actual)}")
    if set(expected) != set(actual):
        fail(f"route identity mismatch: missing={sorted(set(expected)-set(actual))} extra={sorted(set(actual)-set(expected))}")
    for route_id, row in expected.items():
        source = actual[route_id]
        if source["path"] != row["path"]:
            fail(f"path mismatch {route_id}: {source['path']} != {row['path']}")
        if source["menu_id"] != row["menu_id"]:
            fail(f"menu mismatch {route_id}: {source['menu_id']} != {row['menu_id']}")
        if source["feature_flag"] != f"adm.route.{route_id}.enabled":
            fail(f"feature flag mismatch: {route_id}")
        if not source["component"].endswith(row["component"]):
            fail(f"component mismatch {route_id}: {source['component']} != {row['component']}")

    forbidden_patterns = (
        '|| "home"',
        "||'home'",
        "||admCapabilityRegistry.dashboard.component",
        "|| admCapabilityRegistry.dashboard.component",
        'return "dashboard"',
    )
    for pattern in forbidden_patterns:
        if pattern in route_text.replace(" ", "") or pattern in route_text:
            fail(f"silent fallback pattern detected in routes.ts: {pattern}")

    app_text = app.read_text(encoding="utf-8")
    if "<RouterView" not in app_text:
        fail("App.vue does not render RouterView")
    if 'activeFeatureComponent' in app_text or 'componentForMenu(this.activeMenu)' in app_text:
        fail("App.vue still bypasses Vue Router with manual component rendering")
    if 'replace({ name: this.activeMenu })' in app_text:
        fail("App.vue still replaces status/unknown routes with active menu")

    router_text = router.read_text(encoding="utf-8")
    for status in ("forbidden", "feature-disabled", "lazy-load-failure", "not-found"):
        if f'name: "{status}"' not in router_text:
            fail(f"status route missing: {status}")
    if "admRouter.onError" not in router_text:
        fail("lazy-load failure handler missing")

    print(f"[PASS] ADM capability registry closure: routes={len(actual)} statusRoutes=4 silentFallback=0")


if __name__ == "__main__":
    main()
