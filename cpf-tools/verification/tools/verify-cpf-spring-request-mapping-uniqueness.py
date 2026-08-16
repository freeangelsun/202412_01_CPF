#!/usr/bin/env python3
"""Detect duplicate Spring MVC request mappings across product controllers.

The gate is intentionally source based so it can run when a full Spring
ApplicationContext is unavailable.  It scans concrete ``*Controller.java``
classes, combines class and method mappings, and fails when two handlers claim
the same HTTP method and normalized path.  A full Context test remains required,
but this gate prevents duplicate handlers from being hidden by isolated stubs.
"""
from __future__ import annotations

import argparse
import json
import re
from collections import defaultdict
from pathlib import Path
from typing import NamedTuple

CONTROLLER_RE = re.compile(r"\bclass\s+([A-Za-z_$][\w$]*)")
CLASS_MAPPING_RE = re.compile(r"@RequestMapping\s*\((.*?)\)\s*(?:@[\w.]+(?:\([^)]*\))?\s*)*(?:public\s+)?(?:final\s+)?class\b", re.S)
METHOD_MAPPING_RE = re.compile(
    r"@(Get|Post|Put|Patch|Delete|Request)Mapping\s*(?:\((.*?)\))?\s*"
    r"(?:@[\w.]+(?:\([^)]*\))?\s*)*"
    r"(?:public|protected|private)\s+[\w<>, ?\[\].@]+\s+([A-Za-z_$][\w$]*)\s*\(",
    re.S,
)
STRING_RE = re.compile(r'"((?:\\.|[^"\\])*)"')
REQUEST_METHOD_RE = re.compile(r"RequestMethod\.([A-Z]+)")


class MappingError(RuntimeError):
    pass


class Handler(NamedTuple):
    method: str
    path: str
    file: str
    class_name: str
    handler_name: str


def strip_comments(text: str) -> str:
    text = re.sub(r"/\*.*?\*/", "", text, flags=re.S)
    return re.sub(r"//[^\n]*", "", text)


def strings(arguments: str | None) -> list[str]:
    if not arguments:
        return [""]
    values = [bytes(value, "utf-8").decode("unicode_escape") for value in STRING_RE.findall(arguments)]
    # Annotation attributes such as produces="application/json" are not paths.
    path_values: list[str] = []
    for value in values:
        if value == "" or value.startswith("/"):
            path_values.append(value)
            continue
        if value.startswith("${") and value.endswith("}") and ":" in value:
            default_path=value.split(":",1)[1][:-1]
            if default_path.startswith("/"):
                path_values.append(default_path)
    return path_values or [""]


def normalize_path(*parts: str) -> str:
    joined = "/".join(part.strip("/") for part in parts if part.strip("/"))
    path = "/" + joined if joined else "/"
    path = re.sub(r"/+", "/", path)
    # Variable names do not distinguish the runtime mapping: /x/{id} == /x/{name}.
    path = re.sub(r"\{[^}/:]+(?=[:}])", "{var", path)
    return path.rstrip("/") or "/"


def class_paths(text: str) -> list[str]:
    match = CLASS_MAPPING_RE.search(text)
    return strings(match.group(1)) if match else [""]


def request_methods(kind: str, arguments: str | None) -> list[str]:
    if kind != "Request":
        return [kind.upper()]
    methods = REQUEST_METHOD_RE.findall(arguments or "")
    return methods or ["ANY"]


def scan_controller(root: Path, path: Path) -> list[Handler]:
    text = strip_comments(path.read_text(encoding="utf-8-sig", errors="replace"))
    if "@RestController" not in text and "@Controller" not in text:
        return []
    if re.search(r"@interface\s+", text):
        return []
    class_match = CONTROLLER_RE.search(text)
    if not class_match:
        raise MappingError(f"{path.relative_to(root)}: controller class name not found")
    class_name = class_match.group(1)
    bases = class_paths(text)
    handlers: list[Handler] = []
    for match in METHOD_MAPPING_RE.finditer(text, class_match.end()):
        kind, arguments, handler_name = match.groups()
        paths = strings(arguments)
        methods = request_methods(kind, arguments)
        for base in bases:
            for relative in paths:
                for method in methods:
                    handlers.append(Handler(
                        method=method,
                        path=normalize_path(base, relative),
                        file=path.relative_to(root).as_posix(),
                        class_name=class_name,
                        handler_name=handler_name,
                    ))
    return handlers


def verify(root: Path) -> dict[str, object]:
    root = root.resolve()
    controllers = sorted(
        path for path in root.rglob("*Controller.java")
        if path.is_file() and not any(part in {"build", ".gradle", "generated", "testFixtures"} for part in path.parts)
    )
    if not controllers:
        raise MappingError("no product Controller.java files found")
    handlers: list[Handler] = []
    for path in controllers:
        handlers.extend(scan_controller(root, path))
    if not handlers:
        raise MappingError("no Spring MVC request mappings found")

    by_key: dict[tuple[str, str], list[Handler]] = defaultdict(list)
    for handler in handlers:
        by_key[(handler.method, handler.path)].append(handler)
        if handler.method != "ANY":
            by_key[("ANY", handler.path)]  # reserve key for conflict check below

    conflicts: list[dict[str, object]] = []
    seen_conflict_keys: set[tuple[str, str]] = set()
    for (method, path), claimed in sorted(by_key.items()):
        if method == "ANY":
            any_handlers = [h for h in handlers if h.method == "ANY" and h.path == path]
            concrete = [h for h in handlers if h.method != "ANY" and h.path == path]
            combined = any_handlers + concrete
            if any_handlers and len({(h.file, h.handler_name) for h in combined}) > 1:
                key = (method, path)
                if key not in seen_conflict_keys:
                    conflicts.append({"method": "ANY/*", "path": path, "handlers": [h._asdict() for h in combined]})
                    seen_conflict_keys.add(key)
        elif len({(h.file, h.handler_name) for h in claimed}) > 1:
            key = (method, path)
            if key not in seen_conflict_keys:
                conflicts.append({"method": method, "path": path, "handlers": [h._asdict() for h in claimed]})
                seen_conflict_keys.add(key)
    result = {
        "status": "PASS" if not conflicts else "FAIL",
        "controllerFileCount": len(controllers),
        "handlerCount": len(handlers),
        "duplicateMappingCount": len(conflicts),
        "conflicts": conflicts,
        "meaning": "Source mapping uniqueness only; full Spring ApplicationContext remains required",
    }
    if conflicts:
        raise MappingError(json.dumps(result, ensure_ascii=False, indent=2))
    return result


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", default=".")
    parser.add_argument("--json-output")
    args = parser.parse_args()
    try:
        result = verify(Path(args.root)); code = 0
    except Exception as exc:
        try:
            result = json.loads(str(exc))
        except json.JSONDecodeError:
            result = {"status": "FAIL", "message": str(exc)}
        code = 1
    text = json.dumps(result, ensure_ascii=False, indent=2)
    if args.json_output:
        output = Path(args.json_output)
        output = output if output.is_absolute() else Path(args.root).resolve() / output
        output.parent.mkdir(parents=True, exist_ok=True)
        output.write_text(text + "\n", encoding="utf-8")
    print(text)
    return code


if __name__ == "__main__":
    raise SystemExit(main())
