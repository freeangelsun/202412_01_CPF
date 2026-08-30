#!/usr/bin/env python3
"""Generate a deterministic pre-runtime OpenAPI and compatibility client from Java controllers.

The pre-runtime artifact is compilation support only. Release verification still requires a
BACKEND_RUNTIME OpenAPI export. Unlike the legacy writer, this parser preserves path/query/header
and request-body contracts so Orval cannot silently generate body-less or parameter-less clients.
"""
from __future__ import annotations

import argparse
import hashlib
import json
import re
import sys
from collections import Counter
from pathlib import Path
from typing import Any

HTTP_MAPPING = re.compile(r'@(Get|Post|Put|Patch|Delete)Mapping\s*(?:\((.*?)\))?', re.S)
REQUEST_MAPPING = re.compile(r'@RequestMapping\s*\((.*?)\)', re.S)
OPERATION = re.compile(r'@Operation\s*\((.*?)\)', re.S)
OP_ID = re.compile(r'operationId\s*=\s*"([^"]+)"')
ERROR_DESCRIPTIONS = {
    "400": "요청 형식/검증 실패", "401": "인증 필요", "403": "권한 부족",
    "404": "대상 없음", "409": "상태/동시성 충돌", "422": "Validation or semantic request failure",
    "429": "호출 제한 초과", "500": "내부 처리 실패", "503": "일시적 서비스 불가",
}
MUTATION_METHODS = {"POST", "PUT", "PATCH", "DELETE"}
QUOTED = re.compile(r'"([^"]*)"')
PATH_PARAMETER = re.compile(r'\{([^{}]+)\}')
METHOD_DECL = re.compile(
    r'(?:(?:public|protected|private)\s+)?(?:static\s+)?(?:final\s+)?'
    r'(?:<[^>]+>\s*)?[\w<>, ?\[\].]+\s+(\w+)\s*\('
)
ANNOTATION_NAMES = ("PathVariable", "RequestParam", "RequestHeader", "RequestBody", "RequestPart")
SERVER_DERIVED_REQUEST_FIELDS = {
    "requestUser", "operator", "createdBy", "updatedBy", "authenticatedUser"
}
MODULES = {
    "ADM": ("cpf-admin/src/main/java", "/adm/api/", "CPF ADM"),
    "BZA": ("cpf-backoffice/online/src/main/java", "/api/v1/backoffice/", "CPF BZA (retired compatibility only)"),
    "MBW": ("cpf-backoffice/online/src/main/java", "/api/v1/backoffice/", "CPF Backoffice (MBW)"),
}


class ContractError(RuntimeError):
    pass




def annotation_blocks(text: str, annotation: str) -> list[str]:
    """Return balanced argument blocks for repeated Java annotations."""
    blocks: list[str] = []
    pattern = re.compile(rf"@{re.escape(annotation)}\b")
    for match in pattern.finditer(text):
        cursor = match.end()
        while cursor < len(text) and text[cursor].isspace():
            cursor += 1
        if cursor < len(text) and text[cursor] == "(":
            end = find_matching(text, cursor)
            blocks.append(text[cursor + 1:end])
        else:
            blocks.append("")
    return blocks


def annotation_named_values(text: str, annotation: str, name: str) -> list[str]:
    values: list[str] = []
    for block in annotation_blocks(text, annotation):
        value = named_argument(block, name)
        if value is not None:
            values.append(value)
    return values


def explicit_api_responses(text: str) -> dict[str, str]:
    responses: dict[str, str] = {}
    for block in annotation_blocks(text, "ApiResponse"):
        code = named_argument(block, "responseCode")
        if not code or not re.fullmatch(r"(?:default|[1-5]\d\d)", code):
            continue
        description = named_argument(block, "description") or ERROR_DESCRIPTIONS.get(code, "Controller-declared response")
        responses[code] = description
    return responses


def standard_error_statuses(module: str, method: str, template: str) -> list[str]:
    statuses = ["400", "401", "403", "429", "500", "503"]
    if "{" in template:
        statuses.append("404")
    if method in MUTATION_METHODS:
        statuses.append("409")
        if module in {"BZA", "MBW"}:
            statuses.append("422")
    return statuses


def error_response(status: str, description: str | None = None) -> dict[str, Any]:
    return {
        "description": description or ERROR_DESCRIPTIONS.get(status, "Controller error response"),
        "content": {"application/json": {"schema": {"$ref": "#/components/schemas/CpfApiError"}}},
    }


def annotation_path(arguments: str | None) -> str:
    if not arguments:
        return ""
    match = QUOTED.search(arguments)
    return match.group(1) if match else ""


def normalize(base: str, sub: str) -> str:
    parts = [value.strip("/") for value in (base, sub) if value and value.strip("/")]
    return "/" + "/".join(parts) if parts else "/"


def class_base(text: str) -> str:
    index = text.find("class ")
    prefix = text[: index if index >= 0 else len(text)]
    values = list(REQUEST_MAPPING.finditer(prefix))
    return annotation_path(values[-1].group(1)) if values else ""


def find_matching(text: str, start: int, opening: str = "(", closing: str = ")") -> int:
    if start >= len(text) or text[start] != opening:
        raise ContractError(f"balanced parse start mismatch at {start}")
    depth = 0
    quote: str | None = None
    escaped = False
    for index in range(start, len(text)):
        char = text[index]
        if quote:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
            continue
        if char in ('"', "'"):
            quote = char
        elif char == opening:
            depth += 1
        elif char == closing:
            depth -= 1
            if depth == 0:
                return index
    raise ContractError(f"unbalanced {opening}{closing} starting at {start}")


def split_top_level(value: str, delimiter: str = ",") -> list[str]:
    result: list[str] = []
    start = 0
    depths = {"(": 0, "<": 0, "[": 0, "{": 0}
    pairs = {")": "(", ">": "<", "]": "[", "}": "{"}
    quote: str | None = None
    escaped = False
    for index, char in enumerate(value):
        if quote:
            if escaped:
                escaped = False
            elif char == "\\":
                escaped = True
            elif char == quote:
                quote = None
            continue
        if char in ('"', "'"):
            quote = char
        elif char in depths:
            depths[char] += 1
        elif char in pairs and depths[pairs[char]] > 0:
            depths[pairs[char]] -= 1
        elif char == delimiter and all(depth == 0 for depth in depths.values()):
            result.append(value[start:index].strip())
            start = index + 1
    tail = value[start:].strip()
    if tail:
        result.append(tail)
    return result


def annotation_arguments(parameter: str, annotation: str) -> tuple[str | None, tuple[int, int] | None]:
    match = re.search(rf"@{annotation}\b", parameter)
    if not match:
        return None, None
    index = match.end()
    while index < len(parameter) and parameter[index].isspace():
        index += 1
    if index < len(parameter) and parameter[index] == "(":
        end = find_matching(parameter, index)
        return parameter[index + 1 : end], (match.start(), end + 1)
    return "", (match.start(), match.end())


def strip_annotations(parameter: str) -> str:
    result = parameter
    while True:
        match = re.search(r"@[\w.]+", result)
        if not match:
            break
        end = match.end()
        while end < len(result) and result[end].isspace():
            end += 1
        if end < len(result) and result[end] == "(":
            end = find_matching(result, end) + 1
        result = result[: match.start()] + " " + result[end:]
    return re.sub(r"\s+", " ", result).strip()


def named_argument(arguments: str, name: str) -> str | None:
    match = re.search(rf"\b{name}\s*=\s*\"([^\"]*)\"", arguments)
    return match.group(1) if match else None


def positional_string_argument(arguments: str) -> str | None:
    first = split_top_level(arguments)[0] if arguments.strip() else ""
    match = re.fullmatch(r'\s*"([^"]*)"\s*', first)
    return match.group(1) if match else None


def boolean_argument(arguments: str, name: str, default: bool) -> bool:
    match = re.search(rf"\b{name}\s*=\s*(true|false)", arguments, re.I)
    return match.group(1).lower() == "true" if match else default


def normalize_java_type(java_type: str) -> str:
    value = re.sub(r"\s+", "", java_type)
    value = value.replace("?extends", "").replace("?super", "")
    # Preserve generic structure while removing package qualification from each type token.
    return re.sub(r"(?<![\w$])(?:[a-z_][\w$]*\.)+([A-Za-z_$][\w$]*)", r"\1", value)


def generic_component_name(java_type: str) -> str:
    """Convert arbitrary Java generic DTO types to stable OpenAPI component identifiers."""
    simple = normalize_java_type(java_type).replace("$", ".")
    match = re.fullmatch(r"([A-Za-z_$][\w$]*)<(.+)>", simple)
    if not match:
        return re.sub(r"[^A-Za-z0-9_$]", "_", simple)
    base, arguments = match.groups()
    parts = [generic_component_name(value) for value in split_top_level(arguments)]
    return base + "Of" + "And".join(parts)


def java_schema(java_type: str) -> dict[str, Any]:
    simple = normalize_java_type(java_type).replace("$", ".")
    # Java nested DTO references use the nested simple name in the component inventory.
    if re.fullmatch(r"(?:[A-Za-z_$][\w$]*\.)+[A-Za-z_$][\w$]*", simple):
        simple = simple.split(".")[-1]
    wrappers = {
        "String": {"type": "string"},
        "char": {"type": "string"},
        "Character": {"type": "string"},
        "UUID": {"type": "string", "format": "uuid"},
        "Instant": {"type": "string", "format": "date-time"},
        "OffsetDateTime": {"type": "string", "format": "date-time"},
        "ZonedDateTime": {"type": "string", "format": "date-time"},
        "LocalDateTime": {"type": "string", "format": "date-time"},
        "LocalDate": {"type": "string", "format": "date"},
        "boolean": {"type": "boolean"},
        "Boolean": {"type": "boolean"},
        "byte": {"type": "integer", "format": "int32"},
        "Byte": {"type": "integer", "format": "int32"},
        "short": {"type": "integer", "format": "int32"},
        "Short": {"type": "integer", "format": "int32"},
        "int": {"type": "integer", "format": "int32"},
        "Integer": {"type": "integer", "format": "int32"},
        "long": {"type": "integer", "format": "int64"},
        "Long": {"type": "integer", "format": "int64"},
        "float": {"type": "number", "format": "float"},
        "Float": {"type": "number", "format": "float"},
        "double": {"type": "number", "format": "double"},
        "Double": {"type": "number", "format": "double"},
        "BigDecimal": {"type": "number"},
        "Object": {},
        "MultipartFile": {"type": "string", "format": "binary"},
    }
    if simple in wrappers:
        return dict(wrappers[simple])
    optional = re.fullmatch(r"Optional<(.+)>", simple)
    if optional:
        return java_schema(optional.group(1))
    array = re.fullmatch(r"(.+)\[\]", simple)
    if array:
        return {"type": "array", "items": java_schema(array.group(1))}
    collection = re.fullmatch(r"(?:List|Set|Collection)<(.+)>", simple)
    if collection:
        return {"type": "array", "items": java_schema(collection.group(1))}
    mapping = re.fullmatch(r"Map<[^,>]+,(.+)>", simple)
    if mapping:
        return {"type": "object", "additionalProperties": java_schema(mapping.group(1))}
    component = generic_component_name(simple)
    return {"$ref": f"#/components/schemas/{component}"}




def apply_validation_constraints(schema: dict[str, Any], annotations: str) -> dict[str, Any]:
    result = dict(schema)
    if re.search(r"@NotBlank\b", annotations) and result.get("type") == "string":
        result["minLength"] = max(1, int(result.get("minLength", 0)))
    size = re.search(r"@Size\s*\(([^)]*)\)", annotations)
    if size:
        args = size.group(1)
        min_m = re.search(r"\bmin\s*=\s*(\d+)", args)
        max_m = re.search(r"\bmax\s*=\s*(\d+)", args)
        if result.get("type") == "string":
            if min_m: result["minLength"] = max(int(min_m.group(1)), int(result.get("minLength", 0)))
            if max_m: result["maxLength"] = int(max_m.group(1))
        elif result.get("type") == "array":
            if min_m: result["minItems"] = int(min_m.group(1))
            if max_m: result["maxItems"] = int(max_m.group(1))
    return result

def default_value_schema(schema: dict[str, Any], value: str) -> dict[str, Any]:
    result = dict(schema)
    if value in ("", "\n\t\t\n\t\t\n\ue000\ue001\ue002\n\t\t\t\t\n"):
        return result
    try:
        if result.get("type") == "integer":
            result["default"] = int(value)
        elif result.get("type") == "number":
            result["default"] = float(value)
        elif result.get("type") == "boolean":
            result["default"] = value.lower() == "true"
        else:
            result["default"] = value
    except ValueError:
        result["default"] = value
    return result


def parameter_contract(raw: str) -> dict[str, Any] | None:
    annotation = next((name for name in ANNOTATION_NAMES if re.search(rf"@{name}\b", raw)), None)
    if not annotation:
        return None
    arguments, _ = annotation_arguments(raw, annotation)
    arguments = arguments or ""
    declaration = strip_annotations(raw)
    declaration = re.sub(r"\b(final|volatile|transient)\b", " ", declaration)
    declaration = re.sub(r"\s+", " ", declaration).strip()
    match = re.match(r"(.+?)\s+([A-Za-z_$][\w$]*)$", declaration)
    if not match:
        raise ContractError(f"cannot parse controller parameter: {raw}")
    java_type, variable = match.groups()
    name = named_argument(arguments, "name") or named_argument(arguments, "value")
    if not name:
        name = positional_string_argument(arguments) or variable
    if annotation != "PathVariable" and (name in SERVER_DERIVED_REQUEST_FIELDS or variable in SERVER_DERIVED_REQUEST_FIELDS):
        # Authenticated identity and audit actor values are resolved by the server trust boundary.
        return None
    if annotation == "RequestHeader" and (name.lower() == "authorization" or variable.lower() == "authorization"):
        # Browser clients never own Bearer credentials. The same-origin ADM/BZA BFF resolves the
        # authenticated session and injects the backend Authorization header at the trust boundary.
        return None
    if annotation == "RequestBody":
        return {
            "kind": "body",
            "required": boolean_argument(arguments, "required", True),
            "schema": java_schema(java_type),
            "javaType": java_type,
        }
    if annotation == "RequestPart":
        normalized_type = normalize_java_type(java_type)
        schema = {"type": "string", "format": "binary"} if normalized_type == "MultipartFile" else java_schema(java_type)
        return {
            "kind": "part",
            "name": name,
            "required": boolean_argument(arguments, "required", True),
            "schema": schema,
            "javaType": java_type,
        }
    location = {"PathVariable": "path", "RequestParam": "query", "RequestHeader": "header"}[annotation]
    default_value = named_argument(arguments, "defaultValue")
    required_default = location == "path" or default_value is None
    required = boolean_argument(arguments, "required", required_default)
    if default_value is not None:
        required = False
    schema = java_schema(java_type)
    if "$ref" in schema:
        # Spring MVC path/query/header values are scalar text before conversion. A pre-runtime
        # contract must not expose an unresolved DTO reference for enum/value-object parameters.
        schema = {"type": "string", "x-cpf-java-type": normalize_java_type(java_type)}
    if default_value is not None:
        schema = default_value_schema(schema, default_value)
    return {"kind": "parameter", "name": name, "in": location, "required": required, "schema": schema}


def method_signature(text: str, start: int) -> tuple[str, str, int, str]:
    # Mapping annotations are followed by zero or more method-level annotations.  Skip those
    # annotations structurally so identifiers or strings inside annotations (for example
    # @PreAuthorize("hasAuthority(...)")) can never be mistaken for the Java handler method.
    cursor = start
    while True:
        while cursor < len(text) and text[cursor].isspace():
            cursor += 1
        if text.startswith("//", cursor):
            newline = text.find("\n", cursor + 2)
            cursor = len(text) if newline < 0 else newline + 1
            continue
        if text.startswith("/*", cursor):
            closing = text.find("*/", cursor + 2)
            if closing < 0:
                raise ContractError("unterminated controller comment after mapping")
            cursor = closing + 2
            continue
        if cursor >= len(text) or text[cursor] != "@":
            break
        annotation = re.match(r"@[\w.]+", text[cursor:])
        if not annotation:
            break
        cursor += annotation.end()
        while cursor < len(text) and text[cursor].isspace():
            cursor += 1
        if cursor < len(text) and text[cursor] == "(":
            cursor = find_matching(text, cursor) + 1
    declaration = METHOD_DECL.match(text, cursor)
    if not declaration:
        raise ContractError("controller method declaration missing after mapping")
    opening = declaration.end() - 1
    closing = find_matching(text, opening)
    declaration_prefix = text[cursor:declaration.start(1)].strip()
    declaration_prefix = re.sub(
        r"^(?:(?:public|protected|private|static|final|synchronized|abstract|default|native|strictfp)\s+)+",
        "",
        declaration_prefix,
    )
    declaration_prefix = re.sub(r"^<[^>]+>\s*", "", declaration_prefix).strip()
    return declaration.group(1), text[opening + 1 : closing], closing, declaration_prefix



def response_java_type(java_type: str) -> str:
    normalized = normalize_java_type(java_type)
    for wrapper in ("ResponseEntity", "HttpEntity"):
        match = re.fullmatch(rf"{wrapper}<(.+)>", normalized)
        if match:
            normalized = match.group(1)
            break
    return normalized


def response_schema(java_type: str) -> dict[str, Any]:
    normalized = response_java_type(java_type)
    if normalized in {"void", "Void"}:
        return {"type": "object", "additionalProperties": False, "description": "No response body"}
    return java_schema(normalized)

def record_schemas(text: str, *, strip_server_derived: bool = False) -> dict[str, dict[str, Any]]:
    schemas: dict[str, dict[str, Any]] = {}
    for match in re.finditer(r"\brecord\s+([A-Za-z_$][\w$]*)\s*\(", text):
        name = match.group(1)
        opening = match.end() - 1
        closing = find_matching(text, opening)
        properties: dict[str, Any] = {}
        required: list[str] = []
        for component in split_top_level(text[opening + 1 : closing]):
            declaration = strip_annotations(component)
            field = re.match(r"(.+?)\s+([A-Za-z_$][\w$]*)$", declaration.strip())
            if not field:
                continue
            java_type, field_name = field.groups()
            if strip_server_derived and field_name in SERVER_DERIVED_REQUEST_FIELDS:
                continue
            properties[field_name] = apply_validation_constraints(java_schema(java_type), component)
            if (java_type.strip() in {"boolean", "byte", "short", "int", "long", "float", "double"}
                    or re.search(r"@(NotNull|NotBlank|NotEmpty)\b", component)):
                required.append(field_name)
        schemas[name] = {
            "type": "object",
            "additionalProperties": False,
            **({"required": required} if required else {}),
            "properties": properties,
            "description": "Controller-source pre-runtime request schema. Authenticated operator fields are server-derived.",
        }
    return schemas


def class_field_schemas(text: str, *, strip_server_derived: bool = False) -> dict[str, dict[str, Any]]:
    schemas: dict[str, dict[str, Any]] = {}
    class_match = re.search(r"\b(?:public\s+)?(?:final\s+)?class\s+([A-Za-z_$][\w$]*)", text)
    if not class_match:
        return schemas
    class_name = class_match.group(1)
    properties: dict[str, Any] = {}
    required: list[str] = []
    for match in re.finditer(
            r"(?m)^\s*((?:@[\w.]+(?:\([^\n]*\))?\s*)*)private\s+(?:final\s+)?([^;=]+?)\s+([A-Za-z_$][\w$]*)\s*(?:=[^;]*)?;",
            text):
        annotations, java_type, field_name = match.groups()
        if strip_server_derived and field_name in SERVER_DERIVED_REQUEST_FIELDS:
            continue
        properties[field_name] = apply_validation_constraints(java_schema(java_type.strip()), annotations)
        if (java_type.strip() in {"boolean", "byte", "short", "int", "long", "float", "double"}
                or re.search(r"@(NotNull|NotBlank|NotEmpty)\b", annotations)):
            required.append(field_name)
    if properties:
        schemas[class_name] = {
            "type": "object", "additionalProperties": False,
            **({"required": required} if required else {}), "properties": properties,
            "description": "Controller-source pre-runtime DTO schema. Authenticated operator fields are server-derived.",
        }
    return schemas


def discover_schemas(source: Path) -> dict[str, dict[str, Any]]:
    schemas: dict[str, dict[str, Any]] = {}
    dto_suffixes = ("Request", "Response", "Dto", "Command", "Query")
    for file in sorted(source.rglob("*.java")):
        text = file.read_text(encoding="utf-8", errors="ignore")
        relative_parts = set(file.relative_to(source).parts)
        is_controller = "@RestController" in text or "@CpfController" in text or "@CpfRestController" in text
        if "dto" in relative_parts or file.stem.endswith(dto_suffixes):
            request_contract = file.stem.endswith(("Request", "Command"))
            schemas.update(class_field_schemas(text, strip_server_derived=request_contract))
        # Controller-local records are operation-scoped later so equal simple names cannot collide.
        if not is_controller:
            request_contract = file.stem.endswith(("Request", "Command"))
            schemas.update(record_schemas(text, strip_server_derived=request_contract))
    return schemas


def schema_references(value: Any) -> set[str]:
    found: set[str] = set()
    if isinstance(value, dict):
        ref = value.get("$ref")
        if isinstance(ref, str) and ref.startswith("#/components/schemas/"):
            found.add(ref.rsplit("/", 1)[-1])
        for child in value.values():
            found.update(schema_references(child))
    elif isinstance(value, list):
        for child in value:
            found.update(schema_references(child))
    return found


def discover(root: Path, module: str) -> tuple[list[dict[str, Any]], dict[str, dict[str, Any]]]:
    source_rel, prefix, _ = MODULES[module]
    records: list[dict[str, Any]] = []
    source = root / source_rel
    if not source.exists():
        raise ContractError(f"{module} source missing: {source}")
    schemas = discover_schemas(source)
    referenced_schemas: set[str] = set()
    for file in sorted(source.rglob("*.java")):
        text = file.read_text(encoding="utf-8", errors="ignore")
        if "@RestController" not in text and "@CpfController" not in text and "@CpfRestController" not in text:
            continue
        base = class_base(text)
        class_index = text.find("class ")
        class_annotation_scope = text[:class_index if class_index >= 0 else 0]
        class_security = annotation_named_values(class_annotation_scope, "SecurityRequirement", "name")
        local_record_schemas = record_schemas(text, strip_server_derived=True)
        for mapping in HTTP_MAPPING.finditer(text):
            method = {"Get": "GET", "Post": "POST", "Put": "PUT", "Patch": "PATCH", "Delete": "DELETE"}[mapping.group(1)]
            path = normalize(base, annotation_path(mapping.group(2)))
            if not path.startswith(prefix):
                continue
            _, parameters_text, signature_end, return_type = method_signature(text, mapping.end())
            annotation_scope = text[mapping.end() : signature_end]
            # Java annotation order is semantically irrelevant. @Hidden may appear before or
            # after the Spring mapping annotation, so inspect the contiguous declaration prefix
            # since the previous member boundary as well as the mapping-to-signature span.
            # This keeps retired/internal compatibility endpoints out of the pre-runtime public contract.
            prefix_window = text[max(0, mapping.start() - 2000) : mapping.start()]
            member_boundary = max(prefix_window.rfind("}"), prefix_window.rfind(";"))
            declaration_prefix = prefix_window[member_boundary + 1 :]
            if re.search(r"@Hidden\b", declaration_prefix) or re.search(r"@Hidden\b", annotation_scope):
                continue
            operation_match = OPERATION.search(annotation_scope)
            operation_id = ""
            if operation_match:
                found = OP_ID.search(operation_match.group(1))
                operation_id = found.group(1) if found else ""
            if not operation_id:
                raise ContractError(f"operationId missing: {file.relative_to(root)} {method} {path}")
            parameters: list[dict[str, Any]] = []
            request_body: dict[str, Any] | None = None
            request_parts: list[dict[str, Any]] = []
            for raw in split_top_level(parameters_text):
                contract = parameter_contract(raw)
                if not contract:
                    continue
                if contract["kind"] == "part":
                    request_parts.append(contract)
                    continue
                if contract["kind"] == "body":
                    if request_body is not None:
                        raise ContractError(f"multiple request bodies: {operation_id}")
                    schema = contract["schema"]
                    if "$ref" in schema:
                        referenced_name = schema["$ref"].split("/")[-1]
                        if referenced_name in local_record_schemas:
                            inline_name = component_name(operation_id)
                            local_schema = local_record_schemas[referenced_name]
                            existing = schemas.get(inline_name)
                            if existing is not None and existing != local_schema:
                                raise ContractError(f"operation request schema collision: {inline_name}")
                            schemas[inline_name] = local_schema
                            schema = {"$ref": f"#/components/schemas/{inline_name}"}
                        else:
                            referenced_schemas.add(referenced_name)
                    else:
                        inline_name = component_name(operation_id)
                        existing = schemas.get(inline_name)
                        if existing is not None and existing != schema:
                            raise ContractError(f"inline request schema collision: {inline_name}")
                        schemas[inline_name] = schema
                        schema = {"$ref": f"#/components/schemas/{inline_name}"}
                    request_body = {"required": contract["required"], "schema": schema, "contentType": "application/json"}
                else:
                    parameters.append({key: value for key, value in contract.items() if key != "kind"})
                    schema = contract["schema"]
                    if "$ref" in schema:
                        referenced_schemas.add(schema["$ref"].split("/")[-1])
            if request_parts:
                if request_body is not None:
                    raise ContractError(f"mixed RequestBody/RequestPart is unsupported: {operation_id}")
                inline_name = component_name(operation_id)
                properties = {part["name"]: part["schema"] for part in request_parts}
                required_parts = [part["name"] for part in request_parts if part["required"]]
                multipart_schema = {
                    "type": "object",
                    "additionalProperties": False,
                    **({"required": required_parts} if required_parts else {}),
                    "properties": properties,
                    "description": "Controller-source multipart request schema.",
                }
                existing = schemas.get(inline_name)
                if existing is not None and existing != multipart_schema:
                    raise ContractError(f"multipart request schema collision: {inline_name}")
                schemas[inline_name] = multipart_schema
                request_body = {
                    "required": bool(required_parts),
                    "schema": {"$ref": f"#/components/schemas/{inline_name}"},
                    "contentType": "multipart/form-data",
                }
            declared = {(item["in"], item["name"]) for item in parameters}
            for name in PATH_PARAMETER.findall(path):
                if ("path", name) not in declared:
                    parameters.append({"name": name, "in": "path", "required": True, "schema": {"type": "string"}})
            declaration_annotations = declaration_prefix + "\n" + annotation_scope
            method_security = annotation_named_values(declaration_annotations, "SecurityRequirement", "name")
            security = list(dict.fromkeys([*class_security, *method_security]))
            # ADM mutation endpoints protected by the canonical session contract also require
            # the same-origin CSRF header at the browser/BFF boundary.
            if module == "ADM" and method in MUTATION_METHODS and "admSessionCookie" in security:
                security.append("admCsrfHeader")
            success_schema = response_schema(return_type)
            for referenced_name in schema_references(success_schema):
                referenced_schemas.add(referenced_name)
            records.append({
                "method": method,
                "template": path,
                "operationId": operation_id,
                "parameters": parameters,
                "requestBody": request_body,
                "responseSchema": success_schema,
                "responses": explicit_api_responses(declaration_annotations),
                "security": security,
                "source": file.relative_to(root).as_posix(),
            })
    if not records:
        raise ContractError(f"no {module} public operations discovered")
    duplicates = [key for key, count in Counter(record["operationId"] for record in records).items() if count > 1]
    contracts = [key for key, count in Counter((record["method"], record["template"]) for record in records).items() if count > 1]
    if duplicates:
        raise ContractError(f"duplicate operationId={duplicates[:20]}")
    if contracts:
        raise ContractError(f"duplicate method/path={contracts[:20]}")
    referenced_schemas.update(schema_references(schemas))
    for name in sorted(referenced_schemas):
        schemas.setdefault(name, {
            "type": "object",
            "additionalProperties": True,
            "description": "Controller-source pre-runtime placeholder; runtime OpenAPI must supply validation details.",
        })
    return sorted(records, key=lambda record: (record["method"], record["template"], record["operationId"])), schemas


def component_name(operation_id: str, suffix: str = "Request") -> str:
    value = re.sub(r"[^A-Za-z0-9_$]", "_", operation_id)
    return value[:1].upper() + value[1:] + suffix


def quote(value: str) -> str:
    return json.dumps(value, ensure_ascii=False)



def build_openapi_spec(module: str, records: list[dict[str, Any]], schemas: dict[str, dict[str, Any]]) -> dict[str, Any]:
    """Build the deterministic tracked pre-runtime OpenAPI contract from controller source."""
    paths: dict[str, dict[str, Any]] = {}
    for record in records:
        responses: dict[str, Any] = {
            "200": {
                "description": "Controller source contract response",
                "content": {"application/json": {"schema": record.get("responseSchema", {"$ref": "#/components/schemas/CpfControllerSourceResponse"})}},
            }
        }
        applicable = standard_error_statuses(module, record["method"], record["template"])
        explicit = record.get("responses", {})
        for status in applicable:
            responses.setdefault(status, error_response(status, explicit.get(status)))
        for status, description in explicit.items():
            responses.setdefault(status, error_response(status, description))
        operation: dict[str, Any] = {
            "operationId": record["operationId"],
            "responses": responses,
            "x-cpf-applicable-error-statuses": applicable,
            "x-cpf-controller-source": record["source"],
        }
        if record.get("security"):
            # Multiple named requirements form one AND security alternative.
            operation["security"] = [{name: [] for name in record["security"]}]
        if record["parameters"]:
            operation["parameters"] = record["parameters"]
        if record["requestBody"]:
            operation["requestBody"] = {
                "required": record["requestBody"]["required"],
                "content": {record["requestBody"].get("contentType", "application/json"): {"schema": record["requestBody"]["schema"]}},
            }
        paths.setdefault(record["template"], {})[record["method"].lower()] = operation
    all_schemas = {
        "CpfControllerSourceResponse": {"type": "object", "additionalProperties": True},
        "CpfApiError": {
            "type": "object", "additionalProperties": True,
            "required": ["status", "code", "message", "path"],
            "properties": {
                "timestamp": {"type": "string", "format": "date-time"},
                "status": {"type": "integer", "format": "int32"},
                "error": {"type": "string"}, "code": {"type": "string"},
                "message": {"type": "string"}, "path": {"type": "string"},
                "transactionId": {"type": "string", "minLength": 34, "maxLength": 34},
            },
        },
        **schemas,
    }
    components: dict[str, Any] = {"schemas": all_schemas}
    if module == "ADM":
        components["securitySchemes"] = {
            "admSessionCookie": {"type": "apiKey", "in": "cookie", "name": "JSESSIONID"},
            "admCsrfHeader": {"type": "apiKey", "in": "header", "name": "X-XSRF-TOKEN"},
        }
    return {
        "openapi": "3.1.0",
        "info": {"title": f"{MODULES[module][2]} controller source pre-runtime contract", "version": "0.0.0-pre-runtime"},
        "paths": paths,
        "components": components,
        "x-cpf-export-origin": "CONTROLLER_SOURCE_PRE_RUNTIME",
        "x-cpf-product-module": ("Backoffice" if module == "MBW" else module),
        "x-cpf-openapi-operation-count": len(records),
        "x-cpf-public-operation-count": len(records),
        "x-cpf-canonical-schema-version": 5,
        "x-cpf-controller-source-contract-version": 2,
        "x-cpf-release-eligible": False,
    }


def write(root: Path, module: str, output: Path, records: list[dict[str, Any]], schemas: dict[str, dict[str, Any]], openapi_output: Path | None = None) -> None:
    output.mkdir(parents=True, exist_ok=True)
    ids = " | ".join(quote(record["operationId"]) for record in records)
    rows = ",\n".join(
        f"  {{ method: {quote(record['method'])}, template: {quote(record['template'])}, operationId: {quote(record['operationId'])} }}"
        for record in records
    )
    contract = f'''// Generated from explicit Java controller annotations for pre-runtime compilation.
// Release verification requires replacement from canonical BACKEND_RUNTIME OpenAPI.
export type CpfOperationId = {ids};
export interface CpfOperationDescriptor {{ method: string; template: string; operationId: CpfOperationId; }}
export const cpfOperationDescriptors: readonly CpfOperationDescriptor[] = [
{rows}
] as const;
function matchesTemplate(template: string, pathname: string): boolean {{
  const expected=template.split("/"); const actual=pathname.split("/");
  if(expected.length!==actual.length)return false;
  return expected.every((segment,index)=>(segment.startsWith("{{")&&segment.endsWith("}}"))||segment===actual[index]);
}}
export function resolveCpfOperation(method: string, rawUrl: string): CpfOperationDescriptor {{
  const pathname=new URL(rawUrl,window.location.origin).pathname;
  const normalizedMethod=method.trim().toUpperCase();
  const found=cpfOperationDescriptors.find(value=>value.method===normalizedMethod&&matchesTemplate(value.template,pathname));
  if(!found)throw new Error(`CPF controller operation is not registered: ${{normalizedMethod}} ${{pathname}}`);
  return found;
}}
'''
    (output / "cpf-operation-contract.ts").write_text(contract, encoding="utf-8")
    lines = [
        "// Pre-runtime generated compatibility client. Runtime OpenAPI generation must replace this file.",
        'import { cpfGeneratedRequest } from "../shared/cpfApi";',
        "export interface CpfGeneratedRequestOptions { data?: unknown; signal?: AbortSignal; headers?: HeadersInit; path?: Record<string,string|number>; query?: Record<string,unknown>; }",
        'function renderPath(template:string,values:Record<string,string|number>={}):string{return template.replace(/\\{([^}]+)\\}/g,(_,name)=>{const value=values[name];if(value===undefined||value===null||String(value).trim()==="")throw new Error(`Missing path parameter: ${name}`);return encodeURIComponent(String(value));});}',
    ]
    for record in records:
        lines.append(
            f'export async function {record["operationId"]}<T=unknown>(options:CpfGeneratedRequestOptions={{}}):Promise<T>'
            f'{{return cpfGeneratedRequest<T>({{url:renderPath({quote(record["template"])},options.path),method:{quote(record["method"])},'
            'data:options.data,params:options.query,signal:options.signal,headers:options.headers});}'
        )
    (output / "cpf-api.ts").write_text("\n".join(lines) + "\n", encoding="utf-8")
    orval_dir = output / "orval"
    orval_dir.mkdir(parents=True, exist_ok=True)
    (orval_dir / "cpf-api.ts").write_text(
        "// CONTROLLER_SOURCE_PRE_RUNTIME adapter. @tanstack/vue-query is owned by shared cpfApi.\nexport * from \"../cpf-api\";\n",
        encoding="utf-8",
    )
    if openapi_output is not None:
        spec = build_openapi_spec(module, records, schemas)
        openapi_output.parent.mkdir(parents=True, exist_ok=True)
        openapi_output.write_text(json.dumps(spec, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n", encoding="utf-8")

    def sha256_file(path: Path) -> str:
        return hashlib.sha256(path.read_bytes()).hexdigest()

    frontend_root = output.parents[1]
    openapi_path = openapi_output if openapi_output is not None else frontend_root / "openapi/cpf-openapi.json"
    config_path = frontend_root / "orval.config.ts"
    lock_path = frontend_root / "package-lock.json"
    package_path = frontend_root / "package.json"
    for required in (openapi_path, config_path, lock_path, package_path):
        if not required.is_file():
            raise ContractError(f"marker input missing: {required}")
    package = json.loads(package_path.read_text(encoding="utf-8"))
    generated_files = [
        {"path": file.relative_to(frontend_root).as_posix(), "sha256": sha256_file(file)}
        for file in sorted(output.rglob("*.ts"))
    ]
    operation_ids = sorted(record["operationId"] for record in records)
    marker = {
        "schemaVersion": 3,
        "identityPolicy": "TRACKED_HASHES_RELEASE_SHA_IN_EVIDENCE",
        "origin": "CONTROLLER_SOURCE_PRE_RUNTIME",
        "releaseEligible": False,
        "requiredReplacementOrigin": "BACKEND_RUNTIME",
        "openApiPath": openapi_path.relative_to(frontend_root).as_posix(),
        "openApiSha256": sha256_file(openapi_path),
        "openApiOperationCount": len(operation_ids),
        "openApiOperationIdsSha256": hashlib.sha256("\n".join(operation_ids).encode()).hexdigest(),
        "generator": {"name": "controller-source-contract", "version": "2"},
        "generatorConfigPath": config_path.relative_to(frontend_root).as_posix(),
        "generatorConfigSha256": sha256_file(config_path),
        "packageLockPath": lock_path.relative_to(frontend_root).as_posix(),
        "packageLockSha256": sha256_file(lock_path),
        "nodeRequirement": package.get("engines", {}).get("node"),
        "npmRequirement": package.get("engines", {}).get("npm"),
        "generatedFiles": generated_files,
        "generatedFileSetSha256": hashlib.sha256(
            "\n".join(f"{item['path']}:{item['sha256']}" for item in generated_files).encode()
        ).hexdigest(),
        "sanitized": True,
    }
    if not marker["nodeRequirement"] or not marker["npmRequirement"]:
        raise ContractError("Node/npm compatibility requirement missing")
    (output / ".cpf-openapi-source.json").write_text(json.dumps(marker, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--module", choices=sorted(MODULES), default="ADM")
    parser.add_argument("--output", type=Path)
    parser.add_argument("--openapi-output", type=Path)
    parser.add_argument("--openapi-only", action="store_true", help="Write only the controller-source OpenAPI contract without frontend compatibility artifacts")
    parser.add_argument("--check-openapi", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    default_output = Path("cpf-admin/frontend/src/generated" if args.module == "ADM" else ("cpf-backoffice-web/frontend/src/generated" if args.module == "MBW" else "cpf-backoffice-web/frontend/src/generated"))
    raw_output = args.output or default_output
    output = raw_output if raw_output.is_absolute() else root / raw_output
    records, schemas = discover(root, args.module)
    default_openapi = Path("cpf-admin/frontend/openapi/cpf-openapi.json" if args.module == "ADM" else ("cpf-backoffice/online/openapi/cpf-openapi.json" if args.module == "MBW" else "cpf-backoffice/online/openapi/cpf-openapi.json"))
    openapi_output = (args.openapi_output or default_openapi)
    openapi_output = openapi_output if openapi_output.is_absolute() else root / openapi_output
    if args.check_openapi:
        tracked = openapi_output
        if not tracked.is_file():
            raise ContractError(f"tracked pre-runtime OpenAPI missing: {tracked}")
        actual = json.loads(tracked.read_text(encoding="utf-8"))
        expected = build_openapi_spec(args.module, records, schemas)
        if actual != expected:
            raise ContractError(f"tracked pre-runtime OpenAPI drift: module={args.module} path={tracked}")
        print(f"[PASS] tracked pre-runtime OpenAPI current module={args.module} operations={len(records)} path={tracked}")
        return 0
    if args.openapi_only:
        spec = build_openapi_spec(args.module, records, schemas)
        openapi_output.parent.mkdir(parents=True, exist_ok=True)
        openapi_output.write_text(json.dumps(spec, ensure_ascii=False, sort_keys=True, separators=(",", ":")) + "\n", encoding="utf-8")
        print(f"[PASS] pre-runtime controller OpenAPI module={args.module} operations={len(records)} output={openapi_output}")
        return 0
    write(root, args.module, output, records, schemas, openapi_output)
    print(f"[PASS] pre-runtime controller operation contract module={args.module} operations={len(records)} output={output}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except ContractError as error:
        print(f"[FAIL] {error}", file=sys.stderr)
        raise SystemExit(1)
