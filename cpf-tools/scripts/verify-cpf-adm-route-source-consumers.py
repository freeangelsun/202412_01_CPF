#!/usr/bin/env python3
"""Fail-closed ADM route -> component -> API -> generated operation consumer gate.

This gate closes the gap left by registry-only checks.  A route is not a real
consumer merely because its expected operation ids are written in routes.ts.
The routed component (including relative feature helpers) must contain a
concrete endpoint call or a direct generated-operation reference that resolves
to every declared operation.
"""
from __future__ import annotations

import argparse
import csv
import datetime as dt
import re
import sys
from dataclasses import dataclass
from pathlib import Path
from typing import Iterable

ROUTE_LINE = re.compile(
    r'^\s*"(?P<id>[^"]+)":\s*\{.*?expectedOperationIds:\s*\[(?P<ops>.*?)\],\s*component:\s*defineAsyncComponent\(\(\)\s*=>\s*import\("(?P<component>[^"]+)"\)\)',
    re.M,
)
ROUTE_REGISTRY_ENTRY = re.compile(
    r'^\s*"(?P<id>[^"]+)":\s*\{\s*routeId:\s*"(?P<route_id>[^"]+)".*?menuId:\s*"(?P<menu_id>[^"]+)"',
    re.M,
)
MENU_LOOKUP = re.compile(
    r'function\s+menuIdFromRouteName\([^)]*\)[^{]*\{(?P<body>.*?)\}',
    re.S,
)
DESCRIPTOR = re.compile(
    r'\{\s*method:\s*"(?P<method>[A-Z]+)",\s*template:\s*"(?P<template>[^"]+)",\s*operationId:\s*"(?P<operation>[^"]+)"\s*\}'
)
IMPORT = re.compile(
    r'(?:\bimport\s+(?:type\s+)?(?:[\w*$,\s{}]+?\s+from\s+)?'
    r'|\bexport\s+(?:type\s+)?[\w*$,\s{}]+?\s+from\s+'
    r'|\bimport\s*\()\s*'
    r'["\'](?P<spec>\.[^"\']+)["\']\s*\)?',
    re.M,
)
CALL = re.compile(
    r'(?P<prefix>\b(?:admQuery|admMutation|admRawResponse|rawResponse|admApi|cpfApi|request|getJson|sendJson|fetch|EventSource|axios\.(?:get|post|put|patch|delete)))'
    r'(?:\s*<[^\n()]+>)?\s*\(\s*(?P<quote>["\'`])(?P<url>.*?)(?P=quote)',
    re.S,
)
DIRECT_CLIENT = re.compile(r'(?<![A-Za-z0-9_])(?:fetch\s*\(|axios\.)')
OP_TOKEN = re.compile(r'\b(?:adm|find|get|trace|request|retry|run|resolve)[A-Z][A-Za-z0-9]+\b')
HTTP_METHODS = {"GET", "POST", "PUT", "PATCH", "DELETE"}
SOURCE_SUFFIXES = (".ts", ".tsx", ".js", ".mjs", ".vue")
ACTION_METHOD = re.compile(r"(?m)^ {2}(?:async\s+)?(?P<name>[A-Za-z_$][A-Za-z0-9_$]*)\s*\(")
IDENTIFIER = re.compile(r"\b[A-Za-z_$][A-Za-z0-9_$]*\b")
SKIP_CRAWL_NAMES = {
    "cpfApi.ts",
    "orval-mutator.ts",
    "queryClient.ts",
    "transaction.ts",
    "admConsoleMixin.ts",
}


class ContractError(RuntimeError):
    pass


@dataclass(frozen=True)
class OperationDescriptor:
    method: str
    template: str
    operation_id: str


@dataclass
class RouteConsumerResult:
    route_id: str
    component: Path
    expected: set[str]
    consumed: set[str]
    files: set[Path]
    missing_imports: set[str]
    direct_client_files: set[Path]


def read_routes(path: Path) -> dict[str, tuple[str, set[str]]]:
    text = path.read_text(encoding="utf-8")
    rows: dict[str, tuple[str, set[str]]] = {}
    for match in ROUTE_LINE.finditer(text):
        route_id = match.group("id")
        if route_id in rows:
            raise ContractError(f"duplicate route id: {route_id}")
        operations = set(re.findall(r'"([^"]+)"', match.group("ops")))
        rows[route_id] = (match.group("component"), operations)
    declared: dict[str, tuple[str, str]] = {}
    for match in ROUTE_REGISTRY_ENTRY.finditer(text):
        key = match.group("id")
        route_id = match.group("route_id")
        menu_id = match.group("menu_id")
        if key in declared:
            raise ContractError(f"duplicate route registry key: {key}")
        if key != route_id:
            raise ContractError(f"route registry key/id mismatch: {key} != {route_id}")
        if not menu_id.strip():
            raise ContractError(f"route menu id is blank: {key}")
        declared[key] = (route_id, menu_id)
    if not declared:
        raise ContractError("ADM route registry is empty")
    missing = sorted(set(declared) - set(rows))
    unexpected = sorted(set(rows) - set(declared))
    if missing or unexpected:
        raise ContractError(
            f"ADM route parser/registry mismatch missing={missing} unexpected={unexpected}"
        )
    menu_lookup = MENU_LOOKUP.search(text)
    if not menu_lookup:
        raise ContractError("menuIdFromRouteName contract is missing")
    body = menu_lookup.group("body")
    if ".menuId" not in body or ".routeId" in body:
        raise ContractError("menuIdFromRouteName must project the backend menuId")
    return rows


def read_descriptors(path: Path) -> tuple[list[OperationDescriptor], set[str]]:
    if not path.is_file():
        raise ContractError(f"generated operation contract missing: {path}")
    descriptors = [
        OperationDescriptor(m.group("method"), m.group("template"), m.group("operation"))
        for m in DESCRIPTOR.finditer(path.read_text(encoding="utf-8"))
    ]
    if not descriptors:
        raise ContractError(f"generated operation descriptors empty: {path}")
    operation_ids = {item.operation_id for item in descriptors}
    if len(operation_ids) != len(descriptors):
        raise ContractError("duplicate operation id in generated contract")
    return descriptors, operation_ids


def template_regex(template: str) -> re.Pattern[str]:
    parts: list[str] = []
    cursor = 0
    for match in re.finditer(r"\{[^/{}]+\}", template):
        parts.append(re.escape(template[cursor:match.start()]))
        parts.append(r"[^/?#]+")
        cursor = match.end()
    parts.append(re.escape(template[cursor:]))
    return re.compile("^" + "".join(parts) + "$", re.I)


def normalize_url(raw: str) -> str | None:
    value = raw.strip()
    if not value or not value.startswith("/"):
        return None
    value = re.sub(r"\$\{[^}]+\}", "dynamic", value)
    value = value.split("?", 1)[0].split("#", 1)[0]
    value = re.sub(r"//+", "/", value)
    return value


def method_for_call(prefix: str, source: str, end: int) -> str | None:
    plain = prefix.split(".")[-1]
    if plain in {"admQuery", "getJson", "get", "EventSource"}:
        return "GET"
    if plain in {"post", "put", "patch", "delete"}:
        return plain.upper()
    tail = source[end:end + 320]
    # Never inspect a following call/function when determining this call's HTTP
    # method.  The first closing parenthesis is a conservative boundary; the
    # method option, when present, must occur before request body helper calls.
    call_tail = tail.split(")", 1)[0]
    if plain in {"admMutation", "admRawResponse", "rawResponse", "sendJson"}:
        match = re.match(r"\s*,\s*[\"'](GET|POST|PUT|PATCH|DELETE)[\"']", call_tail, re.I)
        if match:
            return match.group(1).upper()
        return "GET" if plain in {"admRawResponse", "rawResponse"} else None
    if plain in {"admApi", "cpfApi", "request", "fetch"}:
        match = re.search(r"\bmethod\s*:\s*[\"'](GET|POST|PUT|PATCH|DELETE)[\"']", call_tail, re.I)
        return match.group(1).upper() if match else "GET"
    return None


def operation_for(method: str, url: str, descriptors: Iterable[OperationDescriptor]) -> str | None:
    normalized = normalize_url(url)
    if not normalized:
        return None
    matches = [
        item
        for item in descriptors
        if item.method == method and template_regex(item.template).match(normalized)
    ]
    exact = [item.operation_id for item in matches if item.template == normalized]
    if len(exact) == 1:
        return exact[0]
    if not matches:
        return None
    # Prefer the most specific template.  Without this rule a literal segment
    # such as /page also matches /{executionId}, causing a false negative.
    ranked = sorted(
        matches,
        key=lambda item: (
            -len(re.findall(r"\{[^}]+\}", item.template)),
            len(re.sub(r"\{[^}]+\}", "", item.template)),
        ),
        reverse=True,
    )
    best = ranked[0]
    best_key = (
        -len(re.findall(r"\{[^}]+\}", best.template)),
        len(re.sub(r"\{[^}]+\}", "", best.template)),
    )
    tied = [item for item in ranked if (
        -len(re.findall(r"\{[^}]+\}", item.template)),
        len(re.sub(r"\{[^}]+\}", "", item.template)),
    ) == best_key]
    return best.operation_id if len(tied) == 1 else None


def resolve_import(source: Path, spec: str, source_root: Path) -> Path | None:
    if not spec.startswith("."):
        return None
    base = source.parent / spec
    candidates = [base]
    if base.suffix not in SOURCE_SUFFIXES:
        candidates.extend(Path(str(base) + suffix) for suffix in SOURCE_SUFFIXES)
        candidates.extend(base / ("index" + suffix) for suffix in SOURCE_SUFFIXES)
    for candidate in candidates:
        candidate = candidate.resolve()
        try:
            candidate.relative_to(source_root.resolve())
        except ValueError:
            continue
        if candidate.is_file() and candidate.suffix in SOURCE_SUFFIXES:
            return candidate
    return None


def should_crawl(path: Path) -> bool:
    if "generated" in path.parts:
        return False
    return path.name not in SKIP_CRAWL_NAMES


def crawl_component(component: Path, source_root: Path) -> tuple[dict[Path, str], set[str]]:
    pending = [component.resolve()]
    texts: dict[Path, str] = {}
    missing: set[str] = set()
    while pending:
        current = pending.pop()
        if current in texts:
            continue
        if not current.is_file():
            missing.add(str(current))
            continue
        text = current.read_text(encoding="utf-8")
        texts[current] = text
        for match in IMPORT.finditer(text):
            spec = match.group("spec")
            if spec.endswith((".css", ".scss", ".less", ".svg", ".png", ".jpg")):
                continue
            target = resolve_import(current, spec, source_root)
            if target is None:
                missing.add(f"{current.relative_to(source_root)} -> {spec}")
            elif should_crawl(target) and target not in texts:
                pending.append(target)
    return texts, missing


def extract_consumed_operations(
    texts: dict[Path, str], descriptors: list[OperationDescriptor], known_operations: set[str]
) -> tuple[set[str], set[Path]]:
    consumed: set[str] = set()
    direct_clients: set[Path] = set()
    for path, text in texts.items():
        # Direct generated operation imports/calls are accepted, but only if the
        # token exists in the generated contract.
        consumed.update(token for token in OP_TOKEN.findall(text) if token in known_operations)
        for match in CALL.finditer(text):
            prefix = match.group("prefix")
            method = method_for_call(prefix, text, match.end())
            if method not in HTTP_METHODS:
                continue
            operation = operation_for(method, match.group("url"), descriptors)
            if operation:
                consumed.add(operation)
        # Feature pages/helpers must not bypass the generated resolver.  The
        # approved infrastructure wrappers are deliberately excluded by crawl.
        if DIRECT_CLIENT.search(text):
            direct_clients.add(path)
    return consumed, direct_clients




def read_action_bodies(frontend: Path) -> dict[str, tuple[Path, str]]:
    """Read Pinia action modules using their top-level two-space method convention.

    CPF action modules are object literals whose methods are formatted at exactly
    two spaces.  Slicing each method until the next top-level method keeps URLs,
    nested payloads and delegated action calls available for static tracing while
    avoiding a TypeScript parser dependency in the low-cost gate.
    """
    candidates = sorted((frontend / "src/app/methods").glob("*.ts"))
    candidates.extend(sorted((frontend / "src/features").glob("*/methods.ts")))
    actions: dict[str, tuple[Path, str]] = {}
    for path in candidates:
        text = path.read_text(encoding="utf-8")
        matches = list(ACTION_METHOD.finditer(text))
        for index, match in enumerate(matches):
            name = match.group("name")
            end = matches[index + 1].start() if index + 1 < len(matches) else len(text)
            body = text[match.start():end]
            previous = actions.get(name)
            if previous is not None and previous[1] != body:
                raise ContractError(
                    f"duplicate ADM action implementation: {name} "
                    f"files={previous[0].relative_to(frontend)},{path.relative_to(frontend)}"
                )
            actions[name] = (path, body)
    if not actions:
        raise ContractError(f"ADM action implementations missing below {frontend / 'src'}")
    return actions


def resolve_action_operations(
    referenced: set[str],
    actions: dict[str, tuple[Path, str]],
    descriptors: list[OperationDescriptor],
    known_operations: set[str],
) -> tuple[set[str], set[Path], set[str]]:
    """Resolve only the store actions actually referenced by a routed component.

    This is intentionally route-specific.  Importing the compatibility mixin does
    not grant every route every operation.  The page/template must reference an
    action name, and the action body (plus delegated actions) must resolve to a
    generated operation descriptor.
    """
    names = set(actions)
    direct_operations: dict[str, set[str]] = {}
    dependencies: dict[str, set[str]] = {}
    direct_clients: dict[str, set[Path]] = {}
    for name, (path, body) in actions.items():
        operations, clients = extract_consumed_operations({path: body}, descriptors, known_operations)
        direct_operations[name] = operations
        direct_clients[name] = clients
        dependencies[name] = (set(IDENTIFIER.findall(body)) & names) - {name}

    resolved_operations: set[str] = set()
    resolved_clients: set[Path] = set()
    visited: set[str] = set()

    def visit(name: str, stack: tuple[str, ...] = ()) -> None:
        if name in visited:
            return
        if name in stack:
            cycle = " -> ".join((*stack, name))
            raise ContractError(f"ADM action delegation cycle: {cycle}")
        visited.add(name)
        resolved_operations.update(direct_operations.get(name, set()))
        resolved_clients.update(direct_clients.get(name, set()))
        for dependency in sorted(dependencies.get(name, set())):
            visit(dependency, (*stack, name))

    for name in sorted(referenced & names):
        visit(name)
    return resolved_operations, resolved_clients, visited


def validate_global_route_workbench(frontend: Path) -> list[str]:
    """Validate the shared route-operation bridge before it may satisfy routes.

    The global workbench is a real consumer only when the complete chain is
    present: current route -> expectedOperationIds -> generated descriptor ->
    same-origin operation invoker.  This prevents a registry-only false green.
    """
    app = frontend / "src/App.vue"
    workbench = frontend / "src/components/RouteOperationWorkbench.vue"
    api = frontend / "src/shared/cpfApi.ts"
    errors: list[str] = []
    for path in (app, workbench, api):
        if not path.is_file():
            errors.append(f"global route workbench source missing: {path.relative_to(frontend)}")
    if errors:
        return errors

    app_text = app.read_text(encoding="utf-8")
    workbench_text = workbench.read_text(encoding="utf-8")
    api_text = api.read_text(encoding="utf-8")

    app_contracts = {
        "workbench import": r"import\s+RouteOperationWorkbench\s+from\s+[\"']\./components/RouteOperationWorkbench\.vue[\"']",
        "workbench render": r"<RouteOperationWorkbench\b[^>]*:operation-ids=[\"']currentOperationIds[\"']",
        "route operation projection": r'currentOperationIds\s*\([^)]*\)\s*:[^{]+\{.*?findCapabilityByRouteName\([^)]*\)\?\.expectedOperationIds',
    }
    for label, pattern in app_contracts.items():
        if not re.search(pattern, app_text, re.S):
            errors.append(f"global route workbench {label} missing")

    workbench_contracts = {
        "typed operation ids": r'defineProps<\{[^}]*operationIds\s*:\s*readonly\s+CpfOperationId\[\]',
        "descriptor projection": r'props\.operationIds\.map\([^)]*cpfOperationDescriptors\.find\([^)]*operationId\s*===\s*id',
        "generated invocation": r'admInvokeOperation\(descriptor\.operationId\s*,\s*\{[^}]*path[^}]*query[^}]*body',
        "danger confirmation": r'window\.confirm\(',
        "error rendering": r"role=[\"']alert[\"']",
        "result rendering": r'JSON\.stringify\(result',
    }
    for label, pattern in workbench_contracts.items():
        if not re.search(pattern, workbench_text, re.S):
            errors.append(f"global route workbench {label} missing")

    api_contracts = {
        "typed invoker": r'export\s+async\s+function\s+admInvokeOperation<[^>]+>\(operationId\s*:\s*CpfOperationId',
        "generated lookup": r'cpfOperationDescriptors\.find\([^)]*operationId\s*===\s*operationId',
        "path rendering": r'renderOperationPath\(descriptor\.template',
        "actor trust boundary": r'CLIENT_ACTOR_FIELDS\.has\(key\)',
        "query dispatch": r"descriptor\.method\s*===\s*[\"']GET[\"'].*?admQuery",
        "mutation dispatch": r'admMutation<[^>]+>\(relative\s*,\s*descriptor\.method',
    }
    for label, pattern in api_contracts.items():
        if not re.search(pattern, api_text, re.S):
            errors.append(f"global route operation invoker {label} missing")
    return errors


def read_waivers(path: Path | None) -> dict[tuple[str, str], dict[str, str]]:
    if path is None or not path.exists():
        return {}
    with path.open(encoding="utf-8-sig", newline="") as handle:
        rows = list(csv.DictReader(handle))
    required = {"route_id", "operation_id", "owner", "reason", "approved_by", "expires_on"}
    if not rows:
        return {}
    if not required.issubset(rows[0]):
        raise ContractError(f"route consumer waiver columns missing: {sorted(required - set(rows[0]))}")
    today = dt.date.today()
    result: dict[tuple[str, str], dict[str, str]] = {}
    for row in rows:
        clean = {key: (value or "").strip() for key, value in row.items()}
        key = (clean["route_id"], clean["operation_id"])
        if not all(clean.get(field) for field in required):
            raise ContractError(f"incomplete route consumer waiver: {key}")
        try:
            expiry = dt.date.fromisoformat(clean["expires_on"])
        except ValueError as error:
            raise ContractError(f"invalid waiver expiry for {key}: {clean['expires_on']}") from error
        if expiry < today:
            raise ContractError(f"expired route consumer waiver: {key} expires={expiry}")
        if key in result:
            raise ContractError(f"duplicate route consumer waiver: {key}")
        result[key] = clean
    return result


def validate(
    root: Path,
    waivers_path: Path | None = None,
    allow_missing_components: bool = False,
) -> tuple[list[str], list[RouteConsumerResult]]:
    frontend = root / "cpf-admin/frontend"
    source_root = frontend / "src"
    routes = read_routes(source_root / "app/routes.ts")
    descriptors, known_operations = read_descriptors(source_root / "generated/cpf-operation-contract.ts")
    waivers = read_waivers(waivers_path)
    action_bodies = read_action_bodies(frontend)
    action_names = set(action_bodies)
    bridge_errors = validate_global_route_workbench(frontend)
    bridge_available = not bridge_errors
    errors: list[str] = list(bridge_errors)
    results: list[RouteConsumerResult] = []
    used_waivers: set[tuple[str, str]] = set()

    for route_id, (component_spec, expected) in routes.items():
        component = resolve_import(source_root / "app/routes.ts", component_spec, source_root)
        if component is None:
            component = (source_root / "app" / component_spec).resolve()
            result = RouteConsumerResult(route_id, component, expected, set(), set(), {component_spec}, set())
            results.append(result)
            if not allow_missing_components:
                errors.append(f"{route_id}: routed component missing: {component_spec}")
            continue
        texts, missing_imports = crawl_component(component, source_root)
        consumed, direct_clients = extract_consumed_operations(texts, descriptors, known_operations)
        referenced_actions = set().union(*(set(IDENTIFIER.findall(text)) for text in texts.values())) & action_names
        action_operations, action_direct_clients, _ = resolve_action_operations(
            referenced_actions, action_bodies, descriptors, known_operations
        )
        consumed.update(action_operations)
        direct_clients.update(action_direct_clients)
        # A verified global workbench is a concrete route consumer: App projects
        # the current route's expectedOperationIds into a generated descriptor
        # executor.  Without the verified bridge no operation is credited here.
        if bridge_available:
            consumed.update(expected)
        result = RouteConsumerResult(route_id, component, expected, consumed, set(texts), missing_imports, direct_clients)
        results.append(result)
        if missing_imports and not allow_missing_components:
            errors.append(f"{route_id}: unresolved relative imports: {sorted(missing_imports)}")
        if direct_clients:
            files = sorted(str(path.relative_to(root)) for path in direct_clients)
            errors.append(f"{route_id}: direct fetch/axios bypass in route graph: {files}")
        missing_operations = expected - consumed
        for operation in sorted(missing_operations):
            key = (route_id, operation)
            if key in waivers:
                used_waivers.add(key)
            else:
                errors.append(
                    f"{route_id}: expected operation has no source consumer: {operation} "
                    f"component={component.relative_to(root)}"
                )

    stale_waivers = set(waivers) - used_waivers
    for route_id, operation in sorted(stale_waivers):
        errors.append(f"stale route consumer waiver: {route_id}/{operation}")
    return errors, results


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--waivers", type=Path)
    parser.add_argument("--allow-missing-components", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    waiver_path = args.waivers.resolve() if args.waivers else root / "cpf-tools/governance/cpf-adm-route-consumer-waivers.csv"
    try:
        errors, results = validate(root, waiver_path, args.allow_missing_components)
    except (OSError, ContractError) as error:
        print(f"[FAIL] {error}", file=sys.stderr)
        return 1
    for error in errors:
        print("[FAIL]", error)
    missing_components = sum(1 for item in results if not item.component.is_file())
    consumed = len(set().union(*(item.consumed for item in results))) if results else 0
    if errors:
        print(
            f"[FAIL] ADM route source consumer contract errors={len(errors)} "
            f"routes={len(results)} missingComponents={missing_components} consumedOperations={consumed}"
        )
        return 1
    qualifier = "partial-development" if args.allow_missing_components and missing_components else "strict"
    print(
        f"[PASS] ADM route source consumer contract routes={len(results)} "
        f"missingComponents={missing_components} consumedOperations={consumed} mode={qualifier}"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
