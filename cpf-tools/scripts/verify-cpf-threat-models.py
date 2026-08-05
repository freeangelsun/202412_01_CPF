#!/usr/bin/env python3
"""Validate CPF threat-model lifecycle manifests and changed-path coverage."""
from __future__ import annotations

import argparse
import fnmatch
import json
import sys
from datetime import datetime, timezone
from pathlib import Path, PurePosixPath
from typing import Iterable


class ThreatModelError(RuntimeError):
    pass


SEVERITY = {"low": 1, "medium": 2, "high": 3, "critical": 4}
ALLOWED_THREAT_STATUS = {"mitigated", "accepted"}
REQUIRED_TOP_LEVEL = {
    "schema_version", "model_id", "version", "title", "owner", "reviewers",
    "scope_paths", "review_triggers", "assets", "data_flows", "trust_boundaries",
    "mitigations", "threats", "reviewed_at", "expires_at"
}


def _parse_time(value: object, field: str) -> datetime:
    if not isinstance(value, str) or not value.strip():
        raise ThreatModelError(f"{field} is required")
    try:
        parsed = datetime.fromisoformat(value.replace("Z", "+00:00"))
    except ValueError as exc:
        raise ThreatModelError(f"{field} must be ISO-8601") from exc
    if parsed.tzinfo is None:
        raise ThreatModelError(f"{field} must include timezone")
    return parsed.astimezone(timezone.utc)


def _strings(value: object, field: str, non_empty: bool = True) -> list[str]:
    if not isinstance(value, list) or (non_empty and not value) or any(not isinstance(v, str) or not v.strip() for v in value):
        raise ThreatModelError(f"{field} must be {'a non-empty ' if non_empty else 'an '}array of strings")
    return [v.strip() for v in value]


def _safe_repo_path(raw: str) -> PurePosixPath:
    path = PurePosixPath(raw.replace("\\", "/"))
    if path.is_absolute() or not path.parts or any(p in {"", ".", ".."} for p in path.parts) or ":" in path.parts[0]:
        raise ThreatModelError(f"unsafe repository path: {raw!r}")
    return path


def validate_manifest(value: object, source: Path, repo_root: Path, now: datetime) -> dict:
    if not isinstance(value, dict):
        raise ThreatModelError(f"{source}: manifest must be an object")
    missing = sorted(REQUIRED_TOP_LEVEL - value.keys())
    if missing:
        raise ThreatModelError(f"{source}: missing fields: {', '.join(missing)}")
    if value["schema_version"] != "1.0":
        raise ThreatModelError(f"{source}: schema_version must be 1.0")
    model_id = str(value["model_id"]).strip()
    if not model_id:
        raise ThreatModelError(f"{source}: model_id is required")
    owner = str(value["owner"]).strip()
    if not owner:
        raise ThreatModelError(f"{source}: owner is required")
    reviewers = _strings(value["reviewers"], f"{source}: reviewers")
    scope_paths = _strings(value["scope_paths"], f"{source}: scope_paths")
    for pattern in scope_paths:
        _safe_repo_path(pattern.replace("**", "placeholder").replace("*", "placeholder"))
    _strings(value["review_triggers"], f"{source}: review_triggers")
    assets = value["assets"]
    if not isinstance(assets, list) or not assets:
        raise ThreatModelError(f"{source}: assets must be non-empty")
    asset_ids: set[str] = set()
    for i, asset in enumerate(assets):
        if not isinstance(asset, dict):
            raise ThreatModelError(f"{source}: assets[{i}] must be an object")
        asset_id = str(asset.get("id", "")).strip()
        if not asset_id or asset_id in asset_ids:
            raise ThreatModelError(f"{source}: duplicate/empty asset id {asset_id!r}")
        asset_ids.add(asset_id)
        if not str(asset.get("classification", "")).strip():
            raise ThreatModelError(f"{source}: asset {asset_id} classification is required")
    boundaries = value["trust_boundaries"]
    if not isinstance(boundaries, list) or not boundaries:
        raise ThreatModelError(f"{source}: trust_boundaries must be non-empty")
    boundary_ids = {str(v.get("id", "")).strip() for v in boundaries if isinstance(v, dict)}
    if "" in boundary_ids or len(boundary_ids) != len(boundaries):
        raise ThreatModelError(f"{source}: trust boundary ids must be unique and non-empty")
    flows = value["data_flows"]
    if not isinstance(flows, list) or not flows:
        raise ThreatModelError(f"{source}: data_flows must be non-empty")
    flow_ids: set[str] = set()
    for flow in flows:
        if not isinstance(flow, dict):
            raise ThreatModelError(f"{source}: data flow must be an object")
        flow_id = str(flow.get("id", "")).strip()
        if not flow_id or flow_id in flow_ids:
            raise ThreatModelError(f"{source}: data flow ids must be unique")
        flow_ids.add(flow_id)
        refs = _strings(flow.get("assets"), f"{source}: flow {flow_id}.assets")
        unknown = set(refs) - asset_ids
        if unknown:
            raise ThreatModelError(f"{source}: flow {flow_id} references unknown assets {sorted(unknown)}")
        boundary = str(flow.get("crosses_boundary", "")).strip()
        if boundary not in boundary_ids:
            raise ThreatModelError(f"{source}: flow {flow_id} references unknown trust boundary {boundary!r}")
    mitigations = value["mitigations"]
    if not isinstance(mitigations, list) or not mitigations:
        raise ThreatModelError(f"{source}: mitigations must be non-empty")
    mitigation_ids: set[str] = set()
    for mitigation in mitigations:
        if not isinstance(mitigation, dict):
            raise ThreatModelError(f"{source}: mitigation must be an object")
        mitigation_id = str(mitigation.get("id", "")).strip()
        if not mitigation_id or mitigation_id in mitigation_ids:
            raise ThreatModelError(f"{source}: mitigation ids must be unique")
        mitigation_ids.add(mitigation_id)
        evidence = _strings(mitigation.get("evidence_paths"), f"{source}: mitigation {mitigation_id}.evidence_paths")
        for raw in evidence:
            rel = _safe_repo_path(raw)
            target = repo_root.joinpath(*rel.parts).resolve()
            try:
                target.relative_to(repo_root.resolve())
            except ValueError as exc:
                raise ThreatModelError(f"{source}: mitigation evidence escapes repo: {raw}") from exc
            if not target.exists():
                raise ThreatModelError(f"{source}: mitigation evidence missing: {raw}")
    threats = value["threats"]
    if not isinstance(threats, list) or not threats:
        raise ThreatModelError(f"{source}: threats must be non-empty")
    threat_ids: set[str] = set()
    accepted = 0
    for threat in threats:
        if not isinstance(threat, dict):
            raise ThreatModelError(f"{source}: threat must be an object")
        threat_id = str(threat.get("id", "")).strip()
        if not threat_id or threat_id in threat_ids:
            raise ThreatModelError(f"{source}: threat ids must be unique")
        threat_ids.add(threat_id)
        if not str(threat.get("abuse_case", "")).strip():
            raise ThreatModelError(f"{source}: threat {threat_id} abuse_case is required")
        affected = _strings(threat.get("affected_assets"), f"{source}: threat {threat_id}.affected_assets")
        unknown_assets = set(affected) - asset_ids
        if unknown_assets:
            raise ThreatModelError(f"{source}: threat {threat_id} references unknown assets {sorted(unknown_assets)}")
        mitigation_refs = _strings(threat.get("mitigations"), f"{source}: threat {threat_id}.mitigations")
        unknown_mitigations = set(mitigation_refs) - mitigation_ids
        if unknown_mitigations:
            raise ThreatModelError(f"{source}: threat {threat_id} references unknown mitigations {sorted(unknown_mitigations)}")
        status = str(threat.get("status", "")).strip()
        if status not in ALLOWED_THREAT_STATUS:
            raise ThreatModelError(f"{source}: threat {threat_id} status must be mitigated or accepted")
        residual = threat.get("residual_risk")
        if not isinstance(residual, dict):
            raise ThreatModelError(f"{source}: threat {threat_id}.residual_risk must be an object")
        severity = str(residual.get("severity", "")).lower()
        if severity not in SEVERITY:
            raise ThreatModelError(f"{source}: threat {threat_id} residual severity is invalid")
        if status == "accepted":
            accepted += 1
            if not str(residual.get("accepted_by", "")).strip() or not str(residual.get("reason", "")).strip():
                raise ThreatModelError(f"{source}: accepted threat {threat_id} requires accepted_by and reason")
            if SEVERITY[severity] >= SEVERITY["high"]:
                raise ThreatModelError(f"{source}: high/critical residual risk cannot be accepted: {threat_id}")
    reviewed_at = _parse_time(value["reviewed_at"], f"{source}: reviewed_at")
    expires_at = _parse_time(value["expires_at"], f"{source}: expires_at")
    if reviewed_at > now:
        raise ThreatModelError(f"{source}: reviewed_at is in the future")
    if expires_at <= now:
        raise ThreatModelError(f"{source}: threat model review expired")
    if expires_at <= reviewed_at:
        raise ThreatModelError(f"{source}: expires_at must be after reviewed_at")
    return {
        "model_id": model_id,
        "source": source.as_posix(),
        "owner": owner,
        "reviewer_count": len(reviewers),
        "scope_paths": scope_paths,
        "asset_count": len(asset_ids),
        "data_flow_count": len(flow_ids),
        "trust_boundary_count": len(boundary_ids),
        "threat_count": len(threat_ids),
        "mitigation_count": len(mitigation_ids),
        "accepted_risk_count": accepted,
        "expires_at": expires_at.isoformat().replace("+00:00", "Z"),
    }


def verify(repo_root: Path, manifest_dir: Path, changed_paths: list[str], now: datetime | None = None) -> dict:
    now = (now or datetime.now(timezone.utc)).astimezone(timezone.utc)
    manifest_paths = sorted(manifest_dir.glob("*.json"))
    if not manifest_paths:
        raise ThreatModelError("no threat model manifests found")
    models: list[dict] = []
    ids: set[str] = set()
    for path in manifest_paths:
        try:
            value = json.loads(path.read_text(encoding="utf-8"))
        except (OSError, json.JSONDecodeError) as exc:
            raise ThreatModelError(f"cannot read {path}: {exc}") from exc
        model = validate_manifest(value, path, repo_root, now)
        if model["model_id"] in ids:
            raise ThreatModelError(f"duplicate model_id: {model['model_id']}")
        ids.add(model["model_id"])
        models.append(model)
    uncovered: list[str] = []
    for raw in changed_paths:
        changed = _safe_repo_path(raw).as_posix()
        if not any(any(fnmatch.fnmatch(changed, pattern) for pattern in model["scope_paths"]) for model in models):
            uncovered.append(changed)
    if uncovered:
        raise ThreatModelError("changed paths lack threat-model coverage: " + ", ".join(sorted(uncovered)))
    return {
        "status": "PASS",
        "model_count": len(models),
        "changed_path_count": len(changed_paths),
        "uncovered_changed_paths": [],
        "models": models,
    }


def _parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(description=__doc__)
    p.add_argument("--repo-root", required=True, type=Path)
    p.add_argument("--manifest-dir", required=True, type=Path)
    p.add_argument("--changed-path", action="append", default=[])
    p.add_argument("--output-json", type=Path)
    return p


def main(argv: Iterable[str] | None = None) -> int:
    args = _parser().parse_args(argv)
    try:
        result = verify(args.repo_root, args.manifest_dir, args.changed_path)
    except ThreatModelError as exc:
        result = {"status": "FAIL", "error": str(exc)}
        code = 2
    else:
        code = 0
    if args.output_json:
        args.output_json.parent.mkdir(parents=True, exist_ok=True)
        args.output_json.write_text(json.dumps(result, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
    print(json.dumps(result, ensure_ascii=False))
    return code


if __name__ == "__main__":
    raise SystemExit(main())
