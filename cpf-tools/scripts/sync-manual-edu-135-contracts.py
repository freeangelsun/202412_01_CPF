#!/usr/bin/env python3
from __future__ import annotations

import argparse
import json
import shutil
import tempfile
from pathlib import Path

ROLE_BY_FAMILY = {
    "DEV": "CPF_EDU_DEVELOPER",
    "BAT": "CPF_BATCH_OPERATOR",
    "ADM": "CPF_ADM_OPERATOR",
    "BZA": "CPF_BZA_OPERATOR",
    "GW": "CPF_GATEWAY_OPERATOR",
    "OPS": "CPF_PLATFORM_OPERATOR",
}


def canonical_role(requirement_id: str) -> str:
    try:
        return ROLE_BY_FAMILY[requirement_id.split("-", 2)[1]]
    except Exception as exc:
        raise ValueError(f"unsupported requirement id: {requirement_id}") from exc


def expected_contract(feature: dict) -> dict:
    role = canonical_role(feature["requirementId"])
    if feature.get("requiredRole") != role:
        raise ValueError(
            f"catalog requiredRole drift {feature['requirementId']}: "
            f"expected={role} actual={feature.get('requiredRole')}"
        )
    return {
        "requirementId": feature["requirementId"],
        "title": feature["title"],
        "implementationPackage": feature["implementationPackage"],
        "owner": feature["owner"],
        "requiredRole": role,
        "inputFields": feature["requiredFields"],
        "businessStates": feature["businessStates"],
        "workflowSteps": feature["steps"],
        "failurePoints": feature["failurePoints"],
        "exceptionScenarios": feature["exceptionScenarios"],
        "requiredTests": feature["requiredVerification"],
        "readOnly": feature["readOnly"],
        "manualAnchor": feature["manualAnchor"],
        "idempotent": feature["idempotent"],
        "versioned": feature["versioned"],
        "leaseRequired": feature["leaseRequired"],
        "externalEffect": feature["externalEffect"],
        "compensationSupported": feature["compensationSupported"],
        "rollbackSupported": feature["rollbackSupported"],
        "consumerBinding": feature["consumerBinding"],
        "featurePack": feature["featurePack"],
        "optionalFeature": feature["optionalFeature"],
        "featureToggle": feature["featureToggle"],
        "generatedDomainIndependent": feature["generatedDomainIndependent"],
        "productModuleIndependent": feature["productModuleIndependent"],
        "databaseOwner": feature["databaseOwner"],
    }


def semantic_diff(expected: dict, actual: dict) -> list[str]:
    errors: list[str] = []
    expected_keys = list(expected)
    if set(actual) != set(expected):
        errors.append(
            f"keys expected={sorted(expected)} actual={sorted(actual)}"
        )
    for key in expected_keys:
        if actual.get(key) != expected.get(key):
            errors.append(f"{key}: expected={expected.get(key)!r} actual={actual.get(key)!r}")
    return errors


def load_catalog(root: Path) -> dict:
    path = root / "cpf-reference/src/main/resources/edu/manual-135-catalog.json"
    data = json.loads(path.read_text(encoding="utf-8-sig"))
    if data.get("featureCount") != 135 or len(data.get("features", [])) != 135:
        raise ValueError("manual-135 catalog must contain exact 135 features")
    ids = [f["requirementId"] for f in data["features"]]
    if len(ids) != len(set(ids)):
        raise ValueError("manual-135 catalog contains duplicate requirementId")
    return data


def synchronize(root: Path, write: bool) -> list[str]:
    catalog = load_catalog(root)
    errors: list[str] = []
    for feature in catalog["features"]:
        rid = feature["requirementId"]
        expected = expected_contract(feature)
        target = root / feature["resourceContract"]
        if write:
            target.parent.mkdir(parents=True, exist_ok=True)
            target.write_text(
                json.dumps(expected, ensure_ascii=False, indent=2) + "\n",
                encoding="utf-8",
            )
            continue
        if not target.is_file():
            errors.append(f"{rid}: missing {feature['resourceContract']}")
            continue
        try:
            actual = json.loads(target.read_text(encoding="utf-8-sig"))
        except Exception as exc:
            errors.append(f"{rid}: unreadable scenario contract: {exc}")
            continue
        diff = semantic_diff(expected, actual)
        errors.extend(f"{rid}: {item}" for item in diff)
    return errors


def mutation_self_test(root: Path) -> None:
    catalog = load_catalog(root)
    feature = catalog["features"][0]
    expected = expected_contract(feature)
    mutated = json.loads(json.dumps(expected, ensure_ascii=False))
    mutated["requiredRole"] = "CPF_INVALID_ROLE"
    if not semantic_diff(expected, mutated):
        raise AssertionError("requiredRole mutation was not detected")
    mutated = json.loads(json.dumps(expected, ensure_ascii=False))
    mutated["readOnly"] = not bool(expected["readOnly"])
    if not semantic_diff(expected, mutated):
        raise AssertionError("readOnly mutation was not detected")
    mutated = json.loads(json.dumps(expected, ensure_ascii=False))
    mutated["consumerBinding"]["operation"] = "MUTATED_OPERATION"
    if not semantic_diff(expected, mutated):
        raise AssertionError("consumerBinding mutation was not detected")


def main() -> int:
    parser = argparse.ArgumentParser()
    parser.add_argument("--root", type=Path, default=Path.cwd())
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--self-test", action="store_true")
    args = parser.parse_args()
    root = args.root.resolve()
    if args.self_test:
        mutation_self_test(root)
    errors = synchronize(root, args.write)
    if errors:
        for error in errors:
            print("[CPF][EDU135][CONTRACT][FAIL]", error)
        return 1
    mode = "WRITE" if args.write else "CHECK"
    print(f"[CPF][EDU135][CONTRACT][PASS] mode={mode} features=135 selfTest={args.self_test}")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
