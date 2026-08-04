from __future__ import annotations
import csv, hashlib, importlib.util, tempfile
from argparse import Namespace
from pathlib import Path
import pytest
SCRIPT=Path(__file__).parents[1]/"build-cpf-full-qa-ledgers.py"
def load():
    spec=importlib.util.spec_from_file_location("builder",SCRIPT); mod=importlib.util.module_from_spec(spec); assert spec.loader; spec.loader.exec_module(mod); return mod
def write_csv(path,fields,rows):
    path.parent.mkdir(parents=True,exist_ok=True)
    with path.open("w",encoding="utf-8-sig",newline="") as h:
        w=csv.DictWriter(h,fieldnames=fields); w.writeheader(); w.writerows(rows)
def make_index(root,stem,part,id_field):
    with part.open(encoding="utf-8-sig",newline="") as h: rows=list(csv.DictReader(h))
    raw=part.read_bytes(); write_csv(root/f"cpf-docs/work/current/{stem}.csv",["part_sequence","part_path","part_record_count","first_record_id","last_record_id","size_bytes","sha256","logical_record_count"],[{"part_sequence":1,"part_path":part.relative_to(root).as_posix(),"part_record_count":len(rows),"first_record_id":rows[0][id_field],"last_record_id":rows[-1][id_field],"size_bytes":len(raw),"sha256":hashlib.sha256(raw).hexdigest(),"logical_record_count":len(rows)}])
def fixture(root,sha):
    req=root/"cpf-docs/work/current/CPF_REQUIREMENT_MASTER.parts/part.csv"; write_csv(req,["requirement_id","requirement","priority","owner_module","owner_package","source_basis","change_target","actual_consumer","acceptance_criteria","verification_method","regression_protection"],[{"requirement_id":"CPF-FR-000001","requirement":"r","priority":"P0","owner_module":"cpf-core","owner_package":"com.cpf","source_basis":"s","change_target":"c","actual_consumer":"a","acceptance_criteria":"ac","verification_method":"v","regression_protection":"t"}]); make_index(root,"CPF_REQUIREMENT_MASTER",req,"requirement_id")
    sc=root/"cpf-docs/work/current/CPF_SCENARIO_MASTER.parts/part.csv"; write_csv(sc,["scenario_id","linked_requirement_id","work_package_id","scenario_type","title","preconditions","steps","expected_result","failure_criteria","environment","topology","required_evidence"],[{"scenario_id":"CPF-SC-000001","linked_requirement_id":"CPF-FR-000001","work_package_id":"WP-1","scenario_type":"POSITIVE","title":"s","preconditions":"p","steps":"s","expected_result":"e","failure_criteria":"f","environment":"local","topology":"single","required_evidence":"log"}]); make_index(root,"CPF_SCENARIO_MASTER",sc,"scenario_id")
    ex=root/"cpf-docs/work/current/CPF_EXECUTION_SEQUENCE.parts/part.csv"; write_csv(ex,["execution_order","requirement_id","work_package_id"],[{"execution_order":"00-00000001","requirement_id":"CPF-FR-000001","work_package_id":"WP-1"}]); make_index(root,"CPF_EXECUTION_SEQUENCE",ex,"execution_order")
    tracked=root/"tracked.txt"; tracked.write_text("tracked\n",encoding="utf-8")
    manifest=root/"SOURCE_MANIFEST.csv"; write_csv(manifest,["path","sha256","baseline_sha"],[{"path":"tracked.txt","sha256":hashlib.sha256(tracked.read_bytes()).hexdigest(),"baseline_sha":sha}])
    return manifest
def args(root,sha,manifest):
    return Namespace(root=str(root),expected_sha=sha,identity_mode="auto",source_manifest=str(manifest),source_manifest_sha256=hashlib.sha256(manifest.read_bytes()).hexdigest(),generated_at="2026-08-04T12:30:00+09:00",updated_by="QA-B",requirement_output="cpf-docs/work/current/REQUIREMENT_STATUS.csv",scenario_output="cpf-docs/work/current/SCENARIO_STATUS.csv",json_output=None)
def test_manifest_identity_builds_and_never_marks_pass():
    with tempfile.TemporaryDirectory() as d:
        root=Path(d); sha="a"*40; manifest=fixture(root,sha); result=load().build(args(root,sha,manifest)); assert result["sourceIdentity"]["mode"]=="source-manifest"
        _,rows=load().read_csv(root/"cpf-docs/work/current/REQUIREMENT_STATUS.csv"); assert rows[0]["QA_검수여부"]=="아니오" and rows[0]["verification_status"]=="미검증"
def test_stale_manifest_sha_fails_closed():
    with tempfile.TemporaryDirectory() as d:
        root=Path(d); sha="a"*40; manifest=fixture(root,"b"*40)
        with pytest.raises(Exception,match="baseline mismatch"): load().build(args(root,sha,manifest))
def test_manifest_content_hash_mismatch_fails_closed():
    with tempfile.TemporaryDirectory() as d:
        root=Path(d); sha="a"*40; manifest=fixture(root,sha); a=args(root,sha,manifest); (root/"tracked.txt").write_text("tampered\n")
        with pytest.raises(Exception,match="hash mismatch"): load().build(a)
def test_no_git_and_no_manifest_fails_closed():
    with tempfile.TemporaryDirectory() as d:
        root=Path(d); sha="a"*40; manifest=fixture(root,sha); a=args(root,sha,manifest); a.source_manifest=None; a.source_manifest_sha256=None
        with pytest.raises(Exception,match="git-clean identity"): load().build(a)

import subprocess


def init_git(root: Path) -> str:
    subprocess.run(["git", "init", "-q"], cwd=root, check=True)
    subprocess.run(["git", "config", "user.email", "qa-b@example.invalid"], cwd=root, check=True)
    subprocess.run(["git", "config", "user.name", "CPF QA-B"], cwd=root, check=True)
    subprocess.run(["git", "add", "."], cwd=root, check=True)
    subprocess.run(["git", "commit", "-q", "-m", "fixture"], cwd=root, check=True)
    return subprocess.run(
        ["git", "rev-parse", "HEAD"], cwd=root, check=True, text=True, capture_output=True
    ).stdout.strip()


def git_args(root: Path, sha: str) -> Namespace:
    return Namespace(
        root=str(root), expected_sha=sha, identity_mode="auto", source_manifest=None,
        source_manifest_sha256=None, generated_at="2026-08-04T12:30:00+09:00",
        updated_by="QA-B",
        requirement_output="generated/REQUIREMENT_STATUS.csv",
        scenario_output="generated/SCENARIO_STATUS.csv", json_output=None,
    )


def test_clean_exact_git_head_builds():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        fixture(root, "a" * 40)
        head = init_git(root)
        result = load().build(git_args(root, head))
        assert result["sourceIdentity"] == {
            "mode": "git-clean", "actualHead": head, "workingTree": "clean"
        }


def test_wrong_git_head_fails_closed():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        fixture(root, "a" * 40)
        head = init_git(root)
        wrong = ("0" if head[0] != "0" else "1") + head[1:]
        with pytest.raises(Exception, match="HEAD mismatch"):
            load().build(git_args(root, wrong))


def test_dirty_git_working_tree_fails_closed():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        fixture(root, "a" * 40)
        head = init_git(root)
        (root / "tracked.txt").write_text("dirty\n", encoding="utf-8")
        with pytest.raises(Exception, match="dirty Working Tree"):
            load().build(git_args(root, head))


def test_dirty_overlay_can_be_verified_by_explicit_manifest_mode():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        manifest = fixture(root, "a" * 40)
        head = init_git(root)
        # The manifest declares the immutable base HEAD, while hashes cover overlay files.
        _, rows = load().read_csv(manifest)
        rows[0]["baseline_sha"] = head
        (root / "tracked.txt").write_text("overlay\n", encoding="utf-8")
        rows[0]["sha256"] = hashlib.sha256((root / "tracked.txt").read_bytes()).hexdigest()
        write_csv(manifest, ["path", "sha256", "baseline_sha"], rows)
        a = args(root, head, manifest)
        a.identity_mode = "source-manifest"
        a.source_manifest_sha256 = hashlib.sha256(manifest.read_bytes()).hexdigest()
        result = load().build(a)
        assert result["sourceIdentity"]["mode"] == "source-manifest"
        assert result["sourceIdentity"]["baseHead"] == head
        assert result["sourceIdentity"]["workingTree"] == "dirty-overlay"


def test_manifest_mode_rejects_wrong_overlay_base_head():
    with tempfile.TemporaryDirectory() as d:
        root = Path(d)
        manifest = fixture(root, "a" * 40)
        head = init_git(root)
        wrong = ("0" if head[0] != "0" else "1") + head[1:]
        _, rows = load().read_csv(manifest)
        rows[0]["baseline_sha"] = wrong
        write_csv(manifest, ["path", "sha256", "baseline_sha"], rows)
        a = args(root, wrong, manifest)
        a.identity_mode = "source-manifest"
        a.source_manifest_sha256 = hashlib.sha256(manifest.read_bytes()).hexdigest()
        with pytest.raises(Exception, match="overlay base HEAD mismatch"):
            load().build(a)
